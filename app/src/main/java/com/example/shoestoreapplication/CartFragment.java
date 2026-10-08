package com.example.shoestoreapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapplication.database.DatabaseHelper;
import com.example.shoestoreapplication.models.CartItem;
import com.example.shoestoreapplication.utils.SessionManager;
import com.example.shoestoreapplication.adapters.CartAdapter;
import java.text.DecimalFormat;
import java.util.List;

public class CartFragment extends Fragment {

    private RecyclerView rvCartItems;
    private TextView tvSubtotal, tvTotalPrice;
    private Button btnCheckout;

    private CartAdapter adapter;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int cartId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());
        cartId = dbHelper.getOrCreateCartId(sessionManager.getUserId());

        rvCartItems = view.findViewById(R.id.rv_cart_items);
        tvSubtotal = view.findViewById(R.id.tv_subtotal);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);
        btnCheckout = view.findViewById(R.id.btn_checkout);

        setupRecyclerView();

        btnCheckout.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CheckoutFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void setupRecyclerView() {
        rvCartItems.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<CartItem> items = dbHelper.getCartItems(cartId);
        adapter = new CartAdapter(items, (item, newQuantity) -> {
            dbHelper.updateCartItemQuantity(item.getCartItemId(), newQuantity);
            calculateTotal(items);
        });

        rvCartItems.setAdapter(adapter);
        calculateTotal(items);
    }

    private void calculateTotal(List<CartItem> items) {
        int total = 0;
        for (CartItem item : items) {
            total += (item.getPrice() * item.getQuantity());
        }

        DecimalFormat formatter = new DecimalFormat("#,###");
        String formattedTotal = formatter.format(total) + "đ";
        tvSubtotal.setText(formattedTotal);
        tvTotalPrice.setText(formattedTotal);
    }
}