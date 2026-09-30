package com.example.shoestoreapplication.models;

public class CartItem {
    private int cartItemId;
    private String productName;
    private String size;
    private int price;
    private int quantity;

    public CartItem(int cartItemId, String productName, String size, int price, int quantity) {
        this.cartItemId = cartItemId;
        this.productName = productName;
        this.size = size;
        this.price = price;
        this.quantity = quantity;
    }

    public int getCartItemId() { return cartItemId; }
    public String getProductName() { return productName; }
    public String getSize() { return size; }
    public int getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}