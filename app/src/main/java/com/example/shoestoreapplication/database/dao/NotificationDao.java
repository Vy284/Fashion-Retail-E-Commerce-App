package com.example.shoestoreapplication.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.shoestoreapplication.database.DatabaseHelper;

public class NotificationDao {

    public static final String TABLE_NOTIFICATIONS = "Notifications";
    public static final String COLUMN_NOTIFICATION_ID = "notification_id";
    public static final String COLUMN_NOTIF_USER_ID = "user_id";
    public static final String COLUMN_NOTIF_ORDER_ID = "order_id";
    public static final String COLUMN_NOTIF_TITLE = "title";
    public static final String COLUMN_NOTIF_MESSAGE = "message";
    public static final String COLUMN_NOTIF_IS_READ = "is_read";
    public static final String COLUMN_NOTIF_CREATED_AT = "created_at";

    private final DatabaseHelper helper;

    public NotificationDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public long addNotification(int userId, Integer orderId, String title, String message) {
        SQLiteDatabase db = helper.getWritableDatabase();
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

    /** Caller phải tự đóng Cursor. */
    public Cursor getNotificationsByUserId(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NOTIFICATIONS +
                        " WHERE " + COLUMN_NOTIF_USER_ID + " = ?" +
                        " ORDER BY " + COLUMN_NOTIFICATION_ID + " DESC",
                new String[]{String.valueOf(userId)});
    }

    public boolean markNotificationAsRead(int notificationId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIF_IS_READ, 1);
        return db.update(TABLE_NOTIFICATIONS, values, COLUMN_NOTIFICATION_ID + " = ?",
                new String[]{String.valueOf(notificationId)}) > 0;
    }

    public int markAllNotificationsAsRead(int userId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIF_IS_READ, 1);
        return db.update(TABLE_NOTIFICATIONS, values, COLUMN_NOTIF_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});
    }

    public int getUnreadNotificationCount(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NOTIFICATIONS +
                        " WHERE " + COLUMN_NOTIF_USER_ID + " = ? AND " + COLUMN_NOTIF_IS_READ + " = 0",
                new String[]{String.valueOf(userId)})) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    public boolean deleteNotification(int notificationId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(TABLE_NOTIFICATIONS, COLUMN_NOTIFICATION_ID + " = ?",
                new String[]{String.valueOf(notificationId)}) > 0;
    }
}