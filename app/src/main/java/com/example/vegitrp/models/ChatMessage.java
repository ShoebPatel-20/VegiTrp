package com.example.vegitrp.models;

public class ChatMessage {
    private String id;
    private String senderId;
    private String receiverId;
    private String orderId;
    private String message;
    private long timestamp;

    public ChatMessage() {
    }

    public ChatMessage(String id, String senderId, String receiverId, String orderId, String message, long timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.orderId = orderId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
