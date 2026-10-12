package com.example.vegitrp.models;

import com.google.firebase.database.Exclude;

public class Vehicle {
    private String key;
    private String vehicleName;
    private String vehicleNumber;

    public Vehicle() {
        // Default constructor required for calls to DataSnapshot.getValue(Vehicle.class)
    }

    public Vehicle(String vehicleName, String vehicleNumber) {
        this.vehicleName = vehicleName;
        this.vehicleNumber = vehicleNumber;
    }

    @Exclude
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }
}
