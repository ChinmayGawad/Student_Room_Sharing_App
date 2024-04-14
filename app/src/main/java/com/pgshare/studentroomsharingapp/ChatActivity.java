package com.pgshare.studentroomsharingapp;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pgshare.studentroomsharingapp.Adapter.Message;
import com.pgshare.studentroomsharingapp.Adapter.MessageAdapt;
import com.pgshare.studentroomsharingapp.Adapter.UserHelper;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private EditText messageEditText;
    private Button sendButton;
    private ListView messageListView;
    private ArrayList<Message> messages;
    private MessageAdapt messageAdapt;
    private DatabaseReference messagesRef;

    private FirebaseAuth firebaseAuth;

    private String roomId; // Variable to store the room ID

    // Override onCreate method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.C_color)));

        // Initialize views
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        messageListView = findViewById(R.id.messageListView);

        // Check if intent has extras
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("roomId")) {
            roomId = intent.getStringExtra("roomId"); // Get room ID from intent
            // Initialize other components and set up Firebase database reference
            initializeComponents();
        } else {
            // Handle case where room ID is not provided
            Toast.makeText(this, "Room ID not provided", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
        }
    }

    // Method to initialize other components and set up Firebase database reference
    private void initializeComponents() {
        // Initialize messages list and adapter
        messages = new ArrayList<>();
        messageAdapt = new MessageAdapt(this, messages);
        messageListView.setAdapter(messageAdapt);

        // Initialize Firebase database reference for the specific room
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        messagesRef = database.getReference("messages").child(roomId);

        // Set up send button click listener
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // Set up Firebase database listener to fetch messages
        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messages.add(message);
                messageAdapt.notifyDataSetChanged();
                Log.d("temp_debug", "Msg Sent: " + message.getMessage() + ":" + message.isSentByUser() + ":" + message.getUsername() + ":" + message.getEmail());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();

        if (!messageText.isEmpty()) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = database.getReference("Users").child(userId);

//            UserHelper user =  database.getReference("Users").child(userId).getReference

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    UserHelper user = snapshot.getValue(UserHelper.class);
                    String displayName = user.getName();
                    String email = user.getEmail();
//                    String displayName = usersRef.child("name").get().toString();
//                    String email = usersRef.child("email").get().toString();

                    Message message = new Message(messageText, true, displayName, email);
                    messagesRef.push().setValue(message); // Push message to the specific room's messages
                    messageEditText.setText("");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        } else {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
        }
    }
}
