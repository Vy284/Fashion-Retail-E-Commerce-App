package com.example.shoestoreapplication.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.shoestoreapplication.database.DatabaseHelper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserDao {

    private final DatabaseHelper helper;

    public UserDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public long registerUser(String fullName, String email, String phone, String password) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("email", email);
        values.put("phone", phone);
        values.put("password_hash", hashPassword(password));
        return db.insert("Users", null, values);
    }

    /** Trả về user_id, hoặc -1 nếu sai thông tin. */
    public int checkLogin(String emailOrPhone, String password) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT user_id FROM Users WHERE (email = ? OR phone = ?) AND password_hash = ?",
                new String[]{emailOrPhone, emailOrPhone, hashPassword(password)})) {
            return c.moveToFirst() ? c.getInt(0) : -1;
        }
    }

    // Trước đây: isUserExists
    public boolean existsByEmailOrPhone(String email, String phone) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT 1 FROM Users WHERE email = ? OR phone = ? LIMIT 1",
                new String[]{email, phone})) {
            return c.moveToFirst();
        }
    }

    // Trước đây: userExists
    public boolean existsById(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT 1 FROM Users WHERE user_id = ? LIMIT 1",
                new String[]{String.valueOf(userId)})) {
            return c.moveToFirst();
        }
    }

    /** Caller phải tự đóng Cursor. */
    public Cursor getUserById(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Users WHERE user_id = ?", new String[]{String.valueOf(userId)});
    }

    public boolean updateUserProfile(int userId, String fullName, String phone, String avatarUrl) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("phone", phone);
        if (avatarUrl != null) {
            values.put("avatar_url", avatarUrl);
        }
        return db.update("Users", values, "user_id = ?", new String[]{String.valueOf(userId)}) > 0;
    }

    public boolean checkOldPassword(int userId, String oldPassword) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT 1 FROM Users WHERE user_id = ? AND password_hash = ?",
                new String[]{String.valueOf(userId), hashPassword(oldPassword)})) {
            return c.moveToFirst();
        }
    }

    public boolean updatePassword(int userId, String newPassword) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password_hash", hashPassword(newPassword));
        return db.update("Users", values, "user_id = ?", new String[]{String.valueOf(userId)}) > 0;
    }

    // Lưu ý: SHA-256 không salt, chỉ phù hợp đồ án/demo.
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}