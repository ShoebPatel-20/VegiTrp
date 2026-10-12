package com.example.vegitrp.models;

public class Product {
    private String name;

    public Product() {
        // Default constructor required for calls to DataSnapshot.getValue(Product.class)
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
