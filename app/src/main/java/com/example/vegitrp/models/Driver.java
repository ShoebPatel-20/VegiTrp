package com.example.vegitrp.models;

import com.google.firebase.database.Exclude;

public class Driver {
    private String name;
    private String phone;
    @Exclude
    private String key;

    public Driver() {
        // Default constructor required for calls to DataSnapshot.getValue(Driver.class)
    }

    public Driver(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
