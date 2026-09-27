package com.example.shoestoreapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;

public class ProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ShapeableImageView imgAvatar;
    private EditText edtFullname, edtPhone, edtOldPassword, edtNewPassword;
    private TextView tvHeaderName;
    private Button btnSave;

    // Bộ chọn ảnh từ thư viện thiết bị
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        imgAvatar.setImageURI(selectedImageUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Ánh xạ View
        btnBack = findViewById(R.id.btn_back);
        imgAvatar = findViewById(R.id.img_profile_avatar);
        edtFullname = findViewById(R.id.edt_fullname);
        edtPhone = findViewById(R.id.edt_phone);
        edtOldPassword = findViewById(R.id.edt_old_password);
        edtNewPassword = findViewById(R.id.edt_new_password);
        tvHeaderName = findViewById(R.id.tv_header_name);
        btnSave = findViewById(R.id.btn_save);

        // 1. Click Nút Back -> Quay lại
        btnBack.setOnClickListener(v -> finish());

        // 2. Click Avatar -> Đổi ảnh từ máy
        imgAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // 3. Click Lưu Thay Đổi
        btnSave.setOnClickListener(v -> {
            String newName = edtFullname.getText().toString().trim();
            if (!newName.isEmpty()) {
                tvHeaderName.setText(newName);
            }
            Toast.makeText(this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
        });
    }
}