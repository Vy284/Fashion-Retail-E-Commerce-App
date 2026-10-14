package com.example.shoestoreapplication.database.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.shoestoreapplication.database.DatabaseHelper;
import com.example.shoestoreapplication.models.Product;
import com.example.shoestoreapplication.models.ProductVariant;

import java.util.ArrayList;
import java.util.List;

public class ProductDao {

    // Hằng số sắp xếp (trước đây nằm trong DatabaseHelper)
    public static final int SORT_NEWEST = 0;
    public static final int SORT_PRICE_ASC = 1;
    public static final int SORT_PRICE_DESC = 2;
    public static final int SORT_NAME_ASC = 3;

    // Cột: 0=product_id, 1=name, 2=base_price, 3=brand_name, 4=image_url (ảnh chính, lấy luôn bằng subquery)
    static final String PRODUCT_COLUMNS =
            "p.product_id, p.name, p.base_price, b.name AS brand_name, " +
                    "COALESCE((SELECT pi.image_url FROM Product_Images pi " +
                    "          WHERE pi.product_id = p.product_id " +
                    "          ORDER BY pi.sort_order, pi.image_id LIMIT 1), '') AS image_url ";

    private static final String PRODUCT_FROM =
            "FROM Products p INNER JOIN Brands b ON p.brand_id = b.brand_id ";

    private final DatabaseHelper helper;

    public ProductDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    // Đọc Cursor (theo PRODUCT_COLUMNS) thành List<Product>, tự đóng cursor
    static List<Product> readProducts(Cursor cursor) {
        List<Product> list = new ArrayList<>();
        try (Cursor c = cursor) {
            while (c.moveToNext()) {
                list.add(new Product(c.getInt(0), c.getString(1), c.getInt(2),
                        c.getString(3), c.getString(4)));
            }
        }
        return list;
    }

    // ---------- Danh sách / tìm kiếm ----------

    public List<Product> getFeaturedProducts() {
        SQLiteDatabase db = helper.getReadableDatabase();
        return readProducts(db.rawQuery(
                "SELECT " + PRODUCT_COLUMNS + PRODUCT_FROM + "LIMIT 10", null));
    }

    public List<Product> searchProducts(String keyword) {
        return filterProducts(keyword, "all", null, "default");
    }

    /**
     * Dùng cho HomeFragment.
     * gender = "all" | "men" | "women" | "unisex" | "kids"; brands = danh sách TÊN (null/rỗng = không lọc)
     * sort = "default" | "price_asc" | "price_desc"
     */
    public List<Product> filterProducts(String keyword, String gender, List<String> brands, String sort) {
        SQLiteDatabase db = helper.getReadableDatabase();
        StringBuilder sql = new StringBuilder("SELECT " + PRODUCT_COLUMNS + PRODUCT_FROM + "WHERE 1=1");
        List<String> args = new ArrayList<>();

        appendKeyword(sql, args, keyword);
        if (gender != null && !gender.equals("all")) {
            sql.append(" AND p.gender_type = ?");
            args.add(gender);
        }
        if (brands != null && !brands.isEmpty()) {
            sql.append(" AND b.name IN (");
            for (int i = 0; i < brands.size(); i++) {
                sql.append(i == 0 ? "?" : ",?");
                args.add(brands.get(i));
            }
            sql.append(")");
        }
        if ("price_asc".equals(sort)) {
            appendSort(sql, SORT_PRICE_ASC);
        } else if ("price_desc".equals(sort)) {
            appendSort(sql, SORT_PRICE_DESC);
        } else {
            sql.append(" ORDER BY p.product_id ASC"); // giữ hành vi cũ của Home
        }
        return readProducts(db.rawQuery(sql.toString(), args.toArray(new String[0])));
    }

    /** Dùng cho màn Category. categoryId/brandId/minPrice/maxPrice <= 0 = không lọc. */
    public List<Product> filterProducts(String keyword, int categoryId, int brandId,
                                        int minPrice, int maxPrice, int sortType) {
        SQLiteDatabase db = helper.getReadableDatabase();
        StringBuilder sql = new StringBuilder("SELECT " + PRODUCT_COLUMNS + PRODUCT_FROM + "WHERE 1=1");
        List<String> args = new ArrayList<>();

        appendKeyword(sql, args, keyword);
        if (categoryId > 0) {
            sql.append(" AND p.category_id = ?");
            args.add(String.valueOf(categoryId));
        }
        if (brandId > 0) {
            sql.append(" AND p.brand_id = ?");
            args.add(String.valueOf(brandId));
        }
        if (minPrice > 0) {
            sql.append(" AND p.base_price >= ?");
            args.add(String.valueOf(minPrice));
        }
        if (maxPrice > 0) {
            sql.append(" AND p.base_price <= ?");
            args.add(String.valueOf(maxPrice));
        }
        appendSort(sql, sortType);
        return readProducts(db.rawQuery(sql.toString(), args.toArray(new String[0])));
    }

    private void appendKeyword(StringBuilder sql, List<String> args, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (p.name LIKE ? OR b.name LIKE ?)");
            String k = "%" + keyword.trim() + "%";
            args.add(k);
            args.add(k);
        }
    }

    private void appendSort(StringBuilder sql, int sortType) {
        switch (sortType) {
            case SORT_PRICE_ASC:
                sql.append(" ORDER BY p.base_price ASC");
                break;
            case SORT_PRICE_DESC:
                sql.append(" ORDER BY p.base_price DESC");
                break;
            case SORT_NAME_ASC:
                sql.append(" ORDER BY p.name COLLATE NOCASE ASC");
                break;
            default:
                sql.append(" ORDER BY p.created_at DESC, p.product_id DESC");
        }
    }

    // ---------- Brand / Category ----------

    /** {id, name} */
    public List<String[]> getBrands() {
        return readIdName("SELECT brand_id, name FROM Brands ORDER BY name");
    }

    /** {id, name} */
    public List<String[]> getCategories() {
        return readIdName("SELECT category_id, name FROM Categories ORDER BY name");
    }

    /** Chỉ tên thương hiệu (cho dialog lọc ở Home). Trước đây: getAllBrands() */
    public List<String> getAllBrands() {
        List<String> names = new ArrayList<>();
        for (String[] row : getBrands()) {
            names.add(row[1]);
        }
        return names;
    }

    private List<String[]> readIdName(String sql) {
        List<String[]> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) {
                list.add(new String[]{String.valueOf(c.getInt(0)), c.getString(1)});
            }
        }
        return list;
    }

    // ---------- Chi tiết sản phẩm ----------

    /** Cột: name, base_price, description, brand_name. Caller phải tự đóng Cursor. */
    public Cursor getProductDetail(int productId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        return db.rawQuery(
                "SELECT p.name, p.base_price, p.description, b.name AS brand_name " +
                        "FROM Products p INNER JOIN Brands b ON p.brand_id = b.brand_id " +
                        "WHERE p.product_id = ?",
                new String[]{String.valueOf(productId)});
    }

    public List<String> getProductImages(int productId) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT image_url FROM Product_Images WHERE product_id = ? ORDER BY sort_order, image_id",
                new String[]{String.valueOf(productId)})) {
            while (c.moveToNext()) {
                list.add(c.getString(0));
            }
        }
        return list;
    }

    public List<ProductVariant> getVariants(int productId) {
        List<ProductVariant> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT variant_id, color, size, stock_quantity, price FROM Product_Variants " +
                        "WHERE product_id = ? ORDER BY variant_id",
                new String[]{String.valueOf(productId)})) {
            while (c.moveToNext()) {
                list.add(new ProductVariant(c.getInt(0), c.getString(1), c.getString(2),
                        c.getInt(3), c.getInt(4)));
            }
        }
        return list;
    }

    public int getDefaultVariantId(int productId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT variant_id FROM Product_Variants WHERE product_id = ? LIMIT 1",
                new String[]{String.valueOf(productId)})) {
            return c.moveToFirst() ? c.getInt(0) : -1;
        }
    }

    // ---------- Ảnh ----------

    public String getFirstImage(int productId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT image_url FROM Product_Images WHERE product_id = ? " +
                        "ORDER BY sort_order, image_id LIMIT 1",
                new String[]{String.valueOf(productId)})) {
            return c.moveToFirst() ? c.getString(0) : "";
        }
    }

    // Giữ lại để code cũ chạy; nếu đã có product_id thì dùng getFirstImage()
    public String getImageByProductName(String productName) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT pi.image_url FROM Product_Images pi " +
                        "JOIN Products p ON pi.product_id = p.product_id " +
                        "WHERE p.name = ? ORDER BY pi.sort_order, pi.image_id LIMIT 1",
                new String[]{productName})) {
            return c.moveToFirst() ? c.getString(0) : "";
        }
    }
}