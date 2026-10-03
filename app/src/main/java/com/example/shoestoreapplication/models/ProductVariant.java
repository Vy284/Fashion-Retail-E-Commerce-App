package com.example.shoestoreapplication.models;

public class ProductVariant {
    public final int id;
    public final String color;
    public final String size;
    public final int stock;
    public final int price;

    public ProductVariant(int id, String color, String size, int stock, int price) {
        this.id = id;
        this.color = color;
        this.size = size;
        this.stock = stock;
        this.price = price;
    }
}