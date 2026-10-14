package com.example.shoestoreapplication.models;

public class NotificationItem {
    public final int id;
    public final String title;
    public final String message;
    public final String time;
    public final boolean read;

    public NotificationItem(int id, String title, String message, String time, boolean read) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.time = time;
        this.read = read;
    }
}