package com.example.vegitrp.models;

import com.google.firebase.database.Exclude;

public class Review {
    private String id;
    private String orderId;
    private String reviewerId;
    private String driverId;
    private float ratingValue;
    private String comment;
    private long timestamp;

    public Review() {
        // Default constructor required for calls to DataSnapshot.getValue(Review.class)
    }

    public Review(String id, String orderId, String reviewerId, String driverId, float ratingValue, String comment, long timestamp) {
        this.id = id;
        this.orderId = orderId;
        this.reviewerId = reviewerId;
        this.driverId = driverId;
        this.ratingValue = ratingValue;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public float getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(float ratingValue) {
        this.ratingValue = ratingValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
