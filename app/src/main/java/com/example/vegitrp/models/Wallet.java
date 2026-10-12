package com.example.vegitrp.models;

public class Wallet {
    private String id;
    private String userId;
    private double balance;

    public Wallet() {
    }

    public Wallet(String id, String userId, double balance) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
