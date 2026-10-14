package com.example.shoestoreapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapplication.R;
import com.example.shoestoreapplication.models.CartItem;
import com.example.shoestoreapplication.utils.ImageUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private final OnCartChangeListener listener;
    private final DecimalFormat priceFormat = new DecimalFormat("#,###");

    public interface OnCartChangeListener {
        /** newQuantity có thể là 0 => Fragment sẽ hỏi xác nhận xóa. */
        void onQuantityChanged(CartItem item, int newQuantity);
        void onRemove(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartChangeListener listener) {
        this.cartItems = cartItems == null ? new ArrayList<>() : cartItems;
        this.listener = listener;
    }

    public void updateData(List<CartItem> newItems) {
        final List<CartItem> oldList = cartItems;
        final List<CartItem> newList = newItems == null ? new ArrayList<>() : newItems;

        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return oldList.size(); }
            @Override public int getNewListSize() { return newList.size(); }

            @Override
            public boolean areItemsTheSame(int o, int n) {
                return oldList.get(o).getCartItemId() == newList.get(n).getCartItemId();
            }

            @Override
            public boolean areContentsTheSame(int o, int n) {
                CartItem a = oldList.get(o), b = newList.get(n);
                return a.getQuantity() == b.getQuantity()
                        && a.getPrice() == b.getPrice()
                        && Objects.equals(a.getProductName(), b.getProductName())
                        && Objects.equals(a.getSize(), b.getSize())
                        && Objects.equals(a.getColor(), b.getColor())
                        && Objects.equals(a.getImageUrl(), b.getImageUrl());
            }
        });

        cartItems = newList;
        result.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.tvName.setText(item.getProductName());

        String color = item.getColor();
        String variant = "Size: " + item.getSize()
                + (color.isEmpty() ? "" : "  |  Màu: " + color);
        holder.tvSize.setText(variant);

        holder.tvQty.setText(String.valueOf(item.getQuantity()));
        holder.tvPrice.setText(priceFormat.format(item.getPrice()) + "đ");

        if (holder.imgProduct != null) {
            ImageUtils.load(holder.imgProduct, item.getImageUrl());
        }

        // Không sửa dữ liệu tại đây: ViewModel kiểm tra rồi cập nhật lại danh sách
        holder.btnPlus.setOnClickListener(v ->
                listener.onQuantityChanged(item, item.getQuantity() + 1));

        holder.btnMinus.setOnClickListener(v ->
                listener.onQuantityChanged(item, item.getQuantity() - 1));
    }

    @Override
    public int getItemCount() { return cartItems.size(); }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSize, tvPrice, tvQty, btnMinus, btnPlus;
        ImageView imgProduct;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvSize = itemView.findViewById(R.id.tv_size);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvQty = itemView.findViewById(R.id.tv_qty);
            btnMinus = itemView.findViewById(R.id.btn_minus);
            btnPlus = itemView.findViewById(R.id.btn_plus);

            // Giữ cách tìm id cũ cho tới khi bạn chốt tên id trong item_cart_product.xml
            String pkg = itemView.getContext().getPackageName();
            int id = itemView.getResources().getIdentifier("img_product", "id", pkg);
            if (id == 0) id = itemView.getResources().getIdentifier("img_cart_product", "id", pkg);
            imgProduct = id != 0 ? (ImageView) itemView.findViewById(id) : null;
        }
    }
}