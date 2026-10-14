package com.example.shoestoreapplication.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.shoestoreapplication.models.Product;
import com.example.shoestoreapplication.repository.ShoeRepository;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private final ShoeRepository repo;
    private final MutableLiveData<List<Product>> products = new MutableLiveData<>();
    private final MutableLiveData<String> gender = new MutableLiveData<>("all");
    private String sort = "default";
    private final List<String> selectedBrands = new ArrayList<>();
    private boolean showAll = false;

    public HomeViewModel(@NonNull Application app) {
        super(app);
        repo = ShoeRepository.get(app);
    }

    public LiveData<List<Product>> getProducts() { return products; }
    public LiveData<String> getGender() { return gender; }
    public String getSort() { return sort; }
    public List<String> getSelectedBrands() { return new ArrayList<>(selectedBrands); }

    public void toggleGender(String target) {
        gender.setValue(target.equals(gender.getValue()) ? "all" : target);
        load();
    }

    public void applyFilter(String newSort, List<String> brands) {
        sort = newSort;
        selectedBrands.clear();
        selectedBrands.addAll(brands);
        load();
    }

    public void showAllProducts() { showAll = true; load(); }

    public void load() {
        final String g = gender.getValue();
        final String s = sort;
        final List<String> b = new ArrayList<>(selectedBrands);
        final boolean all = showAll;
        repo.run(() -> {
            List<Product> list = repo.filterProducts(g, b, s);
            boolean filtering = !"all".equals(g) || !b.isEmpty();
            if (!all && !filtering && list.size() > 10) {
                list = new ArrayList<>(list.subList(0, 10));
            }
            products.postValue(list);
        });
    }

    // Wishlist: adapter cần kết quả ngay nên gọi đồng bộ
    public boolean isWishlisted(int userId, int productId) {
        return userId != -1 && repo.isInWishlist(userId, productId);
    }
    public boolean toggleWishlist(int userId, int productId) {
        return userId != -1 && repo.toggleWishlist(userId, productId);
    }
}