package com.pgshare.studentroomsharingapp.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pgshare.studentroomsharingapp.model.Message
import com.pgshare.studentroomsharingapp.model.RecentChat
import com.pgshare.studentroomsharingapp.model.Room
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    private fun safeUserId(): String = auth.currentUser?.uid ?: ""

    // ─── Rooms ────────────────────────────────────────────

    fun observeRooms(): Flow<List<Room>> = callbackFlow {
        val listener = database.getReference("Rooms")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val rooms = mutableListOf<Room>()
                    for (data in snapshot.children) {
                        data.getValue(Room::class.java)?.let { rooms.add(it) }
                    }
                    trySend(rooms)
                }

                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.w("FirebaseRepository", "observeRooms cancelled: ${error.message}")
                    trySend(emptyList())
                }
            })

        awaitClose { database.getReference("Rooms").removeEventListener(listener) }
    }

    suspend fun publishRoom(room: Room): Result<Unit> = runCatching {
        val ref = database.getReference("Rooms")
        val id = if (room.id.isNullOrEmpty()) ref.push().key ?: throw Exception("Failed to generate room key") else room.id!!
        room.id = id
        ref.child(id).setValue(room).await()
    }

    suspend fun seedDemoRooms(): Result<Unit> = runCatching {
        val roomsRef = database.getReference("Rooms")
        roomsRef.removeValue().await()

        val demoListings = listOf(
            Room(
                id = "demo_room_1",
                userId = "demo_owner_1",
                roomName = "Luxury Student Studio Near Campus",
                location = "University North Gate",
                description = "Type: Private Room\n\nSpacious studio apartment with high-speed WiFi, study desk, and 24/7 security. Perfect for university students.",
                price = "8500",
                deposit = "17000",
                imageUrls = arrayListOf("https://images.unsplash.com/photo-1555854877-bab0e564b8d5?auto=format&fit=crop&w=800&q=80"),
                amenities = arrayListOf("High-Speed WiFi", "Air Conditioning", "Fully Furnished", "Attached Bath"),
                isRoomBooked = false
            ),
            Room(
                id = "demo_room_2",
                userId = "demo_owner_2",
                roomName = "Cozy Shared PG for Students",
                location = "Metro Station West",
                description = "Type: PG\n\nClean and affordable shared room with mess facility, daily housekeeping, and laundry service included.",
                price = "6000",
                deposit = "12000",
                imageUrls = arrayListOf("https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?auto=format&fit=crop&w=800&q=80"),
                amenities = arrayListOf("High-Speed WiFi", "Washing Machine", "Attached Bath"),
                isRoomBooked = false
            ),
            Room(
                id = "demo_room_3",
                userId = "demo_owner_3",
                roomName = "Premium Private Single Room",
                location = "Tech Park Sector 5",
                description = "Type: Flat\n\nFully furnished single room in a modern 3BHK flat with balcony view, power backup, and modular kitchen access.",
                price = "12500",
                deposit = "25000",
                imageUrls = arrayListOf("https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&w=800&q=80"),
                amenities = arrayListOf("High-Speed WiFi", "Air Conditioning", "Balcony", "Fully Furnished"),
                isRoomBooked = false
            ),
            Room(
                id = "demo_room_4",
                userId = "demo_owner_4",
                roomName = "Modern Female Student Flat",
                location = "College Green Avenue",
                description = "Type: Flat\n\nSafe and peaceful environment for female students with gym access, power backup, and close proximity to public transit.",
                price = "9000",
                deposit = "18000",
                imageUrls = arrayListOf("https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=800&q=80"),
                amenities = arrayListOf("High-Speed WiFi", "Air Conditioning", "Fully Furnished"),
                isRoomBooked = true
            )
        )

        for (room in demoListings) {
            roomsRef.child(room.id!!).setValue(room).await()
        }
    }

    // ─── Users ────────────────────────────────────────────

    suspend fun getUserName(uid: String): String? {
        return try {
            val snapshot = database.getReference("Users").child(uid).get().await()
            val username = snapshot.child("username").getValue(String::class.java)
            val email = snapshot.child("email").getValue(String::class.java)
            when {
                !username.isNullOrEmpty() -> username
                !email.isNullOrEmpty() -> email.substringBefore("@")
                else -> null
            }
        } catch (e: Exception) {
            android.util.Log.w("FirebaseRepository", "getUserName failed for $uid: ${e.message}")
            null
        }
    }

    fun observeUserProfile(): Flow<UserProfile?> = callbackFlow {
        val uid = getCurrentUserId() ?: run { trySend(null); close(); return@callbackFlow }

        val listener = database.getReference("Users").child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("username").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
                    val role = snapshot.child("role").getValue(String::class.java)
                    trySend(UserProfile(username, email, profileImageUrl, role))
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(null)
                    close()
                }
            })

        awaitClose { database.getReference("Users").child(uid).removeEventListener(listener) }
    }

    suspend fun saveUsername(name: String) {
        val uid = getCurrentUserId() ?: return
        database.getReference("Users").child(uid).child("username").setValue(name).await()
    }

    // ─── Chats ────────────────────────────────────────────

    fun observeMessages(chatRoomId: String): Flow<List<Message>> = callbackFlow {
        val ref = database.getReference("chats").child(chatRoomId).child("messages")
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (data in snapshot.children) {
                    data.getValue(Message::class.java)?.let { messages.add(it) }
                }
                trySend(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
                close()
            }
        })

        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun sendMessage(chatRoomId: String, senderId: String, receiverId: String, roomId: String, text: String) {
        val rootRef = database.reference
        val messageId = rootRef.child("chats").child(chatRoomId).child("messages").push().key ?: return
        val timestamp = System.currentTimeMillis()

        val message = Message(
            messageId = messageId,
            message = text,
            senderId = senderId,
            timestamp = timestamp
        )
        val senderPreview = RecentChat(chatRoomId, roomId, receiverId, text, timestamp)
        val receiverPreview = RecentChat(chatRoomId, roomId, senderId, text, timestamp)

        val updates = hashMapOf<String, Any>(
            "chats/$chatRoomId/messages/$messageId" to message,
            "inbox/$senderId/$chatRoomId" to senderPreview,
            "inbox/$receiverId/$chatRoomId" to receiverPreview
        )
        rootRef.updateChildren(updates).await()
    }

    // ─── Inbox ────────────────────────────────────────────

    fun observeInbox(userId: String): Flow<List<RecentChat>> = callbackFlow {
        val ref = database.getReference("inbox").child(userId)
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<RecentChat>()
                for (data in snapshot.children) {
                    data.getValue(RecentChat::class.java)?.let { items.add(it) }
                }
                items.sortByDescending { it.timestamp }
                trySend(items)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
                close()
            }
        })

        awaitClose { ref.removeEventListener(listener) }
    }

    // ─── Favorites ────────────────────────────────────────

    fun observeFavorites(userId: String): Flow<List<Room>> = callbackFlow {
        val ref = database.getReference("Users").child(userId).child("favorites")
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rooms = mutableListOf<Room>()
                for (data in snapshot.children) {
                    data.getValue(Room::class.java)?.let { rooms.add(it) }
                }
                trySend(rooms)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
                close()
            }
        })

        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val snapshot = database.getReference("Users").child(uid).get().await()
            if (!snapshot.exists()) return null
            val username = snapshot.child("username").getValue(String::class.java)
            val email = snapshot.child("email").getValue(String::class.java)
            val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
            val role = snapshot.child("role").getValue(String::class.java)
            UserProfile(username, email, profileImageUrl, role)
        } catch (e: Exception) {
            android.util.Log.w("FirebaseRepository", "getUserProfile failed for $uid: ${e.message}")
            null
        }
    }

    suspend fun addFavorite(userId: String, room: Room) {
        val key = room.id ?: return
        database.getReference("Users").child(userId).child("favorites").child(key).setValue(room).await()
    }

    suspend fun removeFavorite(userId: String, roomId: String) {
        database.getReference("Users").child(userId).child("favorites").child(roomId).removeValue().await()
    }

    suspend fun getUserRole(uid: String): String? {
        return try {
            val snapshot = database.getReference("Users").child(uid).get().await()
            snapshot.child("role").getValue(String::class.java)
        } catch (e: Exception) {
            android.util.Log.w("FirebaseRepository", "getUserRole failed for $uid: ${e.message}")
            null
        }
    }

    // ─── Auth ────────────────────────────────────────────

    suspend fun signUp(name: String, email: String, password: String, role: String): Result<Unit> = runCatching {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val userId = authResult.user?.uid ?: throw Exception("Registration failed")
        val userData = hashMapOf("email" to email, "username" to name, "role" to role)
        database.getReference("Users").child(userId).setValue(userData).await()
    }

    // ─── Image (100% Free Storage: Imgur Free API / WebP) ────────────────

    suspend fun uploadImageToFreeStorage(uri: Uri, contentResolver: android.content.ContentResolver): String? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            // First attempt free Imgur anonymous API upload for clean HTTPS URL
            val imgurUrl = uploadToImgur(uri, contentResolver)
            if (!imgurUrl.isNullOrEmpty()) {
                return@withContext imgurUrl
            }
            // Fallback to local ultra-compressed WebP string
            compressAndEncodeImage(uri, contentResolver)
        }
    }

    private fun uploadToImgur(uri: Uri, contentResolver: android.content.ContentResolver): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val bytes = inputStream.readBytes()
            inputStream.close()

            val base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val url = java.net.URL("https://api.imgur.com/3/image")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Client-ID c66212e3e57f201") // Free Imgur Client-ID
            conn.doOutput = true
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            val os = conn.outputStream
            val postData = "image=" + java.net.URLEncoder.encode(base64Image, "UTF-8")
            os.write(postData.toByteArray())
            os.flush()
            os.close()

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().readText()
                val json = org.json.JSONObject(response)
                if (json.optBoolean("success")) {
                    return json.getJSONObject("data").getString("link")
                }
            }
            null
        } catch (e: Exception) {
            android.util.Log.w("FirebaseRepository", "Free Imgur upload fallback: ${e.message}")
            null
        }
    }

    fun compressAndEncodeImage(uri: Uri, contentResolver: android.content.ContentResolver): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: run {
                inputStream?.close()
                return null
            }
            inputStream?.close()

            val maxDimension = 480.0
            val ratio = Math.min(maxDimension / originalBitmap.width, maxDimension / originalBitmap.height)
            val width = Math.max(1, Math.round(ratio * originalBitmap.width).toInt())
            val height = Math.max(1, Math.round(ratio * originalBitmap.height).toInt())
            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)

            val outputStream = ByteArrayOutputStream()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                resizedBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 75, outputStream)
            } else {
                @Suppress("DEPRECATION")
                resizedBitmap.compress(Bitmap.CompressFormat.WEBP, 75, outputStream)
            }
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            android.util.Log.w("FirebaseRepository", "compressAndEncodeImage failed: ${e.message}")
            null
        }
    }
}

data class UserProfile(
    val username: String?,
    val email: String?,
    val profileImageUrl: String?,
    val role: String? = null
)
