package com.pgshare.studentroomsharingapp.Adapter;

import com.google.firebase.auth.FirebaseAuth;

public class Message {
    private String message;
    private boolean sentByUser;
    private String timestamp;

    private String username;

    private String email;

    private String roomId;

    public Message() {
        // Default constructor required for Firebase
    }


    public Message(String message, boolean sentByUser, String username, String email) {
        this.message = message;
        this.sentByUser = sentByUser;
        // Set timestamp when the message is created
        this.timestamp = getTimestamp(); // Example timestamp, you can use your own logic to set the timestamp
        this.email = email;
        this.username = username;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSentByUser() {
        return sentByUser;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

}
