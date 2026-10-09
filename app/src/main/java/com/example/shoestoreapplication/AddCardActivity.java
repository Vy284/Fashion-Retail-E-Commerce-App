package com.example.shoestoreapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shoestoreapplication.database.DatabaseHelper;
import com.example.shoestoreapplication.utils.SessionManager;

public class AddCardActivity extends AppCompatActivity {

    private EditText edtCardName, edtCardNumber, edtExpiry, edtCvv;
    private Button btnSaveCard;
    private ImageView btnBack;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        edtCardName = findViewById(R.id.edtCardName);
        edtCardNumber = findViewById(R.id.edtCardNumber);
        edtExpiry = findViewById(R.id.edtExpiry);
        edtCvv = findViewById(R.id.edtCvv);
        btnSaveCard = findViewById(R.id.btnSaveCard);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // Tự động định dạng MM/YY khi nhập hạn thẻ (xử lý tốt hơn khi gõ / xóa / paste)
        edtExpiry.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                // Chỉ giữ lại chữ số
                String digits = s.toString().replaceAll("[^0-9]", "");
                if (digits.length() > 4) {
                    digits = digits.substring(0, 4);
                }

                String formatted;
                if (digits.length() >= 3) {
                    // 3–4 số → MM/YY
                    formatted = digits.substring(0, 2) + "/" + digits.substring(2);
                } else if (digits.length() >= 1) {
                    // 1–2 số → chỉ hiện tháng
                    formatted = digits;
                } else {
                    formatted = "";
                }

                if (!formatted.contentEquals(s)) {
                    s.replace(0, s.length(), formatted);
                }

                isFormatting = false;
            }
        });

        btnSaveCard.setOnClickListener(v -> saveCard());
    }

    private void saveCard() {
        String cardName = edtCardName.getText().toString().trim();
        String cardNumber = edtCardNumber.getText().toString().replace(" ", "").trim();
        String expiry = edtExpiry.getText().toString().trim();
        String cvv = edtCvv.getText().toString().trim();

        if (TextUtils.isEmpty(cardName)) {
            edtCardName.setError("Vui lòng nhập tên chủ thẻ");
            edtCardName.requestFocus();
            return;
        }

        if (!cardNumber.matches("^[0-9]{13,19}$")) {
            edtCardNumber.setError("Số thẻ phải từ 13 đến 19 chữ số");
            edtCardNumber.requestFocus();
            return;
        }

        // Chuẩn hóa hạn thẻ trước khi kiểm tra (phòng trường hợp thiếu dấu /)
        expiry = normalizeExpiry(expiry);

        if (TextUtils.isEmpty(expiry) || !expiry.matches("^(0[1-9]|1[0-2])/([0-9]{2})$")) {
            edtExpiry.setError("Hạn thẻ không hợp lệ (MM/YY)");
            edtExpiry.requestFocus();
            return;
        }

        // Ghi lại giá trị đã chuẩn hóa lên ô nhập
        edtExpiry.setText(expiry);

        if (!cvv.matches("^[0-9]{3,4}$")) {
            edtCvv.setError("CVV phải có 3 hoặc 4 chữ số");
            edtCvv.requestFocus();
            return;
        }

        int userId = sessionManager.getUserId();
        if (!sessionManager.isLoggedIn() || userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm thẻ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy 4 số cuối của thẻ để hiển thị dạng giả lập an toàn
        String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);
        boolean success = dbHelper.addCard(userId, cardName.toUpperCase(), lastFourDigits, expiry, cvv);

        if (success) {
            Toast.makeText(this, "Lưu thẻ thành công!", Toast.LENGTH_SHORT).show();
            finish(); // Quay lại màn hình chọn thẻ
        } else {
            Toast.makeText(this, "Lưu thẻ thất bại, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }

    /** Chuẩn hóa chuỗi hạn thẻ thành dạng MM/YY */
    private String normalizeExpiry(String raw) {
        if (raw == null) return "";
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() != 4) return raw.trim(); // giữ nguyên nếu chưa đủ 4 số
        return digits.substring(0, 2) + "/" + digits.substring(2);
    }
}