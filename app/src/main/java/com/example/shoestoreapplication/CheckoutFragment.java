package com.example.shoestoreapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapplication.adapters.CheckoutAdapter;
import com.example.shoestoreapplication.database.DatabaseHelper;
import com.example.shoestoreapplication.models.CartItem;
import com.example.shoestoreapplication.utils.SessionManager;

import java.text.DecimalFormat;
import java.util.List;

public class CheckoutFragment extends Fragment {

    private RecyclerView rvCheckoutItems;
    private EditText edtName, edtPhone, edtAddress;
    private RadioButton rbCod;
    private TextView tvSubtotal, tvTotal;
    private Button btnPlaceOrder;
    private ImageButton btnBack;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int cartId;
    private int totalAmount = 0; // Đưa về 0 để tính toán động

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());
        cartId = dbHelper.getOrCreateCartId(sessionManager.getUserId());

        rvCheckoutItems = view.findViewById(R.id.rv_checkout_items);
        edtName = view.findViewById(R.id.edt_receiver_name);
        edtPhone = view.findViewById(R.id.edt_receiver_phone);
        edtAddress = view.findViewById(R.id.edt_receiver_address);
        rbCod = view.findViewById(R.id.rb_cod);
        tvSubtotal = view.findViewById(R.id.tv_checkout_subtotal);
        tvTotal = view.findViewById(R.id.tv_checkout_total);
        btnPlaceOrder = view.findViewById(R.id.btn_place_order);
        btnBack = view.findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        btnPlaceOrder.setOnClickListener(v -> processOrder());

        loadCartData();

        return view;
    }

    private void loadCartData() {
        List<CartItem> items = dbHelper.getCartItems(cartId);

        rvCheckoutItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        CheckoutAdapter adapter = new CheckoutAdapter(items);
        rvCheckoutItems.setAdapter(adapter);

        totalAmount = 0;
        for (CartItem item : items) {
            totalAmount += (item.getPrice() * item.getQuantity());
        }

        DecimalFormat formatter = new DecimalFormat("#,###");
        String formattedTotal = formatter.format(totalAmount) + "đ";
        tvSubtotal.setText(formattedTotal);
        tvTotal.setText(formattedTotal);
    }

    private void processOrder() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String paymentMethod = rbCod.isChecked() ? "COD" : "Transfer";

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (totalAmount <= 0) {
            Toast.makeText(requireContext(), "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        long orderId = dbHelper.placeOrder(sessionManager.getUserId(), cartId, name, phone, address, paymentMethod, totalAmount);

        if (orderId != -1) {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, OrderSuccessFragment.newInstance(orderId))
                    .commit();
        } else {
            Toast.makeText(requireContext(), "Lỗi đặt hàng", Toast.LENGTH_SHORT).show();
        }
    }
}