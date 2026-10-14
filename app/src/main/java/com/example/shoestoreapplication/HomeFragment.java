package com.example.shoestoreapplication;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoestoreapplication.adapters.ProductAdapter;
import com.example.shoestoreapplication.models.Product;
import com.example.shoestoreapplication.utils.Nav;
import com.example.shoestoreapplication.utils.SessionManager;
import com.example.shoestoreapplication.viewmodel.HomeViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel vm;
    private SessionManager session;
    private ProductAdapter adapter;
    private MaterialButton btnMen, btnWomen;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(this).get(HomeViewModel.class);
        session = new SessionManager(requireContext());

        EditText edtSearch = view.findViewById(R.id.edt_search);
        btnMen = view.findViewById(R.id.btn_category_men);
        btnWomen = view.findViewById(R.id.btn_category_women);
        MaterialButton btnSeeAll = view.findViewById(R.id.btn_see_all);
        View btnFilter = view.findViewById(R.id.btn_filter);
        View btnNotification = view.findViewById(R.id.btn_notification);
        View imgAvatar = view.findViewById(R.id.img_avatar);

        RecyclerView rv = view.findViewById(R.id.rv_featured_products);
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new ProductAdapter(p ->
                Nav.open(this, ProductDetailFragment.newInstance(p.getId())));
        rv.setAdapter(adapter);

        adapter.setWishlistHandler(new ProductAdapter.WishlistHandler() {
            @Override public boolean isWishlisted(Product p) {
                return vm.isWishlisted(session.getUserId(), p.getId());
            }
            @Override public boolean onToggle(Product p) {
                return vm.toggleWishlist(session.getUserId(), p.getId());
            }
        });

        // Quan sát dữ liệu từ ViewModel
        vm.getProducts().observe(getViewLifecycleOwner(), adapter::setItems);
        vm.getGender().observe(getViewLifecycleOwner(), g -> {
            styleGenderButton(btnMen, "men".equals(g));
            styleGenderButton(btnWomen, "women".equals(g));
        });

        edtSearch.setFocusable(false);
        edtSearch.setClickable(true);
        edtSearch.setOnClickListener(v -> Nav.open(this, new CategoryFragment()));

        btnMen.setOnClickListener(v -> vm.toggleGender("men"));
        btnWomen.setOnClickListener(v -> vm.toggleGender("women"));
        btnSeeAll.setOnClickListener(v -> vm.showAllProducts());
        btnFilter.setOnClickListener(v -> showFilterSheet());
        btnNotification.setOnClickListener(v -> Nav.open(this, new NotificationsFragment()));
        imgAvatar.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ProfileActivity.class)));

        vm.load();
    }

    private void styleGenderButton(MaterialButton btn, boolean selected) {
        btn.setBackgroundTintList(ColorStateList.valueOf(
                Color.parseColor(selected ? "#FF5722" : "#F5F5F5")));
        btn.setTextColor(Color.parseColor(selected ? "#FFFFFF" : "#111111"));
    }

    private void showFilterSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = getLayoutInflater().inflate(R.layout.bottom_sheet_filter, null);

        RadioGroup rgSort = sheet.findViewById(R.id.rg_sort);
        ChipGroup chipGroup = sheet.findViewById(R.id.chip_group_brands);

        String sort = vm.getSort();
        List<String> selected = vm.getSelectedBrands();
        if ("price_asc".equals(sort)) rgSort.check(R.id.rb_price_low_high);
        else if ("price_desc".equals(sort)) rgSort.check(R.id.rb_price_high_low);
        else rgSort.check(R.id.rb_default);

        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setCheckable(true);
            chip.setChecked(selected.contains(chip.getText().toString()));
        }

        sheet.findViewById(R.id.btn_clear).setOnClickListener(v -> {
            rgSort.check(R.id.rb_default);
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                ((Chip) chipGroup.getChildAt(i)).setChecked(false);
            }
        });

        sheet.findViewById(R.id.btn_apply).setOnClickListener(v -> {
            int checkedId = rgSort.getCheckedRadioButtonId();
            String newSort = checkedId == R.id.rb_price_low_high ? "price_asc"
                    : checkedId == R.id.rb_price_high_low ? "price_desc" : "default";

            List<String> brands = new ArrayList<>();
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                Chip chip = (Chip) chipGroup.getChildAt(i);
                if (chip.isChecked()) brands.add(chip.getText().toString());
            }
            vm.applyFilter(newSort, brands);
            dialog.dismiss();
        });

        dialog.setContentView(sheet);
        dialog.show();
    }
}