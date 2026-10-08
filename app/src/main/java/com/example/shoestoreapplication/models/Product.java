package com.example.shoestoreapplication.models;

public class Product {
    private final int id;
    private final String name;
    private final int price;
    private final String brand;
    private final String imageUrl;

    public Product(int id, String name, int price, String brand, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.brand = brand;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getBrand() { return brand; }
    public String getImageUrl() { return imageUrl; }
}