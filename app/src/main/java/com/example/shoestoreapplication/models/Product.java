package com.example.shoestoreapplication.models;

public class Product {
    private int id;
    private String name;
    private int price;
    private String brandName;
    private String imageUrl;

    public Product(int id, String name, int price, String brandName, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.brandName = brandName;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getBrandName() { return brandName; }
    public String getImageUrl() { return imageUrl; }
}