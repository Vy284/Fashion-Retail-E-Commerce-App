package com.example.shoestoreapplication.database;

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
    private static final int DATABASE_VERSION = 4;
    private static final String SQL_FILE = "database/shoes_retail_ecommerce_schema_seed.sql";

    private static DatabaseHelper instance;

    private final Context context;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    // Giữ public để code cũ (new DatabaseHelper(ctx)) vẫn chạy; nên dần chuyển sang getInstance()
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    // Không bật khóa ngoại ở đây vì onCreate/onUpgrade chạy DROP/CREATE nhiều bảng
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
    }

    // Bật khóa ngoại sau khi tạo/nâng cấp DB xong
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.setForeignKeyConstraintsEnabled(true);
        }
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
}