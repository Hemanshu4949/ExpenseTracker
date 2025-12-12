package com.example.expensetracker.Model;

import com.google.firebase.Timestamp;

public class FriendRequest {
    private String senderId;
    private String senderName;
    private String senderEmail;
    private String status; // "pending", "accepted", "rejected"
    private Timestamp timestamp;


    public FriendRequest() {
    }

    public FriendRequest(String senderId, String senderName, String senderEmail, String status, Timestamp timestamp) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getSenderEmail() { return senderEmail; }
    public String getStatus() { return status; }
    public Timestamp getTimestamp() { return timestamp; }
}
