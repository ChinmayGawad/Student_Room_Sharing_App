package com.pgshare.studentroomsharingapp;

import java.util.Date;

public class Message {
    private String message;
    private boolean sentByUser;
    private String timestamp;

    public Message(String message, boolean sentByUser) {
        this.message = message;
        this.sentByUser = sentByUser;
        // Set timestamp when the message is created
        this.timestamp = getTimestamp(); // Example timestamp, you can use your own logic to set the timestamp
    }

    public String getMessage() {
        return message;
    }

    public boolean isSentByUser() {
        return sentByUser;
    }

    public String getTimestamp() {
        return timestamp != null ? timestamp : "";
    }
}
