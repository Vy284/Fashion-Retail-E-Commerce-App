package com.example.shoestoreapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapplication.adapters.OrderAdapter;
import com.example.shoestoreapplication.database.DatabaseHelper;
import com.example.shoestoreapplication.models.Order;
import com.example.shoestoreapplication.utils.Nav;
import com.example.shoestoreapplication.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView rvOrders;
    private LinearLayout layoutEmpty;
    private MaterialButton btnShopNow;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());

        rvOrders = view.findViewById(R.id.rv_order_history);
        layoutEmpty = view.findViewById(R.id.layout_empty_orders);
        btnShopNow = view.findViewById(R.id.btn_shop_now);

        // Khám phá ngay: quay về trang chủ (cùng cách mở màn như các màn khác)
        btnShopNow.setOnClickListener(v -> Nav.open(this, new HomeFragment()));

        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        setupRecyclerView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (rvOrders != null) setupRecyclerView();
    }

    private void setupRecyclerView() {
        int userId = sessionManager.getUserId();

        // Chưa đăng nhập: hiện màn trống, không truy vấn
        List<Order> orderList = userId == -1 ? null : dbHelper.getOrderHistory(userId);

        if (orderList == null || orderList.isEmpty()) {
            rvOrders.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvOrders.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);

            OrderAdapter adapter = new OrderAdapter(orderList, order ->
                    Nav.open(this, OrderDetailFragment.newInstance(order.getOrderId())));
            rvOrders.setAdapter(adapter);
        }
    }
}