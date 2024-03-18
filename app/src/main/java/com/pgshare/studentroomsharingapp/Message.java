package com.pgshare.studentroomsharingapp;
import java.io.Serializable;

public class Message implements Serializable {
    private String message;
    private boolean sentByUser; // Indicates whether the message was sent by the user or received
    // Add other fields like timestamp if needed

    // Empty constructor required for Firebase deserialization
    public Message() {
    }

    public Message(String message, boolean sentByUser) {
        this.message = message;
        this.sentByUser = sentByUser;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSentByUser() {
        return sentByUser;
    }
}
