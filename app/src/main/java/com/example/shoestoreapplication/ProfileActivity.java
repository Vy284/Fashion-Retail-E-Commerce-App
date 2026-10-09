package com.example.shoestoreapplication;

import android.content.Intent;
import android.database.Cursor;
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

import com.example.shoestoreapplication.database.DatabaseHelper;
import com.example.shoestoreapplication.utils.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;

public class ProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ShapeableImageView imgAvatar;
    private TextView tvHeaderName, tvHeaderEmail;
    private EditText edtFullName, edtEmail, edtPhone, edtOldPassword, edtNewPassword;
    private Button btnSave;
    private Button btnLogout;
    private Button btnGoToOrderHistory;
    private Button btnMyCards; // Khai báo thêm biến nếu cần (hoặc dùng trực tiếp bằng findViewById)

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int currentUserId;
    private String selectedAvatarUri = null;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        selectedAvatarUri = imageUri.toString();
                        imgAvatar.setImageURI(imageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        initViews();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        imgAvatar = findViewById(R.id.img_profile_avatar);
        tvHeaderName = findViewById(R.id.tv_header_name);
        tvHeaderEmail = findViewById(R.id.tv_header_email);

        edtFullName = findViewById(R.id.edt_fullname);
        edtEmail = findViewById(R.id.edt_email);
        edtPhone = findViewById(R.id.edt_phone);
        edtOldPassword = findViewById(R.id.edt_old_password);
        edtNewPassword = findViewById(R.id.edt_new_password);

        btnSave = findViewById(R.id.btn_save);
        btnLogout = findViewById(R.id.btn_logout);
        btnGoToOrderHistory = findViewById(R.id.btn_go_to_order_history);
        btnMyCards = findViewById(R.id.btn_my_cards); // Ánh xạ ID nếu layout của bạn có nút này
    }

    private void loadUserData() {
        Cursor cursor = dbHelper.getUserById(currentUserId);
        if (cursor != null && cursor.moveToFirst()) {
            String fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            String avatarUrl = cursor.getString(cursor.getColumnIndexOrThrow("avatar_url"));

            tvHeaderName.setText(fullName);
            tvHeaderEmail.setText(email);
            edtFullName.setText(fullName);
            edtEmail.setText(email);
            edtPhone.setText(phone);

            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                try {
                    imgAvatar.setImageURI(Uri.parse(avatarUrl));
                    selectedAvatarUri = avatarUrl;
                } catch (Exception e) {
                    imgAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                }
            }
            cursor.close();
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        imgAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> handleSaveProfile());

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Thêm sự kiện chuyển màn hình sang SelectCardActivity cạnh btnGoToOrderHistory
        findViewById(R.id.btn_my_cards).setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, SelectCardActivity.class))
        );

        btnGoToOrderHistory.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, new OrderHistoryFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void handleSaveProfile() {
        String newName = edtFullName.getText().toString().trim();
        String newPhone = edtPhone.getText().toString().trim();
        String oldPass = edtOldPassword.getText().toString().trim();
        String newPass = edtNewPassword.getText().toString().trim();

        if (newName.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "Tên và số điện thoại không được để trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isUpdated = dbHelper.updateUserProfile(currentUserId, newName, newPhone, selectedAvatarUri);

        if (!newPass.isEmpty()) {
            if (oldPass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu hiện tại để đổi mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dbHelper.checkOldPassword(currentUserId, oldPass)) {
                if (newPass.length() < 6) {
                    Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
                    return;
                }
                dbHelper.updatePassword(currentUserId, newPass);
                edtOldPassword.setText("");
                edtNewPassword.setText("");
            } else {
                Toast.makeText(this, "Mật khẩu hiện tại không đúng!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (isUpdated) {
            Toast.makeText(this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
            tvHeaderName.setText(newName);
        } else {
            Toast.makeText(this, "Có lỗi xảy ra, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        }
    }
}