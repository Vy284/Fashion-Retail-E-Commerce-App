package com.example.shoestoreapplication;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.shoestoreapplication.database.DatabaseHelper;

import java.util.Locale;

public class OrderDetailFragment extends Fragment {

    private static final String ARG_ORDER_ID = "order_id";

    public static OrderDetailFragment newInstance(int orderId) {
        OrderDetailFragment f = new OrderDetailFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_ORDER_ID, orderId);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        int orderId = getArguments() != null ? getArguments().getInt(ARG_ORDER_ID, -1) : -1;
        if (orderId == -1) return;

        DatabaseHelper db = new DatabaseHelper(requireContext());

        // Thông tin chung
        try (Cursor c = db.getOrderSummary(orderId)) {
            if (c.moveToFirst()) {
                ((TextView) view.findViewById(R.id.tv_code)).setText("Đơn hàng #" + c.getString(0));
                ((TextView) view.findViewById(R.id.tv_status)).setText(statusText(c.getString(1)));
                ((TextView) view.findViewById(R.id.tv_total)).setText(money(c.getInt(2)));
                ((TextView) view.findViewById(R.id.tv_date)).setText(c.getString(3));
                ((TextView) view.findViewById(R.id.tv_recipient)).setText(c.getString(4));
                ((TextView) view.findViewById(R.id.tv_phone)).setText(c.getString(5));
                ((TextView) view.findViewById(R.id.tv_address)).setText(c.getString(6));
            }
        }

        // Danh sách sản phẩm
        LinearLayout llItems = view.findViewById(R.id.ll_items);
        llItems.removeAllViews();
        try (Cursor c = db.getOrderItems(orderId)) {
            while (c.moveToNext()) {
                String name = c.getString(0);
                String size = c.getString(1);
                String color = c.getString(2);
                int qty = c.getInt(3);
                int price = c.getInt(4);

                RelativeLayout row = new RelativeLayout(requireContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.bottomMargin = dp(12);
                row.setLayoutParams(lp);

                LinearLayout left = new LinearLayout(requireContext());
                left.setOrientation(LinearLayout.VERTICAL);
                RelativeLayout.LayoutParams leftLp = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                leftLp.addRule(RelativeLayout.ALIGN_PARENT_START);
                leftLp.setMarginEnd(dp(100));
                left.setLayoutParams(leftLp);

                TextView tvName = new TextView(requireContext());
                tvName.setText(name);
                tvName.setTextColor(Color.BLACK);
                tvName.setTextSize(14);
                tvName.setTypeface(null, android.graphics.Typeface.BOLD);

                TextView tvInfo = new TextView(requireContext());
                tvInfo.setText("Size " + size + " • " + color + " • x" + qty);
                tvInfo.setTextColor(Color.parseColor("#757575"));
                tvInfo.setTextSize(13);

                left.addView(tvName);
                left.addView(tvInfo);

                TextView tvPrice = new TextView(requireContext());
                tvPrice.setText(money(price * qty));
                tvPrice.setTextColor(Color.BLACK);
                tvPrice.setTextSize(14);
                RelativeLayout.LayoutParams priceLp = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                priceLp.addRule(RelativeLayout.ALIGN_PARENT_END);
                tvPrice.setLayoutParams(priceLp);

                row.addView(left);
                row.addView(tvPrice);
                llItems.addView(row);
            }
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private String money(int amount) {
        return String.format(Locale.US, "%,d", amount).replace(',', '.') + "đ";
    }

    private String statusText(String s) {
        if (s == null) return "";
        switch (s) {
            case "pending": return "Chờ xác nhận";
            case "processing": return "Đang xử lý";
            case "shipped": return "Đang giao";
            case "delivered": return "Đã giao";
            case "cancelled": return "Đã hủy";
            default: return s;
        }
    }
}