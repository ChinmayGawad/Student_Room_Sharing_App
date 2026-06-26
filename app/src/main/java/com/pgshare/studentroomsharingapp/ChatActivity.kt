package com.pgshare.studentroomsharingapp

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

class ChatActivity : AppCompatActivity() {
    private var messageEditText: EditText? = null
    private var sendButton: Button? = null
    private var messageListView: ListView? = null
    private var messages: ArrayList<Message?>? = null
    private var messageAdapt: MessageAdapt? = null
    private var messagesRef: DatabaseReference? = null

    private val firebaseAuth: FirebaseAuth? = null

    private var roomId: String? = null // Variable to store the room ID

    // Override onCreate method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        getSupportActionBar()!!.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.C_color)))

        // Initialize views
        messageEditText = findViewById<EditText>(R.id.messageEditText)
        sendButton = findViewById<Button>(R.id.sendButton)
        messageListView = findViewById<ListView>(R.id.messageListView)

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
        messages = ArrayList<Message?>()
        messageAdapt = MessageAdapt(this, messages)
        messageListView!!.setAdapter(messageAdapt)

        // Initialize Firebase database reference for the specific room
        val database = FirebaseDatabase.getInstance()
        messagesRef = database.getReference("messages").child(roomId!!)

        // Set up send button click listener
        sendButton!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                sendMessage()
            }
        })

        // Set up Firebase database listener to fetch messages
        messagesRef!!.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val message = dataSnapshot.getValue<Message?>(Message::class.java)
                messages!!.add(message)
                messageAdapt!!.notifyDataSetChanged()
                Log.d(
                    "temp_debug",
                    "Msg Sent: " + message!!.getMessage() + ":" + message.isSentByUser() + ":" + message.getUsername() + ":" + message.getEmail()
                )
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
        val messageText = messageEditText!!.getText().toString().trim { it <= ' ' }

        if (!messageText.isEmpty()) {
            val userId = FirebaseAuth.getInstance().getCurrentUser()!!.getUid()
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
                    messageEditText!!.setText("")
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        } else {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
        }
    }
}
