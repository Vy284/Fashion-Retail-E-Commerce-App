package com.example.shoestoreapplication;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shoestoreapplication.database.DatabaseHelper;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName, edtPhone, edtEmail, edtPassword, edtConfirmPassword;
    private ImageButton btnTogglePassword, btnToggleConfirmPassword;
    private Button btnRegister, btnGoToLogin;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupListeners();
    }

    private void initViews() {
        edtFullName = findViewById(R.id.edt_reg_fullname);
        edtPhone = findViewById(R.id.edt_reg_phone);
        edtEmail = findViewById(R.id.edt_reg_email);
        edtPassword = findViewById(R.id.edt_reg_password);
        edtConfirmPassword = findViewById(R.id.edt_reg_confirm_password);

        btnTogglePassword = findViewById(R.id.btn_toggle_reg_password);
        btnToggleConfirmPassword = findViewById(R.id.btn_toggle_reg_confirm_password);

        btnRegister = findViewById(R.id.btn_register);
        btnGoToLogin = findViewById(R.id.btn_go_to_login);
    }

    private void setupListeners() {

        btnGoToLogin.setOnClickListener(v -> finish()); // Đóng activity này, quay lại Login


        btnTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            togglePasswordVisibility(edtPassword, btnTogglePassword, isPasswordVisible);
        });


        btnToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            togglePasswordVisibility(edtConfirmPassword, btnToggleConfirmPassword, isConfirmPasswordVisible);
        });


        btnRegister.setOnClickListener(v -> handleRegister());
    }

    private void togglePasswordVisibility(EditText editText, ImageButton button, boolean isVisible) {
        if (isVisible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            button.setImageResource(R.drawable.ic_eye_open);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            button.setImageResource(R.drawable.ic_eye_closed);
        }
        editText.setSelection(editText.getText().length());
    }


    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^0[0-9]{9,10}$");
    }

    private void handleRegister() {
        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();


        if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }


        if (fullName.length() < 2) {
            Toast.makeText(this, "Họ và tên quá ngắn!", Toast.LENGTH_SHORT).show();
            return;
        }


        if (!isValidPhone(phone)) {
            Toast.makeText(this, "Số điện thoại không hợp lệ (Bắt đầu bằng 0, gồm 10-11 số)!", Toast.LENGTH_SHORT).show();
            return;
        }


        if (!isValidEmail(email)) {
            Toast.makeText(this, "Email không đúng định dạng!", Toast.LENGTH_SHORT).show();
            return;
        }


        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
            return;
        }


        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }


        if (dbHelper.isUserExists(email, phone)) {
            Toast.makeText(this, "Email hoặc số điện thoại đã được đăng ký!", Toast.LENGTH_SHORT).show();
            return;
        }


        long newUserId = dbHelper.registerUser(fullName, email, phone, password);

        if (newUserId != -1) {
            Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Đăng ký thất bại, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        }
    }
}