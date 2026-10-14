package com.example.shoestoreapplication.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.shoestoreapplication.models.CartItem;
import com.example.shoestoreapplication.repository.ShoeRepository;

import java.util.ArrayList;
import java.util.List;

public class CartViewModel extends AndroidViewModel {

    public static final int MAX_QUANTITY = 10; // có thể thay bằng tồn kho của từng size

    private final ShoeRepository repo;
    private final MutableLiveData<List<CartItem>> items = new MutableLiveData<>();
    private final MutableLiveData<Integer> total = new MutableLiveData<>(0);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private int cartId = -1; // chỉ truy cập trong luồng nền của repo

    public CartViewModel(@NonNull Application app) {
        super(app);
        repo = ShoeRepository.get(app);
    }

    public LiveData<List<CartItem>> getItems() { return items; }
    public LiveData<Integer> getTotal() { return total; }
    public LiveData<String> getError() { return error; }

    /** Gọi sau khi Fragment đã hiển thị lỗi để tránh hiện lại khi xoay màn hình. */
    public void clearError() { error.setValue(null); }

    public void load(int userId) {
        if (userId <= 0) {
            error.setValue("Vui lòng đăng nhập để xem giỏ hàng");
            return;
        }
        repo.run(() -> {
            try {
                cartId = repo.getOrCreateCartId(userId);
                publish();
            } catch (Exception e) {
                error.postValue("Không thể tải giỏ hàng. Vui lòng thử lại");
            }
        });
    }

    public void changeQuantity(CartItem item, int newQty) {
        if (item == null) return;
        if (newQty < 1) {            // dưới 1 thì xóa, Fragment đã hỏi xác nhận
            remove(item);
            return;
        }
        if (newQty > MAX_QUANTITY) {
            error.setValue("Mỗi sản phẩm tối đa " + MAX_QUANTITY + " đôi");
            return;
        }
        repo.run(() -> {
            try {
                repo.updateCartItemQuantity(item.getCartItemId(), newQty);
                publish();
            } catch (Exception e) {
                error.postValue("Không thể cập nhật số lượng");
            }
        });
    }

    public void remove(CartItem item) {
        if (item == null) return;
        repo.run(() -> {
            try {
                repo.deleteCartItem(item.getCartItemId());
                publish();
            } catch (Exception e) {
                error.postValue("Không thể xóa sản phẩm");
            }
        });
    }

    /** Gọi từ CheckoutFragment sau khi tạo đơn hàng thành công. */
    public void clearCart() {
        repo.run(() -> {
            try {
                List<CartItem> list = repo.getCartItems(cartId);
                for (CartItem i : list) repo.deleteCartItem(i.getCartItemId());
                publish();
            } catch (Exception e) {
                error.postValue("Không thể làm trống giỏ hàng");
            }
        });
    }

    // Luôn chạy trong luồng nền của repo
    private void publish() {
        if (cartId < 0) return;      // chưa load xong thì bỏ qua
        List<CartItem> list = repo.getCartItems(cartId);
        if (list == null) list = new ArrayList<>();
        int sum = 0;
        for (CartItem i : list) sum += i.getPrice() * i.getQuantity();
        items.postValue(list);
        total.postValue(sum);
    }
}