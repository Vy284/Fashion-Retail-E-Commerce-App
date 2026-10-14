package com.example.shoestoreapplication;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.shoestoreapplication.database.dao.OrderDao;

public class OrderSuccessFragment extends Fragment {

    private OrderDao orderDao;

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

        orderDao = new OrderDao(requireContext());

        TextView tvOrderCode = view.findViewById(R.id.tv_order_code);
        TextView tvReceiverInfo = view.findViewById(R.id.tv_receiver_info);
        TextView tvReceiverAddress = view.findViewById(R.id.tv_receiver_address);
        Button btnBackHome = view.findViewById(R.id.btn_back_home);

        long orderId = getArguments() != null ? getArguments().getLong("order_id", -1) : -1;

        loadOrderInfo(orderId, tvOrderCode, tvReceiverInfo, tvReceiverAddress);

        btnBackHome.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        });

        return view;
    }

    // Lấy mã đơn và thông tin người nhận từ chính đơn hàng (snapshot lúc đặt)
    private void loadOrderInfo(long orderId, TextView tvCode, TextView tvInfo, TextView tvAddress) {
        if (orderId == -1) {
            return;
        }
        // Cột: 0=order_code, 1=status, 2=total, 3=created_at, 4=tên, 5=sđt, 6=địa chỉ
        try (Cursor c = orderDao.getOrderSummary((int) orderId)) {
            if (c.moveToFirst()) {
                tvCode.setText("#" + c.getString(0));
                tvInfo.setText(c.getString(4) + " - " + c.getString(5));
                tvAddress.setText(c.getString(6));
            }
        }
    }
}