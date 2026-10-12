package com.example.vegitrp.models;

public class Transaction {
    private String id;
    private String walletId;
    private double amount;
    private String type; // "CREDIT" or "DEBIT"
    private String description;
    private long timestamp;

    public Transaction() {
    }

    public Transaction(String id, String walletId, double amount, String type, String description, long timestamp) {
        this.id = id;
        this.walletId = walletId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
