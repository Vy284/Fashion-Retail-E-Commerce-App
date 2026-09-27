package com.example.shoestoreapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.imageview.ShapeableImageView;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khai báo biến cục bộ trực tiếp tại đây
        ShapeableImageView imgAvatar = view.findViewById(R.id.img_avatar);
        ImageButton btnNotification = view.findViewById(R.id.btn_notification);

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

        return view;
    }
}