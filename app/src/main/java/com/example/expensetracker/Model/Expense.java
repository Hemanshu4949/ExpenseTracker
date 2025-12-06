package com.example.expensetracker.Model;

public class Expense {
    private String id;
    private double amount;
    private String category;
    private String date;
    private String notes;
    private String paymentMethod;
    private long timestamp;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String type;

    // Required empty public constructor for Firebase
    public Expense() {
    }

    public Expense(String id, double amount, String category, String date, String notes, String type ,String paymentMethod, long timestamp) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.notes = notes;
        this.paymentMethod = paymentMethod;
        this.timestamp = timestamp;
        this.type = type;
    }

    // --- Getters and Setters for all fields ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

