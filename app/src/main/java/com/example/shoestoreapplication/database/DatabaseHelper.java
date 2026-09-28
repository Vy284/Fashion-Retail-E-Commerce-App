package com.example.shoestoreapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "shoes_retail.db";
    private static final int DATABASE_VERSION = 1;
    private static final String SQL_FILE = "database/shoes_retail_ecommerce_schema_seed.sql";

    private final Context context;

    // Notifications Table Constants
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
        // Bật hỗ trợ khóa ngoại (FOREIGN KEY) trong SQLite
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        executeSqlScript(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa hết bảng cũ rồi tạo lại, vì script dùng CREATE TABLE sẽ lỗi nếu bảng đã tồn tại
        dropAllTables(db);
        executeSqlScript(db);
    }

    private void dropAllTables(SQLiteDatabase db) {
        // Hoãn kiểm tra khóa ngoại tới lúc commit để xóa bảng theo thứ tự nào cũng được
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
                String trimmedLine = line.replace("\uFEFF", "").trim(); // bỏ BOM nếu có
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
            // Ném lại để không để lại database tạo dở (version đã ghi nhận nhưng thiếu bảng)
            throw new RuntimeException("Không đọc được file SQL: " + SQL_FILE, e);
        }
    }

    private void runStatement(SQLiteDatabase db, String sql) {
        String upper = sql.toUpperCase(Locale.ROOT);
        // execSQL không chạy được câu trả về dòng dữ liệu; Android đã tự bọc transaction
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
            throw e; // để onCreate rollback
        }
    }

    // ==========================================
    // NOTIFICATIONS CRUD METHODS
    // (Không gọi db.close(): SQLiteOpenHelper dùng chung một kết nối, đóng nó sẽ
    //  làm các Cursor đang mở ở UI bị lỗi. Helper tự quản lý việc đóng.)
    // ==========================================

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

    // 2. Lấy danh sách thông báo theo user_id (Cursor do nơi gọi tự đóng)
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
}