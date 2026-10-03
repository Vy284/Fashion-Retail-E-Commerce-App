package com.example.shoestoreapplication;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapplication.adapters.ProductAdapter;
import com.example.shoestoreapplication.database.DatabaseHelper;
import com.example.shoestoreapplication.models.Product;
import com.example.shoestoreapplication.utils.Nav;
import com.example.shoestoreapplication.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {

    private DatabaseHelper db;
    private SessionManager session;
    private ProductAdapter adapter;

    private EditText edtSearch;
    private TextView tvCount;

    private String gender = "men";      // men | women | unisex | kids
    private String sort = "default";
    private final List<String> selectedBrands = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new DatabaseHelper(requireContext());
        session = new SessionManager(requireContext());

        edtSearch = view.findViewById(R.id.edt_search);
        tvCount = view.findViewById(R.id.tv_result_count);
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_category);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        RecyclerView rv = view.findViewById(R.id.rv_category_products);
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new ProductAdapter(p ->
                Nav.open(this, ProductDetailFragment.newInstance(p.getId())));
        rv.setAdapter(adapter);

        adapter.setWishlistHandler(new ProductAdapter.WishlistHandler() {
            @Override
            public boolean isWishlisted(Product p) {
                int uid = session.getUserId();
                return uid != -1 && db.isInWishlist(uid, p.getId());
            }

            @Override
            public boolean onToggle(Product p) {
                int uid = session.getUserId();
                return uid != -1 && db.toggleWishlist(uid, p.getId());
            }
        });

        // Chọn Nam / Nữ / Unisex / Trẻ em
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_nam) gender = "men";
            else if (checkedId == R.id.chip_nu) gender = "women";
            else if (checkedId == R.id.chip_unisex) gender = "unisex";
            else if (checkedId == R.id.chip_tre_em) gender = "kids";
            loadProducts();
        });

        // Tìm kiếm ngay khi gõ
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { loadProducts(); }
        });

        view.findViewById(R.id.btn_filter).setOnClickListener(v -> showFilterSheet());

        // Mở sẵn bàn phím để gõ tìm kiếm
        edtSearch.requestFocus();
        edtSearch.post(() -> {
            InputMethodManager imm = (InputMethodManager)
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(edtSearch, InputMethodManager.SHOW_IMPLICIT);
        });

        loadProducts();
    }

    private void loadProducts() {
        List<Product> list = db.filterProducts(
                edtSearch.getText().toString(), gender, selectedBrands, sort);
        adapter.setItems(list);
        tvCount.setText(list.size() + " sản phẩm tìm thấy");
    }

    private void showFilterSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = getLayoutInflater().inflate(R.layout.bottom_sheet_filter, null);

        RadioGroup rgSort = sheet.findViewById(R.id.rg_sort);
        ChipGroup chipGroup = sheet.findViewById(R.id.chip_group_brands);

        if ("price_asc".equals(sort)) rgSort.check(R.id.rb_price_low_high);
        else if ("price_desc".equals(sort)) rgSort.check(R.id.rb_price_high_low);
        else rgSort.check(R.id.rb_default);

        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setCheckable(true);
            chip.setChecked(selectedBrands.contains(chip.getText().toString()));
        }

        sheet.findViewById(R.id.btn_clear).setOnClickListener(v -> {
            rgSort.check(R.id.rb_default);
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                ((Chip) chipGroup.getChildAt(i)).setChecked(false);
            }
        });

        sheet.findViewById(R.id.btn_apply).setOnClickListener(v -> {
            int id = rgSort.getCheckedRadioButtonId();
            if (id == R.id.rb_price_low_high) sort = "price_asc";
            else if (id == R.id.rb_price_high_low) sort = "price_desc";
            else sort = "default";

            selectedBrands.clear();
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                Chip chip = (Chip) chipGroup.getChildAt(i);
                if (chip.isChecked()) selectedBrands.add(chip.getText().toString());
            }
            loadProducts();
            dialog.dismiss();
        });

        dialog.setContentView(sheet);
        dialog.show();
    }
}