package com.example.shoestoreapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.shoestoreapplication.models.CartItem;
import com.example.shoestoreapplication.models.Order;
import com.example.shoestoreapplication.models.Product;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "shoes_retail.db";
    private static final int DATABASE_VERSION = 1;
    private static final String SQL_FILE = "database/shoes_retail_ecommerce_schema_seed.sql";

    private final Context context;

    public static final String TABLE_NOTIFICATIONS = "Notifications";
    public static final String COLUMN_NOTIFICATION_ID = "notification_id";
    public static final String COLUMN_NOTIF_USER_ID = "user_id";
    public static final String COLUMN_NOTIF_ORDER_ID = "order_id";
    public static final String COLUMN_NOTIF_TITLE = "title";
    public static final String COLUMN_NOTIF_MESSAGE = "message";
    public static final String COLUMN_NOTIF_IS_READ = "is_read";
    public static final String COLUMN_NOTIF_CREATED_AT = "created_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        executeSqlScript(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropAllTables(db);
        executeSqlScript(db);
    }

    private void dropAllTables(SQLiteDatabase db) {
        db.execSQL("PRAGMA defer_foreign_keys = ON");
        List<String> tables = new ArrayList<>();
        try (Cursor c = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type = 'table' "
                        + "AND name NOT LIKE 'sqlite_%' AND name != 'android_metadata'", null)) {
            while (c.moveToNext()) {
                tables.add(c.getString(0));
            }
        }
        for (String table : tables) {
            db.execSQL("DROP TABLE IF EXISTS \"" + table + "\"");
        }
    }

    private void executeSqlScript(SQLiteDatabase db) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open(SQL_FILE), StandardCharsets.UTF_8))) {

            StringBuilder statement = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.replace("\uFEFF", "").trim();
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("--")) {
                    continue;
                }
                statement.append(line).append(" ");
                if (trimmedLine.endsWith(";")) {
                    runStatement(db, statement.toString().replace("\uFEFF", "").trim());
                    statement.setLength(0);
                }
            }
            Log.d(TAG, "Database schema and seed executed successfully.");
        } catch (IOException e) {
            throw new RuntimeException("Không đọc được file SQL: " + SQL_FILE, e);
        }
    }

    private void runStatement(SQLiteDatabase db, String sql) {
        String upper = sql.toUpperCase(Locale.ROOT);
        if (upper.startsWith("SELECT") || upper.startsWith("PRAGMA")
                || upper.startsWith("BEGIN") || upper.startsWith("COMMIT")
                || upper.startsWith("END")) {
            Log.w(TAG, "Bỏ qua câu lệnh không chạy được bằng execSQL: " + sql);
            return;
        }
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            Log.e(TAG, "Lỗi ở câu lệnh: " + sql, e);
            throw e;
        }
    }

    // 1. Thêm thông báo mới
    public long addNotification(int userId, Integer orderId, String title, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIF_USER_ID, userId);
        if (orderId != null) {
            values.put(COLUMN_NOTIF_ORDER_ID, orderId);
        } else {
            values.putNull(COLUMN_NOTIF_ORDER_ID);
        }
        values.put(COLUMN_NOTIF_TITLE, title);
        values.put(COLUMN_NOTIF_MESSAGE, message);
        values.put(COLUMN_NOTIF_IS_READ, 0);

        return db.insert(TABLE_NOTIFICATIONS, null, values);
    }

    // 2. Lấy danh sách thông báo theo user_id
    public Cursor getNotificationsByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NOTIFICATIONS +
                " WHERE " + COLUMN_NOTIF_USER_ID + " = ?" +
                " ORDER BY " + COLUMN_NOTIFICATION_ID + " DESC";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    // 3. Đánh dấu thông báo đã đọc
    public boolean markNotificationAsRead(int notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIF_IS_READ, 1);

        int rows = db.update(TABLE_NOTIFICATIONS, values,
                COLUMN_NOTIFICATION_ID + " = ?",
                new String[]{String.valueOf(notificationId)});
        return rows > 0;
    }

    // 4. Lấy số lượng thông báo chưa đọc
    public int getUnreadNotificationCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_NOTIFICATIONS +
                " WHERE " + COLUMN_NOTIF_USER_ID + " = ? AND " + COLUMN_NOTIF_IS_READ + " = 0";
        int count = 0;
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)})) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        }
        return count;
    }

    // 5. Xóa 1 thông báo
    public boolean deleteNotification(int notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_NOTIFICATIONS,
                COLUMN_NOTIFICATION_ID + " = ?",
                new String[]{String.valueOf(notificationId)});
        return rows > 0;
    }

    // 6. register
    public long registerUser(String fullName, String email, String phone, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("email", email);
        values.put("phone", phone);
        values.put("password_hash", hashPassword(password));

        return db.insert("Users", null, values);
    }

    // 7. check valid user
    public int checkLogin(String emailOrPhone, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        int userId = -1;

        String query = "SELECT user_id FROM Users WHERE (email = ? OR phone = ?) AND password_hash = ?";
        Cursor cursor = db.rawQuery(query, new String[]{emailOrPhone, emailOrPhone, hashPassword(password)});

        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        return userId;
    }

    // 8. check user exist
    public boolean isUserExists(String email, String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT user_id FROM Users WHERE email = ? OR phone = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, phone});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Private methods
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    // get user
    public Cursor getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Users WHERE user_id = ?", new String[]{String.valueOf(userId)});
    }

    // update user profile
    public boolean updateUserProfile(int userId, String fullName, String phone, String avatarUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("phone", phone);
        if (avatarUrl != null) {
            values.put("avatar_url", avatarUrl);
        }

        int rows = db.update("Users", values, "user_id = ?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    // check old pass word
    public boolean checkOldPassword(int userId, String oldPassword) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT user_id FROM Users WHERE user_id = ? AND password_hash = ?",
                new String[]{String.valueOf(userId), hashPassword(oldPassword)});
        boolean isCorrect = cursor.getCount() > 0;
        cursor.close();
        return isCorrect;
    }

    // update password
    public boolean updatePassword(int userId, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password_hash", hashPassword(newPassword));
        int rows = db.update("Users", values, "user_id = ?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    // get list featured product
    public List<Product> getFeaturedProducts() {
        List<Product> productList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT p.product_id, p.name, p.base_price, b.name AS brand_name " +
                "FROM Products p " +
                "INNER JOIN Brands b ON p.brand_id = b.brand_id " +
                "LIMIT 10";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Product product = new Product(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getString(3),
                        ""
                );
                productList.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return productList;
    }

    public List<Product> searchProducts(String keyword) {
        List<Product> productList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT p.product_id, p.name, p.base_price, b.name AS brand_name " +
                "FROM Products p " +
                "INNER JOIN Brands b ON p.brand_id = b.brand_id " +
                "WHERE p.name LIKE ? OR b.name LIKE ?";

        Cursor cursor = db.rawQuery(query, new String[]{"%" + keyword + "%", "%" + keyword + "%"});
        if (cursor.moveToFirst()) {
            do {
                productList.add(new Product(
                        cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getString(3), ""
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return productList;
    }


    public int getOrCreateCartId(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int cartId = -1;

        Cursor cursor = db.rawQuery("SELECT cart_id FROM Carts WHERE user_id = ?", new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            cartId = cursor.getInt(0);
        }
        cursor.close();

        if (cartId == -1) {
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            cartId = (int) db.insert("Carts", null, values);
        }
        return cartId;
    }

    public boolean addToCart(int cartId, int variantId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();


        Cursor cursor = db.rawQuery("SELECT cart_item_id, quantity FROM Cart_Items WHERE cart_id = ? AND variant_id = ?",
                new String[]{String.valueOf(cartId), String.valueOf(variantId)});

        if (cursor.moveToFirst()) {

            int currentQty = cursor.getInt(1);
            ContentValues values = new ContentValues();
            values.put("quantity", currentQty + quantity);
            int rows = db.update("Cart_Items", values, "cart_item_id = ?", new String[]{String.valueOf(cursor.getInt(0))});
            cursor.close();
            return rows > 0;
        } else {

            cursor.close();
            ContentValues values = new ContentValues();
            values.put("cart_id", cartId);
            values.put("variant_id", variantId);
            values.put("quantity", quantity);
            return db.insert("Cart_Items", null, values) > 0;
        }
    }


    public List<CartItem> getCartItems(int cartId) {
        List<CartItem> cartItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT ci.cart_item_id, p.name, pv.size, pv.price, ci.quantity " +
                "FROM Cart_Items ci " +
                "INNER JOIN Product_Variants pv ON ci.variant_id = pv.variant_id " +
                "INNER JOIN Products p ON pv.product_id = p.product_id " +
                "WHERE ci.cart_id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(cartId)});
        if (cursor.moveToFirst()) {
            do {
                cartItems.add(new CartItem(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getInt(4)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return cartItems;
    }


    public void updateCartItemQuantity(int cartItemId, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("quantity", newQuantity);
        db.update("Cart_Items", values, "cart_item_id = ?", new String[]{String.valueOf(cartItemId)});
    }
    public Cursor getProductDetail(int productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT p.name, p.base_price, p.description, b.name AS brand_name " +
                "FROM Products p " +
                "INNER JOIN Brands b ON p.brand_id = b.brand_id " +
                "WHERE p.product_id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(productId)});
    }


    public int getDefaultVariantId(int productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int variantId = -1;
        Cursor cursor = db.rawQuery("SELECT variant_id FROM Product_Variants WHERE product_id = ? LIMIT 1",
                new String[]{String.valueOf(productId)});
        if (cursor.moveToFirst()) {
            variantId = cursor.getInt(0);
        }
        cursor.close();
        return variantId;
    }
    public long placeOrder(int userId, int cartId, String name, String phone, String address, String paymentMethod, int total) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        long orderId = -1;

        try {
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
                String insertItemsQuery = "INSERT INTO Order_Items (order_id, variant_id, product_name_snapshot, variant_size_snapshot, variant_color_snapshot, quantity, price_snapshot) " +
                        "SELECT ?, ci.variant_id, p.name, pv.size, pv.color, ci.quantity, pv.price " +
                        "FROM Cart_Items ci " +
                        "JOIN Product_Variants pv ON ci.variant_id = pv.variant_id " +
                        "JOIN Products p ON pv.product_id = p.product_id " +
                        "WHERE ci.cart_id = ?";
                db.execSQL(insertItemsQuery, new Object[]{orderId, cartId});

                ContentValues paymentValues = new ContentValues();
                paymentValues.put("order_id", orderId);
                paymentValues.put("method", dbPaymentMethod);
                paymentValues.put("status", "pending");
                db.insert("Payment", null, paymentValues);

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
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT order_id, created_at, total, status FROM Orders WHERE user_id = ? ORDER BY order_id DESC", new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                orders.add(new Order(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getString(3)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }
}