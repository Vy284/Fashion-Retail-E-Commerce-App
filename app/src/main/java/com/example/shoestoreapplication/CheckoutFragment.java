package com.example.shoestoreapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
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
import java.util.ArrayList;
import java.util.List;

public class CheckoutFragment extends Fragment {

    private RecyclerView rvCheckoutItems;
    private EditText edtName, edtPhone, edtAddress;
    private RadioGroup rgPayment;
    private TextView tvSubtotal, tvTotal;
    private Button btnPlaceOrder;
    private ImageButton btnBack;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int cartId = -1;
    private int totalAmount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());

        int userId = sessionManager.getUserId();
        if (userId != -1) {
            cartId = dbHelper.getOrCreateCartId(userId);
        }

        rvCheckoutItems = view.findViewById(R.id.rv_checkout_items);
        edtName = view.findViewById(R.id.edt_receiver_name);
        edtPhone = view.findViewById(R.id.edt_receiver_phone);
        edtAddress = view.findViewById(R.id.edt_receiver_address);
        rgPayment = view.findViewById(R.id.rg_payment);
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
        rvCheckoutItems.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<CartItem> items = cartId == -1
                ? new ArrayList<>()
                : dbHelper.getCartItems(cartId);
        rvCheckoutItems.setAdapter(new CheckoutAdapter(items));

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
        if (cartId == -1) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để đặt hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedId = rgPayment.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(requireContext(), "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }
        String paymentMethod = (checkedId == R.id.rb_cod) ? "COD" : "Transfer";

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
            // Đặt hàng thất bại: sang màn thất bại, bấm "Thử lại" sẽ quay về màn này
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container,
                            OrderFailedFragment.newInstance(name, phone, address))
                    .addToBackStack(null)
                    .commit();
        }
    }
}