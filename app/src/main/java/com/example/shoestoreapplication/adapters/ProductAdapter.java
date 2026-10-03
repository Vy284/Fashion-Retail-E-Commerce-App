package com.example.shoestoreapplication.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapplication.R;
import com.example.shoestoreapplication.models.Product;
import com.example.shoestoreapplication.utils.ImageUtils;
import com.example.shoestoreapplication.utils.Money;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    public interface OnProductClick {
        void onClick(Product product);
    }

    // Tùy chọn: gắn vào nếu màn hình muốn hiện nút tim wishlist
    public interface WishlistHandler {
        boolean isWishlisted(Product product);   // sản phẩm đang trong wishlist?
        boolean onToggle(Product product);       // bấm tim; trả về trạng thái SAU khi bấm
    }

    private List<Product> items = new ArrayList<>();
    private final OnProductClick listener;
    private WishlistHandler wishlistHandler;

    public ProductAdapter(OnProductClick listener) {
        this.listener = listener;
    }

    public void setItems(List<Product> list) {
        this.items = list;
        notifyDataSetChanged();
    }

    public void setWishlistHandler(WishlistHandler handler) {
        this.wishlistHandler = handler;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Product p = items.get(position);
        h.tvBrand.setText(p.getBrand());
        h.tvName.setText(p.getName());
        h.tvPrice.setText(Money.vnd(p.getPrice()));
        ImageUtils.load(h.img, p.getImageUrl());
        h.itemView.setOnClickListener(v -> listener.onClick(p));

        // Nút tim: chỉ hiện khi layout có tv_wishlist VÀ đã gắn handler
        if (h.tvWishlist != null) {
            if (wishlistHandler == null) {
                h.tvWishlist.setVisibility(View.GONE);
            } else {
                h.tvWishlist.setVisibility(View.VISIBLE);
                setHeart(h.tvWishlist, wishlistHandler.isWishlisted(p));
                h.tvWishlist.setOnClickListener(v ->
                        setHeart(h.tvWishlist, wishlistHandler.onToggle(p)));
            }
        }
    }

    private void setHeart(TextView tv, boolean on) {
        tv.setText(on ? "★" : "☆");
        tv.setTextColor(Color.parseColor(on ? "#FF5722" : "#9E9E9E"));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvBrand, tvName, tvPrice;
        TextView tvWishlist;   // null nếu item_product.xml chưa có tv_wishlist

        VH(View v) {
            super(v);
            img = v.findViewById(R.id.img_product);
            tvBrand = v.findViewById(R.id.tv_brand);
            tvName = v.findViewById(R.id.tv_name);
            tvPrice = v.findViewById(R.id.tv_price);

            // Tìm theo tên để không lỗi biên dịch khi XML chưa thêm tv_wishlist
            int wid = v.getResources().getIdentifier("tv_wishlist", "id", v.getContext().getPackageName());
            tvWishlist = wid != 0 ? (TextView) v.findViewById(wid) : null;
        }
    }
}