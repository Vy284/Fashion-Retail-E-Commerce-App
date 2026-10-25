package com.example.shoestoreapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shoestoreapplication.database.DatabaseHelper;
import com.example.shoestoreapplication.database.dao.UserDao;
import com.example.shoestoreapplication.utils.SessionManager;

public class WelcomeActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            // Khởi tạo DB rồi kiểm tra user trong session còn tồn tại không
            DatabaseHelper.getInstance(this).getWritableDatabase();
            UserDao userDao = new UserDao(this);

            if (userDao.existsById(sessionManager.getUserId())) {
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finish();
                return;
            } else {
                // Session cũ không hợp lệ -> xóa và ở lại màn Welcome
                sessionManager.logout();
            }
        }

        setContentView(R.layout.activity_welcome);

        Button btnGetStarted = findViewById(R.id.btn_get_started);
        Button btnWelcomeLogin = findViewById(R.id.btn_welcome_login);

        btnGetStarted.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class));
        });

        btnWelcomeLogin.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        });
    }
}