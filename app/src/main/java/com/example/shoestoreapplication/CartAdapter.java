package com.example.shoestoreapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {

    public static class CartItem {
        public String name;
        public String size;
        public long price;
        public int qty;

        public CartItem(String name, String size, long price, int qty) {
            this.name = name;
            this.size = size;
            this.price = price;
            this.qty = qty;
        }
    }

    public interface OnCartChanged {
        void onChanged();
    }

    private final List<CartItem> items;
    private final OnCartChanged listener;

    public CartAdapter(List<CartItem> items, OnCartChanged listener) {
        this.items = items;
        this.listener = listener;
    }

    public static String formatVnd(long value) {
        return String.format(Locale.US, "%,d", value).replace(',', '.') + "đ";
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CartItem item = items.get(position);
        h.tvName.setText(item.name);
        h.tvSize.setText("Size: " + item.size);
        h.tvPrice.setText(formatVnd(item.price));
        h.tvQty.setText(String.valueOf(item.qty));

        h.btnPlus.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            items.get(pos).qty++;
            notifyItemChanged(pos);
            listener.onChanged();
        });

        h.btnMinus.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            CartItem it = items.get(pos);
            if (it.qty > 1) {
                it.qty--;
                notifyItemChanged(pos);
            } else {
                items.remove(pos);
                notifyItemRemoved(pos);
            }
            listener.onChanged();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvSize, tvPrice, tvQty, btnMinus, btnPlus;

        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvSize = v.findViewById(R.id.tv_size);
            tvPrice = v.findViewById(R.id.tv_price);
            tvQty = v.findViewById(R.id.tv_qty);
            btnMinus = v.findViewById(R.id.btn_minus);
            btnPlus = v.findViewById(R.id.btn_plus);
        }
    }
}