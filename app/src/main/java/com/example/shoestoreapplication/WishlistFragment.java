package com.example.shoestoreapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import java.util.List;

public class WishlistFragment extends Fragment {

    private DatabaseHelper db;
    private SessionManager session;
    private ProductAdapter adapter;
    private RecyclerView rv;
    private View layoutEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wishlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new DatabaseHelper(requireContext());
        session = new SessionManager(requireContext());

        rv = view.findViewById(R.id.rv_wishlist);
        layoutEmpty = view.findViewById(R.id.layout_empty_wishlist);

        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new ProductAdapter(p ->
                Nav.open(this, ProductDetailFragment.newInstance(p.getId())));
        rv.setAdapter(adapter);

        view.findViewById(R.id.btn_explore).setOnClickListener(v ->
                Nav.open(this, new HomeFragment()));
    }

    // Quay lại tab này sau khi bấm tim ở màn chi tiết thì danh sách tự cập nhật
    @Override
    public void onResume() {
        super.onResume();
        List<Product> list = db.getWishlistProducts(session.getUserId());
        adapter.setItems(list);
        rv.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }
}