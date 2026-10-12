package com.example.vegitrp.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

public class Listing {
    private String key;
    private String farmerId;
    private String buyerId;
    private String product;
    private String quantity;
    private String status;
    private double price;
    private String assignedDriver;
    private String assignedVehicle;
    private double latitude;
    private double longitude;
    private Object timestamp;

    public Listing() {
        // Default constructor required for calls to DataSnapshot.getValue(Listing.class)
    }

    public Listing(String farmerId, String product, String quantity, String status, double price) {
        this.farmerId = farmerId;
        this.product = product;
        this.quantity = quantity;
        this.status = status;
        this.price = price;
        this.timestamp = ServerValue.TIMESTAMP;
    }

    public Listing(String buyerId, String product, String quantity, String status, double price, boolean isOrder) {
        this.buyerId = buyerId;
        this.product = product;
        this.quantity = quantity;
        this.status = status;
        this.price = price;
        this.timestamp = ServerValue.TIMESTAMP;
    }

    @Exclude
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getAssignedDriver() {
        return assignedDriver;
    }

    public void setAssignedDriver(String assignedDriver) {
        this.assignedDriver = assignedDriver;
    }

    public String getAssignedVehicle() {
        return assignedVehicle;
    }

    public void setAssignedVehicle(String assignedVehicle) {
        this.assignedVehicle = assignedVehicle;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
