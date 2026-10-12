package com.example.vegitrp.models;

public class Unit {
    private String name;

    public Unit() {
        // Default constructor required for calls to DataSnapshot.getValue(Unit.class)
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
