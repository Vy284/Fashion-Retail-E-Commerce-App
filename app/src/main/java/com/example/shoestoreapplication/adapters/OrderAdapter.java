package com.example.shoestoreapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapplication.R;
import com.example.shoestoreapplication.models.Order;
import java.text.DecimalFormat;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;

    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        if (holder.tvOrderId != null) holder.tvOrderId.setText("#KKS-" + order.getOrderId());
        if (holder.tvDate != null) holder.tvDate.setText(order.getDate());
        if (holder.tvStatus != null) holder.tvStatus.setText(order.getStatus());

        if (holder.tvTotal != null) {
            DecimalFormat formatter = new DecimalFormat("#,###");
            holder.tvTotal.setText(formatter.format(order.getTotalAmount()) + "đ");
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvDate, tvStatus, tvTotal, tvProductName, tvProductQty;
        ImageView imgProduct;
        Button btnDetail;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvDate = itemView.findViewById(R.id.tv_order_date);
            tvStatus = itemView.findViewById(R.id.tv_order_status);
            tvTotal = itemView.findViewById(R.id.tv_order_total);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductQty = itemView.findViewById(R.id.tv_product_qty);
            imgProduct = itemView.findViewById(R.id.img_product);
            btnDetail = itemView.findViewById(R.id.btn_detail);
        }
    }
}