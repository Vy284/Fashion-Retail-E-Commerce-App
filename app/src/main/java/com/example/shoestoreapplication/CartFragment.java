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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapplication.adapters.CartAdapter;
import com.example.shoestoreapplication.models.CartItem;
import com.example.shoestoreapplication.utils.SessionManager;
import com.example.shoestoreapplication.viewmodel.CartViewModel;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CartFragment extends Fragment {

    private TextView tvSubtotal, tvTotalPrice;
    private CartAdapter adapter;
    private CartViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(this).get(CartViewModel.class);

        RecyclerView rv = view.findViewById(R.id.rv_cart_items);
        tvSubtotal = view.findViewById(R.id.tv_subtotal);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);
        Button btnCheckout = view.findViewById(R.id.btn_checkout);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CartAdapter(new ArrayList<>(), new CartAdapter.OnCartChangeListener() {
            @Override public void onQuantityChanged(CartItem item, int newQuantity) {
                vm.changeQuantity(item, newQuantity);
            }
            @Override public void onRemove(CartItem item) { vm.remove(item); }
        });
        rv.setAdapter(adapter);

        vm.getItems().observe(getViewLifecycleOwner(), adapter::updateData);
        vm.getTotal().observe(getViewLifecycleOwner(), total -> {
            String text = new DecimalFormat("#,###").format(total) + "đ";
            tvSubtotal.setText(text);
            tvTotalPrice.setText(text);
        });

        btnCheckout.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CheckoutFragment())
                        .addToBackStack(null)
                        .commit());

        vm.load(new SessionManager(requireContext()).getUserId());
    }
}