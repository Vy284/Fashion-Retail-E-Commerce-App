package com.example.shoestoreapplication.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class Money {
    public static String vnd(int value) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(value) + "đ";
    }
}