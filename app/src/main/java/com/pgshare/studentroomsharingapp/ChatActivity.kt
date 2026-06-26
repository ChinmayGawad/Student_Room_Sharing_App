package com.pgshare.studentroomsharingapp

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pgshare.studentroomsharingapp.Adapter.Message
import com.pgshare.studentroomsharingapp.Adapter.MessageAdapt
import com.pgshare.studentroomsharingapp.Adapter.UserHelper
import com.pgshare.studentroomsharingapp.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {
    private var sendButton : FloatingActionButton? = null
    private var messageListView: ListView? = null
    private var messages: ArrayList<Message>? = null
    private var messageAdapt: MessageAdapt? = null
    private var messagesRef: DatabaseReference? = null

    private val firebaseAuth: FirebaseAuth? = null

    private var roomId: String? = null // Variable to store the room ID

    lateinit var binding : ActivityChatBinding

    // Override onCreate method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.C_color)))

        // Initialize views
        val messageEditText = binding.etMessageInput
        sendButton = binding.fabSend
        val messageListView = binding.recyclerViewChat

        // Check if intent has extras
        val intent = getIntent()
        if (intent != null && intent.hasExtra("roomId")) {
            roomId = intent.getStringExtra("roomId") // Get room ID from intent
            // Initialize other components and set up Firebase database reference
            initializeComponents()
        } else {
            // Handle case where room ID is not provided
            Toast.makeText(this, "Room ID not provided", Toast.LENGTH_SHORT).show()
            finish() // Close the activity
        }
    }

    // Method to initialize other components and set up Firebase database reference
    private fun initializeComponents() {
        // Initialize messages list and adapter
        messages = ArrayList<Message>()
        messageAdapt = MessageAdapt(this, messages!!)
        messageListView!!.adapter = messageAdapt

        // Initialize Firebase database reference for the specific room
        val database = FirebaseDatabase.getInstance()
        messagesRef = database.getReference("messages").child(roomId!!)

        // Set up send button click listener
        sendButton?.setOnClickListener { sendMessage() }

        // Set up Firebase database listener to fetch messages
        messagesRef!!.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val message = dataSnapshot.getValue<Message?>(Message::class.java)
                if (message != null) {
                    messages!!.add(message)
                    messageAdapt!!.notifyDataSetChanged()
                    Log.d(
                        "temp_debug",
                        "Msg Sent: " + message.message + ":" + message.isSentByUser + ":" + message.username + ":" + message.email
                    )
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun sendMessage() {
        val messageText = binding.etMessageInput.text.toString().trim()

        if (!messageText.isEmpty()) {
            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            val database = FirebaseDatabase.getInstance()
            val usersRef = database.getReference("Users").child(userId)

            //            UserHelper user =  database.getReference("Users").child(userId).getReference
            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue<UserHelper?>(UserHelper::class.java)
                    val displayName = user!!.name
                    val email = user.email

                    //                    String displayName = usersRef.child("name").get().toString();
//                    String email = usersRef.child("email").get().toString();
                    val message = Message(messageText, true, displayName, email)
                    messagesRef!!.push()
                        .setValue(message) // Push message to the specific room's messages
                    binding.etMessageInput.setText("")
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        } else {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
        }
    }
}
