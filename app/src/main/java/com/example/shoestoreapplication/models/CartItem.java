package com.example.shoestoreapplication.models;

public class CartItem {
    private final int cartItemId;
    private final String productName;
    private final String size;
    private String color = "";
    private final int price;
    private int quantity;
    private String imageUrl = "";
    private int stockQuantity;

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

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color == null ? "" : color; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
}