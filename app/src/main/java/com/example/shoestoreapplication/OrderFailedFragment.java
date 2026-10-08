package com.example.shoestoreapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OrderFailedFragment extends Fragment {

    private static final String ARG_NAME = "name";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_ADDRESS = "address";

    public static OrderFailedFragment newInstance(String name, String phone, String address) {
        OrderFailedFragment f = new OrderFailedFragment();
        Bundle b = new Bundle();
        b.putString(ARG_NAME, name);
        b.putString(ARG_PHONE, phone);
        b.putString(ARG_ADDRESS, address);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_failed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            ((TextView) view.findViewById(R.id.tv_receiver_info))
                    .setText(args.getString(ARG_NAME) + " - " + args.getString(ARG_PHONE));
            ((TextView) view.findViewById(R.id.tv_receiver_address))
                    .setText(args.getString(ARG_ADDRESS));
        }
        // Đơn thất bại chưa có mã giao dịch
        ((TextView) view.findViewById(R.id.tv_order_code)).setText("Không có");

        // Thử lại: quay về màn thanh toán (giỏ hàng vẫn còn)
        view.findViewById(R.id.btn_retry).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());
    }
}