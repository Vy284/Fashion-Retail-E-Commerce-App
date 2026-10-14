package com.example.shoestoreapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapplication.R;
import com.example.shoestoreapplication.models.CartItem;
import com.example.shoestoreapplication.utils.ImageUtils;
import java.text.DecimalFormat;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private final OnCartChangeListener listener;

    public interface OnCartChangeListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onRemove(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartChangeListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    public void updateData(List<CartItem> newItems) {
        this.cartItems = newItems;
        notifyDataSetChanged();
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
        holder.tvSize.setText("Size: " + item.getSize());
        holder.tvQty.setText(String.valueOf(item.getQuantity()));
        holder.tvPrice.setText(new DecimalFormat("#,###").format(item.getPrice()) + "đ");

        // Ảnh đã được lấy sẵn trong getCartItems(): không cần truy vấn DB trong Adapter
        if (holder.imgProduct != null) {
            ImageUtils.load(holder.imgProduct, item.getImageUrl());
        }

        holder.btnPlus.setOnClickListener(v -> {
            int newQty = item.getQuantity() + 1;
            item.setQuantity(newQty);
            holder.tvQty.setText(String.valueOf(newQty));
            listener.onQuantityChanged(item, newQty);
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                int newQty = item.getQuantity() - 1;
                item.setQuantity(newQty);
                holder.tvQty.setText(String.valueOf(newQty));
                listener.onQuantityChanged(item, newQty);
            } else {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Xóa sản phẩm")
                        .setMessage("Bỏ \"" + item.getProductName() + "\" khỏi giỏ hàng?")
                        .setPositiveButton("Xóa", (d, w) -> listener.onRemove(item))
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
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

            String pkg = itemView.getContext().getPackageName();
            int id = itemView.getResources().getIdentifier("img_product", "id", pkg);
            if (id == 0) id = itemView.getResources().getIdentifier("img_cart_product", "id", pkg);
            imgProduct = id != 0 ? (ImageView) itemView.findViewById(id) : null;
        }
    }
}