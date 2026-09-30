package com.example.shoestoreapplication;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.shoestoreapplication.database.DatabaseHelper;
import com.example.shoestoreapplication.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;

public class ProductDetailFragment extends Fragment {

    private TextView tvBrand, tvName, tvPrice, tvDescription, tvQuantity;
    private ImageButton btnBack, btnFavorite, btnIncrease, btnDecrease;
    private MaterialButton btnAddToCart;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private int productId = 1;
    private int currentVariantId = -1;
    private int quantity = 1;

    public ProductDetailFragment() {
        // Required empty public constructor
    }


    public static ProductDetailFragment newInstance(int productId) {
        ProductDetailFragment fragment = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putInt("product_id", productId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getInt("product_id", 1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());

        initViews(view);
        loadProductData();
        setupListeners();
    }

    private void initViews(View view) {
        tvBrand = view.findViewById(R.id.tv_product_brand);
        tvName = view.findViewById(R.id.tv_product_name);
        tvPrice = view.findViewById(R.id.tv_product_price);
        tvDescription = view.findViewById(R.id.tv_product_description);
        tvQuantity = view.findViewById(R.id.tv_quantity);

        btnBack = view.findViewById(R.id.btn_back);
        btnFavorite = view.findViewById(R.id.btn_favorite);
        btnIncrease = view.findViewById(R.id.btn_increase);
        btnDecrease = view.findViewById(R.id.btn_decrease);
        btnAddToCart = view.findViewById(R.id.btn_add_to_cart);
    }

    private void loadProductData() {
        Cursor cursor = dbHelper.getProductDetail(productId);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(0);
            int price = cursor.getInt(1);
            String desc = cursor.getString(2);
            String brand = cursor.getString(3);

            tvName.setText(name);
            tvBrand.setText(brand);
            tvDescription.setText(desc != null ? desc : "Đang cập nhật mô tả.");

            DecimalFormat formatter = new DecimalFormat("#,###");
            tvPrice.setText(formatter.format(price) + "đ");

            cursor.close();
        }

        currentVariantId = dbHelper.getDefaultVariantId(productId);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnIncrease.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        btnAddToCart.setOnClickListener(v -> {
            if (currentVariantId == -1) {
                Toast.makeText(requireContext(), "Sản phẩm tạm hết hàng!", Toast.LENGTH_SHORT).show();
                return;
            }

            int userId = sessionManager.getUserId();
            int cartId = dbHelper.getOrCreateCartId(userId);

            boolean isAdded = dbHelper.addToCart(cartId, currentVariantId, quantity);
            if (isAdded) {
                Toast.makeText(requireContext(), "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Lỗi khi thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}