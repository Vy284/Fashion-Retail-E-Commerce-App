package com.example.shoestoreapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.shoestoreapplication.database.DatabaseHelper;
import com.example.shoestoreapplication.utils.SessionManager;

public class OrderSuccessFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    public static OrderSuccessFragment newInstance(long orderId) {
        OrderSuccessFragment fragment = new OrderSuccessFragment();
        Bundle args = new Bundle();
        args.putLong("order_id", orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_success, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());

        TextView tvOrderCode = view.findViewById(R.id.tv_order_code);
        TextView tvReceiverInfo = view.findViewById(R.id.tv_receiver_info);
        TextView tvReceiverAddress = view.findViewById(R.id.tv_receiver_address);
        Button btnBackHome = view.findViewById(R.id.btn_back_home);

        long orderId = -1;
        if (getArguments() != null) {
            orderId = getArguments().getLong("order_id");
            tvOrderCode.setText("#KKS-" + orderId);
        }

        loadReceiverInfo(orderId, tvReceiverInfo, tvReceiverAddress);

        btnBackHome.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        });

        return view;
    }

    private void loadReceiverInfo(long orderId, TextView tvInfo, TextView tvAddress) {
        int userId = sessionManager.getUserId();
        String name = "";
        String phone = "";

        Cursor userCursor = dbHelper.getUserById(userId);
        if (userCursor != null && userCursor.moveToFirst()) {
            name = userCursor.getString(userCursor.getColumnIndexOrThrow("full_name"));
            phone = userCursor.getString(userCursor.getColumnIndexOrThrow("phone"));
            userCursor.close();
        }

        tvInfo.setText(name + " - " + phone);

        if (orderId != -1) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor orderCursor = db.rawQuery("SELECT address_text_snapshot FROM Orders WHERE order_id = ?", new String[]{String.valueOf(orderId)});
            if (orderCursor != null && orderCursor.moveToFirst()) {
                String address = orderCursor.getString(0);
                tvAddress.setText(address);
                orderCursor.close();
            }
        }
    }
}