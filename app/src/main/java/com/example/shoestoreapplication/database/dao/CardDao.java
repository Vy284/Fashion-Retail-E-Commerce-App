package com.example.shoestoreapplication.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.shoestoreapplication.database.DatabaseHelper;

public class CardDao {

    public static final String TABLE_CARDS = "Cards";
    public static final String COLUMN_CARD_ID = "card_id";
    public static final String COLUMN_CARD_USER_ID = "user_id";
    public static final String COLUMN_CARD_HOLDER_NAME = "card_holder_name";
    public static final String COLUMN_CARD_LAST_FOUR = "last_four_digits";
    public static final String COLUMN_CARD_EXPIRY = "expiry_date";
    public static final String COLUMN_CARD_CVV = "cvv";

    private final DatabaseHelper helper;

    public CardDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public boolean addCard(int userId, String holderName, String lastFour, String expiry, String cvv) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CARD_USER_ID, userId);
        values.put(COLUMN_CARD_HOLDER_NAME, holderName);
        values.put(COLUMN_CARD_LAST_FOUR, lastFour);
        values.put(COLUMN_CARD_EXPIRY, expiry);
        values.put(COLUMN_CARD_CVV, cvv); // Chỉ demo: ứng dụng thật không được lưu CVV
        return db.insert(TABLE_CARDS, null, values) != -1;
    }

    /** Caller phải tự đóng Cursor. */
    public Cursor getCardsByUserId(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_CARDS +
                " WHERE " + COLUMN_CARD_USER_ID + " = ?", new String[]{String.valueOf(userId)});
    }

    public boolean deleteCard(int cardId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(TABLE_CARDS, COLUMN_CARD_ID + " = ?",
                new String[]{String.valueOf(cardId)}) > 0;
    }
}