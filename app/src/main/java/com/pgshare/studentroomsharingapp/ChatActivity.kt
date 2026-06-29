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
import com.pgshare.studentroomsharingapp.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var databaseReference: DatabaseReference

    private var receiverId: String = ""
    private var senderId: String = ""
    private var chatRoomId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Assume receiverId is passed via Intent from the Room listing
        receiverId = intent.getStringExtra("RECEIVER_ID") ?: ""
        val roomId = intent.getStringExtra("ROOM_ID") ?: "UnknownRoom"
        senderId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val userPair = if (senderId < receiverId) "${senderId}_$receiverId" else "${receiverId}_$senderId"

        // Create a unique room ID by sorting UIDs so it's identical for both users
        chatRoomId = "${roomId}_${userPair}"
        databaseReference = FirebaseDatabase.getInstance().getReference("chats").child(chatRoomId).child("messages")

        setupRecyclerView()
        listenForMessages()

        binding.fabSend.setOnClickListener {
            val messageText = binding.etMessageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            }
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
        val messageId = databaseReference.push().key ?: return
        val timestamp = System.currentTimeMillis()
        val message = Message(messageId, text, senderId, timestamp = timestamp)

        databaseReference.child(messageId).setValue(message)
            .addOnSuccessListener {
                binding.etMessageInput.text?.clear()
            }
    }
}