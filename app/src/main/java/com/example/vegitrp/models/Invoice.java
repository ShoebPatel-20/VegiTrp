package com.example.vegitrp.models;

public class Invoice {
    private String invoiceId;
    private String orderId;
    private long timestamp;
    private String buyerName;
    private String sellerName;
    private String driverName;
    private String productName;
    private String quantity;
    private double pricePerUnit;
    private double negotiatedPrice;
    private double totalAmount;
    private String deliveryAddress;
    private String paymentStatus;
    private String orderStatus;

    public Invoice() {
    }

    public Invoice(String invoiceId, String orderId, long timestamp, String buyerName, String sellerName, String driverName, String productName, String quantity, double pricePerUnit, double negotiatedPrice, double totalAmount, String deliveryAddress, String paymentStatus, String orderStatus) {
        this.invoiceId = invoiceId;
        this.orderId = orderId;
        this.timestamp = timestamp;
        this.buyerName = buyerName;
        this.sellerName = sellerName;
        this.driverName = driverName;
        this.productName = productName;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.negotiatedPrice = negotiatedPrice;
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.paymentStatus = paymentStatus;
        this.orderStatus = orderStatus;
    }

    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public double getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public double getNegotiatedPrice() { return negotiatedPrice; }
    public void setNegotiatedPrice(double negotiatedPrice) { this.negotiatedPrice = negotiatedPrice; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
}
