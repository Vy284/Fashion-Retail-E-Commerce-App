package com.example.shoestoreapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shoestoreapplication.database.DatabaseHelper;
import com.example.shoestoreapplication.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private ImageButton btnTogglePassword;
    private Button btnLogin, btnGoToRegister;
    private boolean isPasswordVisible = false;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupListeners();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edt_login_email);
        edtPassword = findViewById(R.id.edt_login_password);
        btnTogglePassword = findViewById(R.id.btn_toggle_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoToRegister = findViewById(R.id.btn_go_to_register);
    }

    private void setupListeners() {

        btnGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });


        btnTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {

                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ic_eye_closed);
            } else {
                // Hiện mật khẩu
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ic_eye_open);
            }
            isPasswordVisible = !isPasswordVisible;
            edtPassword.setSelection(edtPassword.getText().length());
        });


        btnLogin.setOnClickListener(v -> handleLogin());
    }


    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    private boolean isValidPhone(String phone) {
        return phone.matches("^0[0-9]{9,10}$");
    }

    private void handleLogin() {
        String emailOrPhone = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();


        if (emailOrPhone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }


        if (!isValidEmail(emailOrPhone) && !isValidPhone(emailOrPhone)) {
            Toast.makeText(this, "Vui lòng nhập đúng định dạng Email hoặc Số điện thoại!", Toast.LENGTH_SHORT).show();
            return;
        }


        int userId = dbHelper.checkLogin(emailOrPhone, password);

        if (userId != -1) {
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            sessionManager.createLoginSession(userId);
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Sai thông tin đăng nhập hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
        }
    }
}