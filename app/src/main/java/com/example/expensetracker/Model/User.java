package com.example.expensetracker.Model;

public class User {
    private String uid;
    private String name;
    private String email;

    public User() {
        // Required for Firestore
    }

    public User(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
    }

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}