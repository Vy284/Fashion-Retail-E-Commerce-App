package com.example.shoestoreapplication.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.shoestoreapplication.database.DatabaseHelper;
import com.example.shoestoreapplication.models.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartDao {

    private final DatabaseHelper helper;

    public CartDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public int getOrCreateCartId(int userId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int cartId = -1;

        try (Cursor c = db.rawQuery("SELECT cart_id FROM Carts WHERE user_id = ?",
                new String[]{String.valueOf(userId)})) {
            if (c.moveToFirst()) {
                cartId = c.getInt(0);
            }
        }

        if (cartId == -1) {
            try {
                ContentValues values = new ContentValues();
                values.put("user_id", userId);
                cartId = (int) db.insertOrThrow("Carts", null, values);
            } catch (SQLException e) {
                Log.e("DB_ERR", "Tạo giỏ thất bại, userId=" + userId, e);
            }
        }
        return cartId;
    }

    public boolean addToCart(int cartId, int variantId, int quantity) {
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            Integer itemId = null;
            int currentQty = 0;
            try (Cursor c = db.rawQuery(
                    "SELECT cart_item_id, quantity FROM Cart_Items WHERE cart_id = ? AND variant_id = ?",
                    new String[]{String.valueOf(cartId), String.valueOf(variantId)})) {
                if (c.moveToFirst()) {
                    itemId = c.getInt(0);
                    currentQty = c.getInt(1);
                }
            }

            ContentValues values = new ContentValues();
            if (itemId != null) {
                values.put("quantity", currentQty + quantity);
                return db.update("Cart_Items", values, "cart_item_id = ?",
                        new String[]{String.valueOf(itemId)}) > 0;
            }
            values.put("cart_id", cartId);
            values.put("variant_id", variantId);
            values.put("quantity", quantity);
            return db.insertOrThrow("Cart_Items", null, values) > 0;
        } catch (SQLException e) {
            Log.e("DB_ERR", "addToCart thất bại: cartId=" + cartId + ", variantId=" + variantId, e);
            return false;
        }
    }

    public List<CartItem> getCartItems(int cartId) {
        List<CartItem> cartItems = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();

        String query = "SELECT ci.cart_item_id, p.name, pv.size, pv.price, ci.quantity, " +
                "(SELECT pi.image_url FROM Product_Images pi " +
                " WHERE pi.product_id = p.product_id " +
                " ORDER BY pi.sort_order, pi.image_id LIMIT 1) AS image_url, " +
                "pv.color, pv.stock_quantity " +
                "FROM Cart_Items ci " +
                "INNER JOIN Product_Variants pv ON ci.variant_id = pv.variant_id " +
                "INNER JOIN Products p ON pv.product_id = p.product_id " +
                "WHERE ci.cart_id = ?";

        try (Cursor c = db.rawQuery(query, new String[]{String.valueOf(cartId)})) {
            while (c.moveToNext()) {
                CartItem item = new CartItem(
                        c.getInt(0), c.getString(1), c.getString(2), c.getInt(3), c.getInt(4));
                item.setImageUrl(c.isNull(5) ? "" : c.getString(5));
                item.setColor(c.isNull(6) ? "" : c.getString(6));
                item.setStockQuantity(c.getInt(7));
                cartItems.add(item);
            }
        }
        return cartItems;
    }

    public void updateCartItemQuantity(int cartItemId, int newQuantity) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("quantity", newQuantity);
        db.update("Cart_Items", values, "cart_item_id = ?", new String[]{String.valueOf(cartItemId)});
    }

    public boolean deleteCartItem(int cartItemId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete("Cart_Items", "cart_item_id = ?",
                new String[]{String.valueOf(cartItemId)}) > 0;
    }
}