package com.example.shoestoreapplication;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shoestoreapplication.database.DatabaseHelper;
import com.example.shoestoreapplication.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.Locale;

public class SelectCardActivity extends AppCompatActivity {

    private LinearLayout cardsContainer;
    private TextView txtSavedCardTitle, txtEmpty, txtTotalPayment;
    private Button btnAddCard, btnPayment;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private int selectedCardId = -1;
    private String selectedLastFour = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_card);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        cardsContainer = findViewById(R.id.cardsContainer);
        txtSavedCardTitle = findViewById(R.id.txtSavedCardTitle);
        txtEmpty = findViewById(R.id.txtEmpty);
        txtTotalPayment = findViewById(R.id.txtTotalPayment);
        btnAddCard = findViewById(R.id.btnAddCard);
        btnPayment = findViewById(R.id.btnPayment);

        ((ImageView) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());

        // Tổng tiền truyền từ màn thanh toán: intent.putExtra("total", total)
        int total = getIntent().getIntExtra("total", -1);
        if (total >= 0) {
            txtTotalPayment.setText(
                    NumberFormat.getInstance(new Locale("vi", "VN")).format(total) + "đ");
        }

        btnAddCard.setOnClickListener(v ->
                startActivity(new Intent(this, AddCardActivity.class)));

        btnPayment.setOnClickListener(v -> {
            if (selectedCardId == -1) {
                Toast.makeText(this, "Vui lòng chọn một thẻ để thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }
            // Trả thẻ đã chọn về màn gọi. Chỗ này nối vào code đặt hàng hiện có của bạn.
            Intent result = new Intent();
            result.putExtra("card_id", selectedCardId);
            result.putExtra("last_four", selectedLastFour);
            setResult(RESULT_OK, result);
            finish();
        });
    }

    // Quay lại từ AddCardActivity thì tải lại danh sách
    @Override
    protected void onResume() {
        super.onResume();
        loadCards();
    }

    private void loadCards() {
        cardsContainer.removeAllViews();

        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean selectionStillExists = false;
        int count = 0;
        LayoutInflater inflater = LayoutInflater.from(this);

        try (Cursor c = dbHelper.getCardsByUserId(userId)) {
            int idxId = c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CARD_ID);
            int idxHolder = c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CARD_HOLDER_NAME);
            int idxLast4 = c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CARD_LAST_FOUR);
            int idxExpiry = c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CARD_EXPIRY);

            while (c.moveToNext()) {
                final int cardId = c.getInt(idxId);
                final String holder = c.getString(idxHolder);
                final String lastFour = c.getString(idxLast4);
                final String expiry = c.getString(idxExpiry);

                View item = inflater.inflate(R.layout.item_saved_card, cardsContainer, false);
                ((TextView) item.findViewById(R.id.txtCardNumber))
                        .setText("••••  ••••  ••••  " + lastFour);
                ((TextView) item.findViewById(R.id.txtCardHolder)).setText(holder);
                ((TextView) item.findViewById(R.id.txtCardExpiry)).setText(expiry);

                if (cardId == selectedCardId) {
                    selectionStillExists = true;
                }
                applySelection(item, cardId == selectedCardId);

                item.setOnClickListener(v -> {
                    selectedCardId = cardId;
                    selectedLastFour = lastFour;
                    refreshSelection();
                });
                item.setOnLongClickListener(v -> {
                    confirmDelete(cardId, lastFour);
                    return true;
                });
                item.setTag(cardId);

                cardsContainer.addView(item);
                count++;
            }
        }

        if (!selectionStillExists) {
            selectedCardId = -1;
            selectedLastFour = "";
        }

        txtSavedCardTitle.setText("Thẻ đã lưu (" + count + ")");
        txtEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
    }

    private void refreshSelection() {
        for (int i = 0; i < cardsContainer.getChildCount(); i++) {
            View item = cardsContainer.getChildAt(i);
            applySelection(item, (int) item.getTag() == selectedCardId);
        }
    }

    private void applySelection(View item, boolean selected) {
        MaterialCardView card = (MaterialCardView) item;
        card.setStrokeWidth(selected ? (int) (3 * getResources().getDisplayMetrics().density) : 0);
        item.findViewById(R.id.txtSelected).setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
    }

    private void confirmDelete(int cardId, String lastFour) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa thẻ")
                .setMessage("Bạn có chắc muốn xóa thẻ •••• " + lastFour + " không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> {
                    if (dbHelper.deleteCard(cardId)) {
                        if (cardId == selectedCardId) {
                            selectedCardId = -1;
                            selectedLastFour = "";
                        }
                        loadCards();
                    } else {
                        Toast.makeText(this, "Xóa thẻ thất bại", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}