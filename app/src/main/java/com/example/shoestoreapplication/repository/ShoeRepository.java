package com.example.shoestoreapplication.repository;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.example.shoestoreapplication.database.dao.CartDao;
import com.example.shoestoreapplication.database.dao.NotificationDao;
import com.example.shoestoreapplication.database.dao.OrderDao;
import com.example.shoestoreapplication.database.dao.ProductDao;
import com.example.shoestoreapplication.database.dao.WishlistDao;
import com.example.shoestoreapplication.models.CartItem;
import com.example.shoestoreapplication.models.NotificationItem;
import com.example.shoestoreapplication.models.Product;
import com.example.shoestoreapplication.models.ProductVariant;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShoeRepository {
    private static final String TAG = "ShoeRepository";

    private static volatile ShoeRepository instance;
    private final NotificationDao notificationDao;
    private final WishlistDao wishlistDao;
    private final ProductDao productDao;
    private final CartDao cartDao;
    private final OrderDao orderDao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    /** Báo lỗi về nơi gọi (thường là ViewModel). */
    public interface ErrorCallback {
        void onError(Exception e);
    }

    private ShoeRepository(Context context) {
        Context app = context.getApplicationContext();
        notificationDao = new NotificationDao(app);
        wishlistDao = new WishlistDao(app);
        productDao = new ProductDao(app);
        cartDao = new CartDao(app);
        orderDao = new OrderDao(app);
    }

    public static ShoeRepository get(Context context) {
        if (instance == null) {
            synchronized (ShoeRepository.class) {
                if (instance == null) instance = new ShoeRepository(context);
            }
        }
        return instance;
    }

    // ----- Luồng nền -----
    /** Chạy tác vụ DB trên luồng nền; exception được ghi log, không làm crash app. */
    public void run(Runnable task) {
        run(task, null);
    }

    public void run(Runnable task, ErrorCallback onError) {
        io.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                Log.e(TAG, "DB task failed", e);
                if (onError != null) onError.onError(e);
            }
        });
    }

    // ----- Sản phẩm -----
    public List<Product> filterProducts(String gender, List<String> brands, String sort) {
        return nonNull(productDao.filterProducts("", gender, brands, sort));
    }

    public List<Product> searchProducts(String keyword) {
        return nonNull(productDao.searchProducts(keyword == null ? "" : keyword.trim()));
    }

    public List<ProductVariant> getVariants(int productId) {
        return nonNull(productDao.getVariants(productId));
    }

    // ----- Wishlist -----
    public boolean isInWishlist(int userId, int productId) {
        return wishlistDao.isInWishlist(userId, productId);
    }

    public boolean toggleWishlist(int userId, int productId) {
        return wishlistDao.toggleWishlist(userId, productId);
    }

    public List<Product> getWishlistProducts(int userId) {
        return nonNull(wishlistDao.getWishlistProducts(userId));
    }

    // ----- Giỏ hàng -----
    public int getOrCreateCartId(int userId) {
        if (userId <= 0) throw new IllegalArgumentException("Invalid userId: " + userId);
        int cartId = cartDao.getOrCreateCartId(userId);
        // CartDao trả -1 khi tạo giỏ thất bại
        if (cartId < 0) throw new IllegalStateException("Cannot create cart for user " + userId);
        return cartId;
    }

    public List<CartItem> getCartItems(int cartId) {
        return nonNull(cartDao.getCartItems(cartId));
    }

    /** Thêm vào giỏ; cùng variant sẽ được cộng dồn số lượng. */
    public boolean addToCart(int cartId, int variantId, int quantity) {
        if (cartId < 0 || variantId <= 0 || quantity < 1) return false;
        return cartDao.addToCart(cartId, variantId, quantity);
    }

    public void updateCartItemQuantity(int cartItemId, int qty) {
        if (qty < 1) throw new IllegalArgumentException("Quantity must be >= 1");
        cartDao.updateCartItemQuantity(cartItemId, qty);
    }

    public boolean deleteCartItem(int cartItemId) {
        return cartDao.deleteCartItem(cartItemId);
    }

    // ----- Đặt hàng (transaction nằm trong OrderDao.placeOrder) -----
    /** @return orderId, hoặc -1 nếu thất bại (hết hàng, lỗi DB...). Đã tự xóa giỏ khi thành công. */
    public long placeOrder(int userId, int cartId, String name, String phone,
                           String address, String paymentMethod, int total) {
        if (userId <= 0 || cartId < 0 || total <= 0) return -1;
        if (isBlank(name) || isBlank(phone) || isBlank(address)) return -1;
        return orderDao.placeOrder(userId, cartId, name.trim(), phone.trim(),
                address.trim(), paymentMethod == null ? "COD" : paymentMethod, total);
    }

    // ----- Thông báo -----
    public List<NotificationItem> getNotifications(int userId) {
        List<NotificationItem> list = new ArrayList<>();
        Cursor cursor = notificationDao.getNotificationsByUserId(userId);
        if (cursor == null) return list;

        try (Cursor c = cursor) {
            int idCol = c.getColumnIndexOrThrow("notification_id");
            int titleCol = c.getColumnIndexOrThrow("title");
            int msgCol = c.getColumnIndexOrThrow("message");
            int timeCol = c.getColumnIndexOrThrow("created_at");
            int readCol = c.getColumnIndexOrThrow("is_read");

            while (c.moveToNext()) {
                list.add(new NotificationItem(
                        c.getInt(idCol),
                        c.getString(titleCol),
                        c.getString(msgCol),
                        c.getString(timeCol),
                        c.getInt(readCol) == 1));
            }
        }
        return list;
    }

    public int getUnreadNotificationCount(int userId) {
        return notificationDao.getUnreadNotificationCount(userId);
    }

    public void markNotificationRead(int id) {
        notificationDao.markNotificationAsRead(id);
    }

    public void markAllNotificationsRead(int userId) {
        notificationDao.markAllNotificationsAsRead(userId);
    }

    // ----- Tiện ích -----
    private static <T> List<T> nonNull(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}