package com.example.vegitrp.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.List;

public class Order {
    private String key;
    private String buyerId;
    private String product;
    private String quantity;
    private String status;
    private double price;
    private String pinCode;
    private Object timestamp;
    private List<String> interestedFarmers;

    // Delivery Logistics
    private String assignedFarmerId;
    private String assignedDriverId;
    private String pickupAddress;
    private String deliveryAddress;
    private double pickupLatitude;
    private double pickupLongitude;
    private double deliveryLatitude;
    private double deliveryLongitude;
    private String driverStatus;
    private java.util.Map<String, Negotiation> negotiations;

    public Order() {
        // Default constructor required for calls to DataSnapshot.getValue(Order.class)
    }

    public Order(String buyerId, String product, String quantity, String status, double price, String pinCode) {
        this.buyerId = buyerId;
        this.product = product;
        this.quantity = quantity;
        this.status = status;
        this.price = price;
        this.pinCode = pinCode;
        this.timestamp = ServerValue.TIMESTAMP;
    }

    @Exclude
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getInterestedFarmers() {
        return interestedFarmers;
    }

    public void setInterestedFarmers(List<String> interestedFarmers) {
        this.interestedFarmers = interestedFarmers;
    }

    public String getAssignedFarmerId() {
        return assignedFarmerId;
    }

    public void setAssignedFarmerId(String assignedFarmerId) {
        this.assignedFarmerId = assignedFarmerId;
    }

    public String getAssignedDriverId() {
        return assignedDriverId;
    }

    public void setAssignedDriverId(String assignedDriverId) {
        this.assignedDriverId = assignedDriverId;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public double getPickupLatitude() {
        return pickupLatitude;
    }

    public void setPickupLatitude(double pickupLatitude) {
        this.pickupLatitude = pickupLatitude;
    }

    public double getPickupLongitude() {
        return pickupLongitude;
    }

    public void setPickupLongitude(double pickupLongitude) {
        this.pickupLongitude = pickupLongitude;
    }

    public double getDeliveryLatitude() {
        return deliveryLatitude;
    }

    public void setDeliveryLatitude(double deliveryLatitude) {
        this.deliveryLatitude = deliveryLatitude;
    }

    public double getDeliveryLongitude() {
        return deliveryLongitude;
    }

    public void setDeliveryLongitude(double deliveryLongitude) {
        this.deliveryLongitude = deliveryLongitude;
    }

    public String getDriverStatus() {
        return driverStatus;
    }

    public void setDriverStatus(String driverStatus) {
        this.driverStatus = driverStatus;
    }

    public java.util.Map<String, Negotiation> getNegotiations() {
        return negotiations;
    }

    public void setNegotiations(java.util.Map<String, Negotiation> negotiations) {
        this.negotiations = negotiations;
    }
}
