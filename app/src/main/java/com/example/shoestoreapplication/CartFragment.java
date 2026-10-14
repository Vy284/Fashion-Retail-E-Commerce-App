package com.example.shoestoreapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CartFragment extends Fragment {

    private final DecimalFormat priceFormat = new DecimalFormat("#,###");

    private TextView tvSubtotal, tvTotalPrice, tvEmptyCart;
    private RecyclerView rvCart;
    private Button btnCheckout;
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

        // Dùng chung ViewModel với CheckoutFragment (cùng Activity)
        vm = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        rvCart = view.findViewById(R.id.rv_cart_items);
        tvSubtotal = view.findViewById(R.id.tv_subtotal);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);
        tvEmptyCart = view.findViewById(R.id.tv_empty_cart); // cần thêm vào fragment_cart.xml
        btnCheckout = view.findViewById(R.id.btn_checkout);

        rvCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CartAdapter(new ArrayList<>(), new CartAdapter.OnCartChangeListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQuantity) {
                if (newQuantity < 1) {
                    confirmRemove(item);          // giảm về 0 => hỏi xóa
                } else {
                    vm.changeQuantity(item, newQuantity);
                }
            }

            @Override
            public void onRemove(CartItem item) {
                confirmRemove(item);
            }
        });
        rvCart.setAdapter(adapter);

        observeViewModel();

        btnCheckout.setOnClickListener(v -> {
            // Validation: không cho thanh toán khi giỏ trống
            if (adapter.getItemCount() == 0) {
                Toast.makeText(requireContext(),
                        "Giỏ hàng đang trống", Toast.LENGTH_SHORT).show();
                return;
            }
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CheckoutFragment())
                    .addToBackStack(null)
                    .commit();
        });

        vm.load(new SessionManager(requireContext()).getUserId());
    }

    private void observeViewModel() {
        vm.getItems().observe(getViewLifecycleOwner(), items -> {
            boolean empty = items == null || items.isEmpty();
            adapter.updateData(items == null ? new ArrayList<>() : items);
            tvEmptyCart.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvCart.setVisibility(empty ? View.GONE : View.VISIBLE);
            btnCheckout.setEnabled(!empty);
        });

        vm.getTotal().observe(getViewLifecycleOwner(), total -> {
            String text = priceFormat.format(total == null ? 0 : total) + "đ";
            tvSubtotal.setText(text);
            tvTotalPrice.setText(text);
        });

        vm.getError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                vm.clearError();
            }
        });
    }

    private void confirmRemove(CartItem item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> {
                    vm.remove(item);
                    Toast.makeText(requireContext(),
                            "Đã xóa khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}