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
import com.example.shoestoreapplication.database.DatabaseHelper;
import com.example.shoestoreapplication.models.Order;
import com.example.shoestoreapplication.utils.ImageUtils;
import java.text.DecimalFormat;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnDetailClickListener {
        void onDetailClick(Order order);
    }

    private List<Order> orderList;
    private OnDetailClickListener detailListener;
    private DatabaseHelper dbHelper;

    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    public OrderAdapter(List<Order> orderList, OnDetailClickListener listener) {
        this.orderList = orderList;
        this.detailListener = listener;
    }

    public void setOnDetailClickListener(OnDetailClickListener listener) {
        this.detailListener = listener;
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

        // Món đầu tiên của đơn: tên, số lượng, ảnh
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(holder.itemView.getContext());
        }
        String[] preview = dbHelper.getOrderPreview(order.getOrderId());
        int others = Integer.parseInt(preview[3]);

        if (holder.tvProductName != null) holder.tvProductName.setText(preview[0]);
        if (holder.tvProductQty != null) {
            String qtyText = "SL: " + preview[2];
            if (others > 0) qtyText += " • +" + others + " sản phẩm khác";
            holder.tvProductQty.setText(qtyText);
        }
        if (holder.imgProduct != null) ImageUtils.load(holder.imgProduct, preview[1]);

        // Bấm "Chi tiết" hoặc bấm cả thẻ đơn hàng đều mở màn chi tiết
        View.OnClickListener open = v -> {
            if (detailListener != null) detailListener.onDetailClick(order);
        };
        if (holder.btnDetail != null) holder.btnDetail.setOnClickListener(open);
        holder.itemView.setOnClickListener(open);
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