package com.example.shoestoreapplication.repository;

import android.content.Context;
import android.database.Cursor;

import com.example.shoestoreapplication.database.DatabaseHelper;
import com.example.shoestoreapplication.models.CartItem;
import com.example.shoestoreapplication.models.NotificationItem;
import com.example.shoestoreapplication.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShoeRepository {
    private static volatile ShoeRepository instance;
    private final DatabaseHelper db;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private ShoeRepository(Context context) {
        db = new DatabaseHelper(context.getApplicationContext());
    }

    public static ShoeRepository get(Context context) {
        if (instance == null) {
            synchronized (ShoeRepository.class) {
                if (instance == null) instance = new ShoeRepository(context);
            }
        }
        return instance;
    }

    /** Chạy một tác vụ DB trên luồng nền. */
    public void run(Runnable task) { io.execute(task); }

    // ----- Sản phẩm / wishlist -----
    public List<Product> filterProducts(String gender, List<String> brands, String sort) {
        return db.filterProducts("", gender, brands, sort);
    }
    public boolean isInWishlist(int userId, int productId) { return db.isInWishlist(userId, productId); }
    public boolean toggleWishlist(int userId, int productId) { return db.toggleWishlist(userId, productId); }

    // ----- Giỏ hàng -----
    public int getOrCreateCartId(int userId) { return db.getOrCreateCartId(userId); }
    public List<CartItem> getCartItems(int cartId) { return db.getCartItems(cartId); }
    public void updateCartItemQuantity(int cartItemId, int qty) { db.updateCartItemQuantity(cartItemId, qty); }
    public boolean deleteCartItem(int cartItemId) { return db.deleteCartItem(cartItemId); }

    // ----- Thông báo -----
    public List<NotificationItem> getNotifications(int userId) {
        List<NotificationItem> list = new ArrayList<>();
        try (Cursor c = db.getNotificationsByUserId(userId)) {
            while (c.moveToNext()) {
                list.add(new NotificationItem(
                        c.getInt(c.getColumnIndexOrThrow("notification_id")),
                        c.getString(c.getColumnIndexOrThrow("title")),
                        c.getString(c.getColumnIndexOrThrow("message")),
                        c.getString(c.getColumnIndexOrThrow("created_at")),
                        c.getInt(c.getColumnIndexOrThrow("is_read")) == 1));
            }
        }
        return list;
    }
    public void markNotificationRead(int id) { db.markNotificationAsRead(id); }
    public void markAllNotificationsRead(int userId) { db.markAllNotificationsAsRead(userId); }
}