package com.pgshare.studentroomsharingapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.pgshare.studentroomsharingapp.Adapter.Message
import com.pgshare.studentroomsharingapp.Adapter.MessageAdapter
import com.pgshare.studentroomsharingapp.Adapter.RecentChat
import com.pgshare.studentroomsharingapp.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var databaseReference: DatabaseReference

    private var receiverId: String = ""
    private var senderId: String = ""
    private var chatRoomId: String = ""
    private var roomId : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Assume receiverId is passed via Intent from the Room listing
        receiverId = intent.getStringExtra("RECEIVER_ID") ?: ""
        roomId = intent.getStringExtra("ROOM_ID") ?: "UnknownRoom"
        senderId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val userPair = if (senderId < receiverId) "${senderId}_$receiverId" else "${receiverId}_$senderId"

        // Create a unique room ID by sorting UIDs so it's identical for both users
        chatRoomId = "${roomId}_${userPair}"
        databaseReference = FirebaseDatabase.getInstance().getReference("chats").child(chatRoomId).child("messages")

        setupRecyclerView()
        chatAdapter.setReceiverId(receiverId)
        listenForMessages()

        // --- NEW ADDITION: Fetch and display the user's name ---
        loadReceiverName()

        binding.fabSend.setOnClickListener {
            val messageText = binding.etMessageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            }
        }

        // --- NEW ADDITION: Fixed the back button to prevent memory leaks ---
        binding.btnBack.setOnClickListener {
            finish() // Simply close this screen to go back to the Dashboard
        }
    }

    private fun setupRecyclerView() {
        messageList = ArrayList()
        chatAdapter = MessageAdapter(messageList, senderId)
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }
    }

    private fun listenForMessages() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                Log.d("ChatDebug", "Firebase found ${snapshot.childrenCount} messages")
                for (dataSnapshot in snapshot.children) {
                    val message = dataSnapshot.getValue(Message::class.java)
                    Log.d("ChatDebug", "Message Text: ${message?.message} | Sender: ${message?.senderId}")
                    message?.let { messageList.add(it) }
                }
                chatAdapter.notifyDataSetChanged()

                // Scroll to the latest message
                if (messageList.isNotEmpty()) {
                    binding.recyclerViewChat.scrollToPosition(messageList.size - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle read error (e.g., log it or show a Toast)
                Log.e("ChatDebug", "Firebase Read Error: ${error.message}")
                Toast.makeText(this@ChatActivity,"Firebase Read Error: ${error.message}",Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendMessage(text: String) {
        // 1. SAFETY NET: Check if Android Studio swallowed our Intent data
        if (receiverId.isEmpty() || roomId.isEmpty() || senderId.isEmpty()) {
            Log.e("ChatDebug", "CRITICAL ERROR: Missing IDs! Sender: '$senderId', Receiver: '$receiverId', Room: '$roomId'")
            Toast.makeText(this, "Session expired. Please go back to the Room screen and click Chat again.", Toast.LENGTH_LONG).show()
            return
        }

        val rootRef = FirebaseDatabase.getInstance().reference

        // 2. Generate a unique ID for the new message
        val messageId = rootRef.child("chats").child(chatRoomId).child("messages").push().key ?: return
        val timestamp = System.currentTimeMillis()

        // 3. Create the Message object
        val message = Message(
            messageId = messageId,
            message = text,
            senderId = senderId,
            timestamp = timestamp
        )

        // 4. Create the lightweight Inbox summaries for both users
        val senderInboxPreview = RecentChat(chatRoomId, roomId, receiverId, text, timestamp)
        val receiverInboxPreview = RecentChat(chatRoomId, roomId, senderId, text, timestamp)

        // 5. Map out the multiple database paths (REMOVED LEADING SLASHES)
        val databaseUpdates = hashMapOf<String, Any>()

        databaseUpdates["chats/$chatRoomId/messages/$messageId"] = message
        databaseUpdates["inbox/$senderId/$chatRoomId"] = senderInboxPreview
        databaseUpdates["inbox/$receiverId/$chatRoomId"] = receiverInboxPreview

        Log.d("ChatDebug", "Attempting batch update to paths: ${databaseUpdates.keys}")

        // 6. Execute the batch update
        rootRef.updateChildren(databaseUpdates).addOnSuccessListener {
            binding.etMessageInput.text?.clear()
            Log.d("ChatDebug", "Batch update SUCCESSFUL!")
        }.addOnFailureListener { e ->
            Log.e("ChatDebug", "Batch update FAILED: ${e.message}")
        }
    }

    // --- NEW ADDITION: Function to load the name from Firebase ---
    private fun loadReceiverName() {
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(receiverId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val username = snapshot.child("username").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)

                    val finalName = when {
                        !username.isNullOrEmpty() -> username
                        !email.isNullOrEmpty() -> email.substringBefore("@").replaceFirstChar { it.titlecase() }
                        else -> "Unknown User"
                    }

                    binding.tvChatTitle.text = finalName
                    chatAdapter.setReceiverName(finalName)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatDebug", "Failed to load user name: ${error.message}")
            }
        })
    }
}