package com.example.shoestoreapplication;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapplication.adapters.ProductAdapter;
import com.example.shoestoreapplication.database.DatabaseHelper;
import com.example.shoestoreapplication.models.Product;
import com.example.shoestoreapplication.utils.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class HomeFragment extends Fragment {

    private ProductAdapter productAdapter;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    // Khai báo imgAvatar ở mức Class để dùng được trong onResume
    private ShapeableImageView imgAvatar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());

        imgAvatar = view.findViewById(R.id.img_avatar);
        ImageButton btnNotification = view.findViewById(R.id.btn_notification);
        RecyclerView rvFeaturedProducts = view.findViewById(R.id.rv_featured_products);
        EditText edtSearch = view.findViewById(R.id.edt_search);

        // 1. Chuyển sang ProfileActivity khi bấm vào Avatar
        if (imgAvatar != null) {
            imgAvatar.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), ProfileActivity.class);
                startActivity(intent);
            });
        }

        // 2. Xử lý sự kiện khi bấm vào Nút Thông Báo
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Bạn bấm vào nút Thông Báo!", Toast.LENGTH_SHORT).show();
            });
        }

        if (rvFeaturedProducts != null) {
            rvFeaturedProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
            List<Product> products = dbHelper.getFeaturedProducts();
            productAdapter = new ProductAdapter(products);
            rvFeaturedProducts.setAdapter(productAdapter);
        }

        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String keyword = s.toString().trim();
                    if (keyword.isEmpty()) {
                        productAdapter.updateData(dbHelper.getFeaturedProducts());
                    } else {
                        productAdapter.updateData(dbHelper.searchProducts(keyword));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        loadUserAvatar();
    }

    private void loadUserAvatar() {
        if (imgAvatar != null && sessionManager != null && dbHelper != null) {
            int userId = sessionManager.getUserId();
            Cursor cursor = dbHelper.getUserById(userId);
            if (cursor != null && cursor.moveToFirst()) {
                String avatarUrl = cursor.getString(cursor.getColumnIndexOrThrow("avatar_url"));
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    try {
                        imgAvatar.setImageURI(Uri.parse(avatarUrl));
                    } catch (Exception ignored) {
                        imgAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                    }
                } else {
                    imgAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                }
                cursor.close();
            }
        }
    }
}