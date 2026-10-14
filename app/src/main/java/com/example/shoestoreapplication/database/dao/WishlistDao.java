package com.example.shoestoreapplication.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.shoestoreapplication.database.DatabaseHelper;
import com.example.shoestoreapplication.models.Product;

import java.util.List;

public class WishlistDao {

    private final DatabaseHelper helper;

    public WishlistDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public boolean isInWishlist(int userId, int productId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT 1 FROM Wishlists WHERE user_id = ? AND product_id = ? LIMIT 1",
                new String[]{String.valueOf(userId), String.valueOf(productId)})) {
            return c.moveToFirst();
        }
    }

    /** Trả về true nếu SAU khi bấm thì sản phẩm đang nằm trong wishlist (để đổi icon tim). */
    public boolean toggleWishlist(int userId, int productId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            if (isInWishlist(userId, productId)) {
                db.delete("Wishlists", "user_id = ? AND product_id = ?",
                        new String[]{String.valueOf(userId), String.valueOf(productId)});
                return false;
            }
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("product_id", productId);
            return db.insertOrThrow("Wishlists", null, values) != -1;
        } catch (SQLException e) {
            Log.e("DB_ERR", "toggleWishlist thất bại: userId=" + userId + ", productId=" + productId, e);
            return false;
        }
    }

    public List<Product> getWishlistProducts(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT " + ProductDao.PRODUCT_COLUMNS +
                "FROM Wishlists w " +
                "INNER JOIN Products p ON w.product_id = p.product_id " +
                "INNER JOIN Brands b ON p.brand_id = b.brand_id " +
                "WHERE w.user_id = ? ORDER BY w.added_at DESC, w.wishlist_id DESC";
        return ProductDao.readProducts(db.rawQuery(sql, new String[]{String.valueOf(userId)}));
    }
}