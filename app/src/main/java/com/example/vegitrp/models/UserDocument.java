package com.example.vegitrp.models;

public class UserDocument {
    private String id;
    private String userId;
    private String documentType;
    private String documentUrl;
    private String status; // Pending, Approved, Rejected
    private long timestamp;

    public UserDocument() {
    }

    public UserDocument(String id, String userId, String documentType, String documentUrl, String status, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.documentType = documentType;
        this.documentUrl = documentUrl;
        this.status = status;
        this.timestamp = timestamp;
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

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
