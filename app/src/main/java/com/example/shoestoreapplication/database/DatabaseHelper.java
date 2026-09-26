package com.example.shoestoreapplication.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DatabaseHelper extends SQLiteOpenHelper {

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
        InputStream inputStream = context.getAssets().open("database/shoes_retail_ecommerce_schema_seed.sql");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sqlBuilder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            //            // Bỏ qua dòng trống và câu lệnh comment
            if (line.trim().startsWith("--") || line.trim().isEmpty()) {
                continue;
            }
            sqlBuilder.append(line).append("\n");
        }
        reader.close();

        // Tách các câu lệnh SQL theo dấu chấm phẩy ';'
        String[] statements = sqlBuilder.toString().split(";");

        for (String statement : statements) {
            statement = statement.trim();
            if (!statement.isEmpty()) {
                db.execSQL(statement);
            }
        }
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