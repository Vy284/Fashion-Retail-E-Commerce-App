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
import com.example.shoestoreapplication.models.ProductVariant;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "shoes_retail.db";
    private static final int DATABASE_VERSION = 2;
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
                        getFirstImage(cursor.getInt(0))
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
                        cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getString(3),
                        getFirstImage(cursor.getInt(0))
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


    // ĐÃ SỬA: lấy thêm ảnh chính của sản phẩm cho từng món trong giỏ / thanh toán
    public List<CartItem> getCartItems(int cartId) {
        List<CartItem> cartItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT ci.cart_item_id, p.name, pv.size, pv.price, ci.quantity, " +
                "(SELECT pi.image_url FROM Product_Images pi " +
                " WHERE pi.product_id = p.product_id " +
                " ORDER BY pi.sort_order, pi.image_id LIMIT 1) AS image_url " +
                "FROM Cart_Items ci " +
                "INNER JOIN Product_Variants pv ON ci.variant_id = pv.variant_id " +
                "INNER JOIN Products p ON pv.product_id = p.product_id " +
                "WHERE ci.cart_id = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(cartId)})) {
            while (cursor.moveToNext()) {
                CartItem item = new CartItem(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getInt(4)
                );
                item.setImageUrl(cursor.isNull(5) ? "" : cursor.getString(5));
                cartItems.add(item);
            }
        }
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

    // ==========================================================
    // ============ HÀM ĐÃ SỬA: kiểm tra + trừ tồn kho ==========
    // ==========================================================
    public long placeOrder(int userId, int cartId, String name, String phone, String address, String paymentMethod, int total) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        long orderId = -1;

        try {
            // ===== 1. KIỂM TRA TỒN KHO: có món nào mua nhiều hơn số còn lại không =====
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
                String insertItemsQuery = "INSERT INTO Order_Items (order_id, variant_id, product_name_snapshot, variant_size_snapshot, variant_color_snapshot, quantity, price_snapshot) " +
                        "SELECT ?, ci.variant_id, p.name, pv.size, pv.color, ci.quantity, pv.price " +
                        "FROM Cart_Items ci " +
                        "JOIN Product_Variants pv ON ci.variant_id = pv.variant_id " +
                        "JOIN Products p ON pv.product_id = p.product_id " +
                        "WHERE ci.cart_id = ?";
                db.execSQL(insertItemsQuery, new Object[]{orderId, cartId});

                // ===== 2. TRỪ KHO THEO SỐ LƯỢNG TRONG GIỎ =====
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

                // Tạo thông báo đặt hàng
                ContentValues notifValues = new ContentValues();
                notifValues.put(COLUMN_NOTIF_USER_ID, userId);
                notifValues.put(COLUMN_NOTIF_ORDER_ID, orderId);
                notifValues.put(COLUMN_NOTIF_TITLE, "Đặt hàng thành công");
                notifValues.put(COLUMN_NOTIF_MESSAGE,
                        "Cảm ơn bạn đã đặt hàng! Đơn hàng #" + orderCode + " của bạn đang được xử lý.");
                notifValues.put(COLUMN_NOTIF_IS_READ, 0);
                db.insert(TABLE_NOTIFICATIONS, null, notifValues);

                // ===== 3. Xóa giỏ hàng SAU khi đã trừ kho (thứ tự này quan trọng) =====
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

    // ==========================================================
    // ===================== PHẦN THÊM MỚI ======================
    // ==========================================================

    // ---------- Hằng số sắp xếp ----------
    public static final int SORT_NEWEST = 0;
    public static final int SORT_PRICE_ASC = 1;
    public static final int SORT_PRICE_DESC = 2;
    public static final int SORT_NAME_ASC = 3;

    // Đọc Cursor thành List<Product> (cột: 0=product_id, 1=name, 2=base_price, 3=brand_name)
    private List<Product> readProducts(Cursor cursor) {
        List<Product> list = new ArrayList<>();
        try (Cursor c = cursor) {
            while (c.moveToNext()) {
                list.add(new Product(c.getInt(0), c.getString(1), c.getInt(2), c.getString(3),
                        getFirstImage(c.getInt(0))));
            }
        }
        return list;
    }

    private void appendSort(StringBuilder sql, int sortType) {
        switch (sortType) {
            case SORT_PRICE_ASC:
                sql.append(" ORDER BY p.base_price ASC");
                break;
            case SORT_PRICE_DESC:
                sql.append(" ORDER BY p.base_price DESC");
                break;
            case SORT_NAME_ASC:
                sql.append(" ORDER BY p.name COLLATE NOCASE ASC");
                break;
            default:
                sql.append(" ORDER BY p.created_at DESC, p.product_id DESC");
        }
    }

    // ---------- Dùng cho HomeFragment ----------
    // gender = "all" | "men" | "women" | "unisex" | "kids"  (khớp cột Products.gender_type)
    // brands = danh sách TÊN thương hiệu (rỗng/null = không lọc)
    // sort   = "default" | "price_asc" | "price_desc"
    public List<Product> filterProducts(String keyword, String gender, List<String> brands, String sort) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder sql = new StringBuilder(
                "SELECT p.product_id, p.name, p.base_price, b.name AS brand_name " +
                        "FROM Products p INNER JOIN Brands b ON p.brand_id = b.brand_id WHERE 1=1");
        List<String> args = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (p.name LIKE ? OR b.name LIKE ?)");
            String k = "%" + keyword.trim() + "%";
            args.add(k);
            args.add(k);
        }
        if (gender != null && !gender.equals("all")) {
            sql.append(" AND p.gender_type = ?");
            args.add(gender);
        }
        if (brands != null && !brands.isEmpty()) {
            sql.append(" AND b.name IN (");
            for (int i = 0; i < brands.size(); i++) {
                sql.append(i == 0 ? "?" : ",?");
                args.add(brands.get(i));
            }
            sql.append(")");
        }
        if ("price_asc".equals(sort)) {
            sql.append(" ORDER BY p.base_price ASC");
        } else if ("price_desc".equals(sort)) {
            sql.append(" ORDER BY p.base_price DESC");
        } else {
            sql.append(" ORDER BY p.product_id ASC");
        }
        return readProducts(db.rawQuery(sql.toString(), args.toArray(new String[0])));
    }

    // Tên tất cả thương hiệu (cho dialog lọc thương hiệu ở Home)
    public List<String> getAllBrands() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT name FROM Brands ORDER BY name", null)) {
            while (c.moveToNext()) {
                list.add(c.getString(0));
            }
        }
        return list;
    }

    // ---------- Dùng cho màn Category: lọc theo danh mục / thương hiệu / khoảng giá ----------
    // categoryId/brandId/minPrice/maxPrice <= 0 = không lọc
    public List<Product> filterProducts(String keyword, int categoryId, int brandId,
                                        int minPrice, int maxPrice, int sortType) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder sql = new StringBuilder(
                "SELECT p.product_id, p.name, p.base_price, b.name AS brand_name " +
                        "FROM Products p INNER JOIN Brands b ON p.brand_id = b.brand_id WHERE 1=1");
        List<String> args = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (p.name LIKE ? OR b.name LIKE ?)");
            String k = "%" + keyword.trim() + "%";
            args.add(k);
            args.add(k);
        }
        if (categoryId > 0) {
            sql.append(" AND p.category_id = ?");
            args.add(String.valueOf(categoryId));
        }
        if (brandId > 0) {
            sql.append(" AND p.brand_id = ?");
            args.add(String.valueOf(brandId));
        }
        if (minPrice > 0) {
            sql.append(" AND p.base_price >= ?");
            args.add(String.valueOf(minPrice));
        }
        if (maxPrice > 0) {
            sql.append(" AND p.base_price <= ?");
            args.add(String.valueOf(maxPrice));
        }
        appendSort(sql, sortType);
        return readProducts(db.rawQuery(sql.toString(), args.toArray(new String[0])));
    }

    // Danh sách {id, name} để đổ vào dialog/chip lọc
    public List<String[]> getCategories() {
        return readIdName("SELECT category_id, name FROM Categories ORDER BY name");
    }

    public List<String[]> getBrands() {
        return readIdName("SELECT brand_id, name FROM Brands ORDER BY name");
    }

    private List<String[]> readIdName(String sql) {
        List<String[]> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) {
                list.add(new String[]{String.valueOf(c.getInt(0)), c.getString(1)});
            }
        }
        return list;
    }

    // ---------- Wishlist ----------
    public boolean isInWishlist(int userId, int productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT 1 FROM Wishlists WHERE user_id = ? AND product_id = ? LIMIT 1",
                new String[]{String.valueOf(userId), String.valueOf(productId)})) {
            return c.moveToFirst();
        }
    }

    // Trả về true nếu SAU khi bấm thì sản phẩm đang nằm trong wishlist (để đổi icon tim)
    public boolean toggleWishlist(int userId, int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (isInWishlist(userId, productId)) {
            db.delete("Wishlists", "user_id = ? AND product_id = ?",
                    new String[]{String.valueOf(userId), String.valueOf(productId)});
            return false;
        }
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("product_id", productId);
        return db.insert("Wishlists", null, values) != -1;
    }

    public List<Product> getWishlistProducts(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT p.product_id, p.name, p.base_price, b.name AS brand_name " +
                "FROM Wishlists w " +
                "INNER JOIN Products p ON w.product_id = p.product_id " +
                "INNER JOIN Brands b ON p.brand_id = b.brand_id " +
                "WHERE w.user_id = ? ORDER BY w.added_at DESC, w.wishlist_id DESC";
        return readProducts(db.rawQuery(sql, new String[]{String.valueOf(userId)}));
    }

    // ---------- Thông báo: đánh dấu tất cả đã đọc ----------
    public int markAllNotificationsAsRead(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIF_IS_READ, 1);
        return db.update(TABLE_NOTIFICATIONS, values,
                COLUMN_NOTIF_USER_ID + " = ?", new String[]{String.valueOf(userId)});
    }

    // ---------- Chi tiết sản phẩm ----------
    // Tên ảnh của sản phẩm, theo thứ tự sort_order (ảnh đầu = ảnh chính)
    public List<String> getProductImages(int productId) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT image_url FROM Product_Images WHERE product_id = ? ORDER BY sort_order, image_id",
                new String[]{String.valueOf(productId)})) {
            while (c.moveToNext()) {
                list.add(c.getString(0));
            }
        }
        return list;
    }

    // Tất cả biến thể (màu + size + tồn kho) của sản phẩm
    public List<ProductVariant> getVariants(int productId) {
        List<ProductVariant> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT variant_id, color, size, stock_quantity, price FROM Product_Variants " +
                        "WHERE product_id = ? ORDER BY variant_id",
                new String[]{String.valueOf(productId)})) {
            while (c.moveToNext()) {
                list.add(new ProductVariant(c.getInt(0), c.getString(1), c.getString(2),
                        c.getInt(3), c.getInt(4)));
            }
        }
        return list;
    }

    // Thông tin chung của 1 đơn
    public Cursor getOrderSummary(int orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT order_code, status, total, created_at, " +
                        "recipient_name_snapshot, phone_snapshot, address_text_snapshot " +
                        "FROM Orders WHERE order_id = ?",
                new String[]{String.valueOf(orderId)});
    }

    // Các sản phẩm trong đơn
    public Cursor getOrderItems(int orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT product_name_snapshot, variant_size_snapshot, variant_color_snapshot, " +
                        "quantity, price_snapshot FROM Order_Items WHERE order_id = ?",
                new String[]{String.valueOf(orderId)});
    }

    // Tên ảnh đầu tiên (ảnh chính) của sản phẩm
    public String getFirstImage(int productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT image_url FROM Product_Images WHERE product_id = ? " +
                        "ORDER BY sort_order, image_id LIMIT 1",
                new String[]{String.valueOf(productId)})) {
            return c.moveToFirst() ? c.getString(0) : "";
        }
    }

    // Ảnh chính của sản phẩm, tra theo tên (dùng cho giỏ hàng / thanh toán / lịch sử đơn)
    public String getImageByProductName(String productName) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT pi.image_url FROM Product_Images pi " +
                        "JOIN Products p ON pi.product_id = p.product_id " +
                        "WHERE p.name = ? ORDER BY pi.sort_order, pi.image_id LIMIT 1",
                new String[]{productName})) {
            return c.moveToFirst() ? c.getString(0) : "";
        }
    }

    // Món đầu tiên của đơn: {tên, ảnh, số lượng, số món khác} (dùng cho lịch sử đơn hàng)
    public String[] getOrderPreview(int orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String name = "";
        String qty = "0";
        int rows = 0;
        try (Cursor c = db.rawQuery(
                "SELECT product_name_snapshot, quantity FROM Order_Items " +
                        "WHERE order_id = ? ORDER BY order_item_id",
                new String[]{String.valueOf(orderId)})) {
            while (c.moveToNext()) {
                if (rows == 0) {
                    name = c.getString(0);
                    qty = String.valueOf(c.getInt(1));
                }
                rows++;
            }
        }
        String image = name.isEmpty() ? "" : getImageByProductName(name);
        return new String[]{name, image, qty, String.valueOf(Math.max(rows - 1, 0))};
    }
}