package com.example.shoestoreapplication.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.shoestoreapplication.database.DatabaseHelper;
import com.example.shoestoreapplication.models.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    private final DatabaseHelper helper;

    public OrderDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    /** Kiểm tra + trừ tồn kho + tạo đơn + thông báo + xóa giỏ, tất cả trong 1 transaction. Trả -1 nếu thất bại. */
    public long placeOrder(int userId, int cartId, String name, String phone, String address,
                           String paymentMethod, int total) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        long orderId = -1;

        try {
            // 1. Kiểm tra tồn kho
            try (Cursor c = db.rawQuery(
                    "SELECT p.name, pv.size, pv.stock_quantity, ci.quantity " +
                            "FROM Cart_Items ci " +
                            "JOIN Product_Variants pv ON ci.variant_id = pv.variant_id " +
                            "JOIN Products p ON pv.product_id = p.product_id " +
                            "WHERE ci.cart_id = ? AND ci.quantity > pv.stock_quantity",
                    new String[]{String.valueOf(cartId)})) {
                if (c.moveToFirst()) {
                    Log.w("Checkout_Bug", "Không đủ hàng: " + c.getString(0)
                            + " size " + c.getString(1)
                            + " (còn " + c.getInt(2) + ", cần " + c.getInt(3) + ")");
                    return -1; // finally vẫn chạy endTransaction() -> rollback
                }
            }

            String orderCode = "KKS-" + System.currentTimeMillis() % 1000000;
            String dbPaymentMethod = paymentMethod.equalsIgnoreCase("COD") ? "cod" : "vnpay";

            ContentValues orderValues = new ContentValues();
            orderValues.put("user_id", userId);
            orderValues.put("order_code", orderCode);
            orderValues.put("status", "pending");
            orderValues.put("subtotal", total);
            orderValues.put("shipping_fee", 0);
            orderValues.put("total", total);
            orderValues.put("recipient_name_snapshot", name);
            orderValues.put("phone_snapshot", phone);
            orderValues.put("address_text_snapshot", address);

            orderId = db.insert("Orders", null, orderValues);

            if (orderId != -1) {
                db.execSQL(
                        "INSERT INTO Order_Items (order_id, variant_id, product_name_snapshot, " +
                                "variant_size_snapshot, variant_color_snapshot, quantity, price_snapshot) " +
                                "SELECT ?, ci.variant_id, p.name, pv.size, pv.color, ci.quantity, pv.price " +
                                "FROM Cart_Items ci " +
                                "JOIN Product_Variants pv ON ci.variant_id = pv.variant_id " +
                                "JOIN Products p ON pv.product_id = p.product_id " +
                                "WHERE ci.cart_id = ?",
                        new Object[]{orderId, cartId});

                // 2. Trừ kho theo số lượng trong giỏ
                db.execSQL(
                        "UPDATE Product_Variants SET stock_quantity = stock_quantity - " +
                                "(SELECT ci.quantity FROM Cart_Items ci " +
                                " WHERE ci.cart_id = ? AND ci.variant_id = Product_Variants.variant_id) " +
                                "WHERE variant_id IN (SELECT variant_id FROM Cart_Items WHERE cart_id = ?)",
                        new Object[]{cartId, cartId});

                ContentValues paymentValues = new ContentValues();
                paymentValues.put("order_id", orderId);
                paymentValues.put("method", dbPaymentMethod);
                paymentValues.put("status", "pending");
                db.insert("Payment", null, paymentValues);

                ContentValues notif = new ContentValues();
                notif.put(NotificationDao.COLUMN_NOTIF_USER_ID, userId);
                notif.put(NotificationDao.COLUMN_NOTIF_ORDER_ID, orderId);
                notif.put(NotificationDao.COLUMN_NOTIF_TITLE, "Đặt hàng thành công");
                notif.put(NotificationDao.COLUMN_NOTIF_MESSAGE,
                        "Cảm ơn bạn đã đặt hàng! Đơn hàng #" + orderCode + " của bạn đang được xử lý.");
                notif.put(NotificationDao.COLUMN_NOTIF_IS_READ, 0);
                db.insert(NotificationDao.TABLE_NOTIFICATIONS, null, notif);

                // 3. Xóa giỏ hàng SAU khi đã trừ kho (thứ tự này quan trọng)
                db.delete("Cart_Items", "cart_id = ?", new String[]{String.valueOf(cartId)});
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.e("Checkout_Bug", "Lỗi chi tiết khi đặt hàng: ", e);
            orderId = -1;
        } finally {
            db.endTransaction();
        }
        return orderId;
    }

    public List<Order> getOrderHistory(int userId) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT order_id, created_at, total, status FROM Orders WHERE user_id = ? ORDER BY order_id DESC",
                new String[]{String.valueOf(userId)})) {
            while (c.moveToNext()) {
                orders.add(new Order(c.getInt(0), c.getString(1), c.getInt(2), c.getString(3)));
            }
        }
        return orders;
    }

    /** Cột: order_code, status, total, created_at, recipient_name, phone, address. Caller tự đóng Cursor. */
    public Cursor getOrderSummary(int orderId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        return db.rawQuery(
                "SELECT order_code, status, total, created_at, " +
                        "recipient_name_snapshot, phone_snapshot, address_text_snapshot " +
                        "FROM Orders WHERE order_id = ?",
                new String[]{String.valueOf(orderId)});
    }

    /** Cột: name, size, color, quantity, price. Caller tự đóng Cursor. */
    public Cursor getOrderItems(int orderId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        return db.rawQuery(
                "SELECT product_name_snapshot, variant_size_snapshot, variant_color_snapshot, " +
                        "quantity, price_snapshot FROM Order_Items WHERE order_id = ?",
                new String[]{String.valueOf(orderId)});
    }

    /** Món đầu tiên của đơn: {tên, ảnh, số lượng, số món khác}. */
    public String[] getOrderPreview(int orderId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String name = "";
        String image = "";
        String qty = "0";
        int rows = 0;
        try (Cursor c = db.rawQuery(
                "SELECT oi.product_name_snapshot, oi.quantity, " +
                        "(SELECT pi.image_url FROM Product_Images pi " +
                        " JOIN Product_Variants pv ON pv.product_id = pi.product_id " +
                        " WHERE pv.variant_id = oi.variant_id " +
                        " ORDER BY pi.sort_order, pi.image_id LIMIT 1) " +
                        "FROM Order_Items oi WHERE oi.order_id = ? ORDER BY oi.order_item_id",
                new String[]{String.valueOf(orderId)})) {
            while (c.moveToNext()) {
                if (rows == 0) {
                    name = c.getString(0);
                    qty = String.valueOf(c.getInt(1));
                    image = c.isNull(2) ? "" : c.getString(2);
                }
                rows++;
            }
        }
        return new String[]{name, image, qty, String.valueOf(Math.max(rows - 1, 0))};
    }
}