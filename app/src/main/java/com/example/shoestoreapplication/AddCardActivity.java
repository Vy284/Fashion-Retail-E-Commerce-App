package com.example.shoestoreapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shoestoreapplication.database.DatabaseHelper;

public class AddCardActivity extends AppCompatActivity {

    private EditText edtCardName, edtCardNumber, edtExpiry, edtCvv;
    private Button btnSaveCard;
    private ImageView btnBack;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        dbHelper = new DatabaseHelper(this);

        edtCardName = findViewById(R.id.edtCardName);
        edtCardNumber = findViewById(R.id.edtCardNumber);
        edtExpiry = findViewById(R.id.edtExpiry);
        edtCvv = findViewById(R.id.edtCvv);
        btnSaveCard = findViewById(R.id.btnSaveCard);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        edtExpiry.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private boolean deletingHyphen;
            private int hyphenStart;
            private boolean deletingBackward;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (isFormatting) return;
                deletingBackward = count > after;
                if (deletingBackward && s.charAt(start) == '/') {
                    deletingHyphen = true;
                    hyphenStart = start;
                } else {
                    deletingHyphen = false;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                if (deletingHyphen && hyphenStart > 0) {
                    if (deletingBackward) {
                        if (hyphenStart - 1 < s.length()) {
                            s.delete(hyphenStart - 1, hyphenStart);
                        }
                    }
                }

                if (s.length() == 2 && !deletingBackward) {
                    s.append("/");
                } else if (s.length() > 2 && s.charAt(2) != '/') {
                    s.insert(2, "/");
                }

                isFormatting = false;
            }
        });

        btnSaveCard.setOnClickListener(v -> saveCard());
    }

    private void saveCard() {
        String cardName = edtCardName.getText().toString().trim();
        String cardNumber = edtCardNumber.getText().toString().trim();
        String expiry = edtExpiry.getText().toString().trim();
        String cvv = edtCvv.getText().toString().trim();

        if (TextUtils.isEmpty(cardName)) {
            edtCardName.setError("Vui lòng nhập tên chủ thẻ");
            edtCardName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(cardNumber) || cardNumber.length() < 13 || cardNumber.length() > 19) {
            edtCardNumber.setError("Số thẻ phải từ 13 đến 19 chữ số");
            edtCardNumber.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(expiry) || !expiry.matches("^(0[1-9]|1[0-2])\\/([0-9]{2})$")) {
            edtExpiry.setError("Hạn thẻ không hợp lệ (MM/YY)");
            edtExpiry.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(cvv) || (cvv.length() != 3 && cvv.length() != 4)) {
            edtCvv.setError("CVV phải có 3 hoặc 4 chữ số");
            edtCvv.requestFocus();
            return;
        }

        String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm thẻ", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = dbHelper.addCard(userId, cardName.toUpperCase(), lastFourDigits, expiry, cvv);

        if (result != -1) {
            Toast.makeText(this, "Lưu thẻ thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Lưu thẻ thất bại, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }
}
