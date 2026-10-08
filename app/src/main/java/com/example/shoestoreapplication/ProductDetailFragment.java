package com.example.shoestoreapplication;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.shoestoreapplication.database.DatabaseHelper;
import com.example.shoestoreapplication.models.ProductVariant;
import com.example.shoestoreapplication.utils.ImageUtils;
import com.example.shoestoreapplication.utils.Money;
import com.example.shoestoreapplication.utils.SessionManager;
import android.content.res.ColorStateList;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ProductDetailFragment extends Fragment {

    private static final String ARG_ID = "product_id";

    public static ProductDetailFragment newInstance(int productId) {
        ProductDetailFragment f = new ProductDetailFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_ID, productId);
        f.setArguments(b);
        return f;
    }

    private DatabaseHelper db;
    private int productId;
    private int userId;

    private List<ProductVariant> variants = new ArrayList<>();
    private String selectedColor = null;
    private String selectedSize = null;
    private int qty = 1;

    private ImageView imgMain;
    private ImageButton btnFavorite;
    private LinearLayout llThumbs, llColors, llSizes;
    private TextView tvQuantity, tvStock;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new DatabaseHelper(requireContext());
        userId = new SessionManager(requireContext()).getUserId();
        productId = requireArguments().getInt(ARG_ID);

        imgMain = view.findViewById(R.id.img_product_main);
        btnFavorite = view.findViewById(R.id.btn_favorite);
        llThumbs = view.findViewById(R.id.ll_thumbs);
        llColors = view.findViewById(R.id.ll_colors);
        llSizes = view.findViewById(R.id.ll_sizes);
        tvQuantity = view.findViewById(R.id.tv_quantity);
        tvStock = view.findViewById(R.id.tv_stock);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // Thông tin chính
        try (Cursor c = db.getProductDetail(productId)) {
            if (c.moveToFirst()) {
                ((TextView) view.findViewById(R.id.tv_product_name)).setText(c.getString(0));
                ((TextView) view.findViewById(R.id.tv_product_price)).setText(Money.vnd(c.getInt(1)));
                ((TextView) view.findViewById(R.id.tv_product_description)).setText(c.getString(2));
                ((TextView) view.findViewById(R.id.tv_product_brand)).setText(c.getString(3).toUpperCase());
            }
        }

        // Ảnh chính + ảnh nhỏ
        List<String> images = db.getProductImages(productId);
        ImageUtils.load(imgMain, images.isEmpty() ? null : images.get(0));
        llThumbs.removeAllViews();
        if (images.size() > 1) {
            for (String name : images) {
                ImageView t = new ImageView(requireContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(80), dp(80));
                lp.setMarginEnd(dp(12));
                t.setLayoutParams(lp);
                t.setScaleType(ImageView.ScaleType.CENTER_CROP);
                t.setBackgroundColor(Color.parseColor("#F0F0F0"));
                ImageUtils.load(t, name);
                t.setOnClickListener(v -> ImageUtils.load(imgMain, name));
                llThumbs.addView(t);
            }
        } else {
            llThumbs.setVisibility(View.GONE);
        }

        // Màu và size
        variants = db.getVariants(productId);
        if (!variants.isEmpty()) selectedColor = variants.get(0).color;
        renderColors();
        renderSizes();

        // Yêu thích
        renderHeart();
        btnFavorite.setOnClickListener(v -> {
            if (userId == -1) return;
            boolean now = db.toggleWishlist(userId, productId);
            renderHeart();
            Toast.makeText(requireContext(),
                    now ? "Đã thêm vào yêu thích" : "Đã bỏ khỏi yêu thích", Toast.LENGTH_SHORT).show();
        });

        // Số lượng
        view.findViewById(R.id.btn_decrease).setOnClickListener(v -> {
            if (qty > 1) {
                qty--;
                tvQuantity.setText(String.valueOf(qty));
            }
        });
        view.findViewById(R.id.btn_increase).setOnClickListener(v -> {
            ProductVariant pv = findVariant(selectedColor, selectedSize);
            if (pv == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn size trước", Toast.LENGTH_SHORT).show();
            } else if (qty < pv.stock) {
                qty++;
                tvQuantity.setText(String.valueOf(qty));
            } else {
                Toast.makeText(requireContext(), "Chỉ còn " + pv.stock + " sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.btn_add_to_cart).setOnClickListener(v -> addToCart());
    }

    // ---------- vẽ giao diện ----------
    private void renderHeart() {
        boolean liked = userId != -1 && db.isInWishlist(userId, productId);
        btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
        btnFavorite.setImageTintList(ColorStateList.valueOf(
                Color.parseColor(liked ? "#FF5722" : "#BDBDBD")));
    }

    private void renderColors() {
        llColors.removeAllViews();
        Set<String> colors = new LinkedHashSet<>();
        for (ProductVariant v : variants) colors.add(v.color);

        for (String color : colors) {
            boolean selected = color.equals(selectedColor);
            View swatch = new View(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(56), dp(56));
            lp.setMarginEnd(dp(16));
            swatch.setLayoutParams(lp);

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(dp(12));
            bg.setColor(colorOf(color));
            bg.setStroke(selected ? dp(4) : dp(1),
                    selected ? Color.parseColor("#FF5722") : Color.parseColor("#DDDDDD"));
            swatch.setBackground(bg);
            swatch.setContentDescription(color);

            swatch.setOnClickListener(v -> {
                selectedColor = color;
                selectedSize = null;
                qty = 1;
                tvQuantity.setText("1");
                renderColors();
                renderSizes();
            });
            llColors.addView(swatch);
        }
    }

    private void renderSizes() {
        llSizes.removeAllViews();
        tvStock.setText("");

        for (ProductVariant pv : variants) {
            if (!pv.color.equals(selectedColor)) continue;

            boolean inStock = pv.stock > 0;
            boolean selected = pv.size.equals(selectedSize);

            TextView chip = new TextView(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(64), dp(56));
            lp.setMarginEnd(dp(12));
            chip.setLayoutParams(lp);
            chip.setGravity(Gravity.CENTER);
            chip.setText(pv.size);
            chip.setTextSize(20);
            chip.setTypeface(null, android.graphics.Typeface.BOLD);
            chip.setTextColor(selected ? Color.WHITE : Color.BLACK);

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(dp(12));
            bg.setColor(Color.parseColor(selected ? "#111111" : "#EEEEEE"));
            chip.setBackground(bg);

            chip.setAlpha(inStock ? 1f : 0.35f);   // hết hàng thì mờ đi
            if (inStock) {
                chip.setOnClickListener(v -> {
                    selectedSize = pv.size;
                    qty = 1;
                    tvQuantity.setText("1");
                    renderSizes();
                });
            }
            llSizes.addView(chip);
        }

        ProductVariant sel = findVariant(selectedColor, selectedSize);
        if (sel != null) tvStock.setText("Còn " + sel.stock + " sản phẩm");
    }

    // ---------- logic ----------
    private ProductVariant findVariant(String color, String size) {
        if (color == null || size == null) return null;
        for (ProductVariant v : variants) {
            if (v.color.equals(color) && v.size.equals(size)) return v;
        }
        return null;
    }

    private void addToCart() {
        if (userId == -1) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedColor == null || selectedSize == null) {
            Toast.makeText(requireContext(), "Vui lòng chọn màu và size", Toast.LENGTH_SHORT).show();
            return;
        }
        ProductVariant pv = findVariant(selectedColor, selectedSize);
        if (pv == null || pv.stock < qty) {
            Toast.makeText(requireContext(), "Sản phẩm không đủ hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        int cartId = db.getOrCreateCartId(userId);
        boolean ok = db.addToCart(cartId, pv.id, qty);
        Toast.makeText(requireContext(),
                ok ? "Đã thêm vào giỏ hàng" : "Thêm vào giỏ thất bại", Toast.LENGTH_SHORT).show();
    }

    // Tên màu trong DB -> mã màu hiển thị. Thêm màu mới thì thêm 1 dòng case.
    private int colorOf(String name) {
        switch (name.trim().toLowerCase()) {
            case "red":   return Color.parseColor("#E53935");
            case "black": return Color.parseColor("#000000");
            case "blue":  return Color.parseColor("#1E5BFF");
            case "grey":
            case "gray":  return Color.parseColor("#888888");
            case "white": return Color.parseColor("#FFFFFF");
            case "volt":  return Color.parseColor("#CEFF00");
            default:      return Color.parseColor("#BDBDBD");
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}