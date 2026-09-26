package com.example.shoestoreapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.shoestoreapplication.database.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo DatabaseHelper và ép tạo CSDL
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.getWritableDatabase();
    }
}