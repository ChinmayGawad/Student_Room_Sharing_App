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
        val id = ref.push().key ?: throw Exception("Failed to generate room key")
        room.id = id
        ref.child(id).setValue(room).await()
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

    // ─── Image ────────────────────────────────────────────

    fun compressAndEncodeImage(uri: Uri, contentResolver: android.content.ContentResolver): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val ratio = Math.min(600.0 / originalBitmap.width, 600.0 / originalBitmap.height)
            val width = Math.round(ratio * originalBitmap.width).toInt()
            val height = Math.round(ratio * originalBitmap.height).toInt()
            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)

            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
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
