package com.pgshare.studentroomsharingapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.pgshare.studentroomsharingapp.Adapter.MessageAdapt;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private EditText messageEditText;
    private Button sendButton;
    private ListView messageListView;
    private ArrayList<Message> messages;
    private MessageAdapt messageAdapt;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        messageListView = findViewById(R.id.messageListView);

        messages = new ArrayList<>();
        messageAdapt = new MessageAdapt(this, messages);
        messageListView.setAdapter(messageAdapt);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (!messageText.isEmpty()) {
            Message message = new Message(messageText, true);
            messages.add(message);
            messageAdapt.notifyDataSetChanged();
            messageEditText.setText("");
        } else {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
        }
    }
}
