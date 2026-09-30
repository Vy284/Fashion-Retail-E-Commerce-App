package com.example.shoestoreapplication.models;

public class Order {
    private int orderId;
    private String date;
    private int totalAmount;
    private String status;

    public Order(int orderId, String date, int totalAmount, String status) {
        this.orderId = orderId;
        this.date = date;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public int getOrderId() { return orderId; }
    public String getDate() { return date; }
    public int getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
}