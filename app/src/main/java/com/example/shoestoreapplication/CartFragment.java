package com.example.shoestoreapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    private final List<CartAdapter.CartItem> items = new ArrayList<>();
    private TextView tvSubtotal, tvTotal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvSubtotal = view.findViewById(R.id.tv_subtotal);
        tvTotal = view.findViewById(R.id.tv_total_price);
        RecyclerView rv = view.findViewById(R.id.rv_cart_items);

        // Dữ liệu mẫu, sau này lấy từ database
        items.add(new CartAdapter.CartItem("Nike Air Force 1", "42", 2990000, 1));
        items.add(new CartAdapter.CartItem("Adidas Ultraboost", "41", 3200000, 1));
        items.add(new CartAdapter.CartItem("Converse Chuck 70", "40", 2990000, 1));

        CartAdapter adapter = new CartAdapter(items, this::updateTotal);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        updateTotal();
    }

    private void updateTotal() {
        long total = 0;
        for (CartAdapter.CartItem it : items) {
            total += it.price * it.qty;
        }
        tvSubtotal.setText(CartAdapter.formatVnd(total));
        tvTotal.setText(CartAdapter.formatVnd(total));
    }
}