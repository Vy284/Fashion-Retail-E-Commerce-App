package com.example.shoestoreapplication.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.shoestoreapplication.models.CartItem;
import com.example.shoestoreapplication.repository.ShoeRepository;

import java.util.List;

public class CartViewModel extends AndroidViewModel {
    private final ShoeRepository repo;
    private final MutableLiveData<List<CartItem>> items = new MutableLiveData<>();
    private final MutableLiveData<Integer> total = new MutableLiveData<>();
    private int cartId = -1; // chỉ truy cập trong luồng nền của repo

    public CartViewModel(@NonNull Application app) {
        super(app);
        repo = ShoeRepository.get(app);
    }

    public LiveData<List<CartItem>> getItems() { return items; }
    public LiveData<Integer> getTotal() { return total; }

    public void load(int userId) {
        repo.run(() -> {
            cartId = repo.getOrCreateCartId(userId);
            publish();
        });
    }

    public void changeQuantity(CartItem item, int newQty) {
        repo.run(() -> {
            repo.updateCartItemQuantity(item.getCartItemId(), newQty);
            publish();
        });
    }

    public void remove(CartItem item) {
        repo.run(() -> {
            repo.deleteCartItem(item.getCartItemId());
            publish();
        });
    }

    private void publish() {
        List<CartItem> list = repo.getCartItems(cartId);
        int sum = 0;
        for (CartItem i : list) sum += i.getPrice() * i.getQuantity();
        items.postValue(list);
        total.postValue(sum);
    }
}