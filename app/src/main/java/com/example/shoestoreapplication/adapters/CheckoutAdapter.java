package com.example.shoestoreapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapplication.R;
import com.example.shoestoreapplication.models.CartItem;
import java.text.DecimalFormat;
import java.util.List;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {

    private List<CartItem> itemList;

    public CheckoutAdapter(List<CartItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        CartItem item = itemList.get(position);

        holder.tvName.setText(item.getProductName());
        holder.tvVariant.setText("Size: " + item.getSize() + " • SL: " + item.getQuantity());

        DecimalFormat formatter = new DecimalFormat("#,###");
        int lineTotal = item.getPrice() * item.getQuantity();
        holder.tvPrice.setText(formatter.format(lineTotal) + "đ");
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvVariant, tvPrice;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_checkout_product);
            tvName = itemView.findViewById(R.id.tv_checkout_name);
            tvVariant = itemView.findViewById(R.id.tv_checkout_variant);
            tvPrice = itemView.findViewById(R.id.tv_checkout_price);
        }
    }
}