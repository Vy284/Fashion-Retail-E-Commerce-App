package com.example.shoestoreapplication.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "shoes_retail.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_FILE =
            "database/shoes_retail_ecommerce_schema_seed.sql";

    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            executeSqlFile(db);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executeSqlFile(SQLiteDatabase db) throws IOException {
        InputStream inputStream = context.getAssets().open(SQL_FILE);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sqlBuilder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            // Bỏ qua dòng trống và câu lệnh comment
            String trimmedLine = line.trim();
            if (trimmedLine.startsWith("--") || trimmedLine.isEmpty()) {
                continue;
            }
            sqlBuilder.append(line).append("\n");
        }
        reader.close();

        // Tách các câu lệnh SQL theo dấu chấm phẩy ';'
        String[] statements = sqlBuilder.toString().split(";");

        for (String rawStatement : statements) {
            String statement = rawStatement.trim();
            if (statement.isEmpty()) {
                continue;
            }

            // execSQL() KHÔNG hỗ trợ câu lệnh trả về kết quả (SELECT, EXPLAIN...).
            // Bỏ qua để tránh crash "Queries can be performed using
            // SQLiteDatabase query or rawQuery methods only".
            if (isQueryStatement(statement)) {
                Log.w(TAG, "Skipping non-executable statement (SELECT/EXPLAIN): "
                        + preview(statement));
                continue;
            }

            try {
                db.execSQL(statement);
            } catch (Exception e) {
                // Không để 1 câu lệnh lỗi làm crash toàn bộ app khi tạo DB.
                // Log lại để dễ debug, rồi tiếp tục với câu tiếp theo.
                Log.e(TAG, "Failed to execute statement: " + preview(statement), e);
            }
        }
    }

    private boolean isQueryStatement(String statement) {
        String upper = statement.trim().toUpperCase();
        return upper.startsWith("SELECT") || upper.startsWith("EXPLAIN");
    }

    private String preview(String statement) {
        String oneLine = statement.replaceAll("\\s+", " ").trim();
        return oneLine.length() > 80 ? oneLine.substring(0, 80) + "..." : oneLine;
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
        // Chưa có migration
    }
}