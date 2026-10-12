package com.example.vegitrp.models;

import com.google.firebase.database.ServerValue;

public class Negotiation {
    private double price;
    private String quantity;
    private String senderId;
    private String senderName;
    private Object timestamp;

    public Negotiation() {
        // Default constructor required for calls to DataSnapshot.getValue(Negotiation.class)
    }

    public Negotiation(double price, String quantity, String senderId, String senderName) {
        this.price = price;
        this.quantity = quantity;
        this.senderId = senderId;
        this.senderName = senderName;
        this.timestamp = ServerValue.TIMESTAMP;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
