PRAGMA foreign_keys = ON;

-- ============================================================
-- SHOES RETAIL E-COMMERCE APP - SQLite schema + sample data
-- ============================================================

BEGIN TRANSACTION;

-- -------------------------
-- 1. Users
-- -------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    phone TEXT,
    role TEXT NOT NULL DEFAULT 'CUSTOMER'
        CHECK (role IN ('CUSTOMER', 'ADMIN')),
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------
-- 2. Brands
-- -------------------------
CREATE TABLE IF NOT EXISTS brands (
    brand_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    description TEXT
);

-- -------------------------
-- 3. Categories
-- -------------------------
CREATE TABLE IF NOT EXISTS categories (
    category_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    description TEXT
);

-- -------------------------
-- 4. Products
-- -------------------------
CREATE TABLE IF NOT EXISTS products (
    product_id INTEGER PRIMARY KEY AUTOINCREMENT,
    brand_id INTEGER NOT NULL,
    category_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    base_price REAL NOT NULL CHECK (base_price >= 0),
    status TEXT NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (brand_id) REFERENCES brands(brand_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

-- -------------------------
-- 5. Product variants
-- Each variant represents a size/color/SKU/stock combination
-- -------------------------
CREATE TABLE IF NOT EXISTS product_variants (
    variant_id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL,
    sku TEXT NOT NULL UNIQUE,
    size TEXT NOT NULL,
    color TEXT NOT NULL,
    price REAL NOT NULL CHECK (price >= 0),
    stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    UNIQUE(product_id, size, color)
);

-- -------------------------
-- 6. Product images
-- -------------------------
CREATE TABLE IF NOT EXISTS product_images (
    image_id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL,
    image_url TEXT NOT NULL,
    is_primary INTEGER NOT NULL DEFAULT 0 CHECK (is_primary IN (0, 1)),
    sort_order INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- -------------------------
-- 7. User addresses
-- -------------------------
CREATE TABLE IF NOT EXISTS addresses (
    address_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    recipient_name TEXT NOT NULL,
    phone TEXT NOT NULL,
    address_line TEXT NOT NULL,
    ward TEXT,
    district TEXT,
    city TEXT NOT NULL,
    is_default INTEGER NOT NULL DEFAULT 0 CHECK (is_default IN (0, 1)),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- -------------------------
-- 8. Cart
-- One cart per user
-- -------------------------
CREATE TABLE IF NOT EXISTS carts (
    cart_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL UNIQUE,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- -------------------------
-- 9. Cart items
-- -------------------------
CREATE TABLE IF NOT EXISTS cart_items (
    cart_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    cart_id INTEGER NOT NULL,
    variant_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id),
    UNIQUE(cart_id, variant_id)
);

-- -------------------------
-- 10. Orders
-- -------------------------
CREATE TABLE IF NOT EXISTS orders (
    order_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    address_id INTEGER,
    order_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status TEXT NOT NULL DEFAULT 'PENDING'
        CHECK (status IN (
            'PENDING',
            'CONFIRMED',
            'PROCESSING',
            'SHIPPED',
            'DELIVERED',
            'CANCELLED'
        )),
    subtotal REAL NOT NULL CHECK (subtotal >= 0),
    shipping_fee REAL NOT NULL DEFAULT 0 CHECK (shipping_fee >= 0),
    discount_amount REAL NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
    total_amount REAL NOT NULL CHECK (total_amount >= 0),
    note TEXT,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (address_id) REFERENCES addresses(address_id) ON DELETE SET NULL
);

-- -------------------------
-- 11. Order items
-- Snapshot product name/SKU/price so old orders remain correct
-- even if product information changes later.
-- -------------------------
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    variant_id INTEGER,
    product_name TEXT NOT NULL,
    sku TEXT,
    size TEXT,
    color TEXT,
    unit_price REAL NOT NULL CHECK (unit_price >= 0),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    line_total REAL NOT NULL CHECK (line_total >= 0),
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id) ON DELETE SET NULL
);

-- -------------------------
-- 12. Payments
-- -------------------------
CREATE TABLE IF NOT EXISTS payments (
    payment_id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL UNIQUE,
    method TEXT NOT NULL
        CHECK (method IN ('COD', 'BANK_TRANSFER', 'MOMO', 'VNPAY')),
    status TEXT NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED')),
    transaction_code TEXT,
    paid_at TEXT,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);

-- -------------------------
-- 13. Reviews
-- -------------------------
CREATE TABLE IF NOT EXISTS reviews (
    review_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    order_id INTEGER,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE SET NULL,
    UNIQUE(user_id, product_id, order_id)
);

-- -------------------------
-- 14. Favorites / wishlist
-- -------------------------
CREATE TABLE IF NOT EXISTS favorites (
    favorite_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    UNIQUE(user_id, product_id)
);

-- ============================================================
-- SAMPLE DATA
-- ============================================================

-- Users
INSERT OR IGNORE INTO users
(user_id, full_name, email, password_hash, phone, role)
VALUES
(1, 'Nguyen Van An', 'an@gmail.com', 'demo_hash_123', '0901000001', 'CUSTOMER'),
(2, 'Tran Thi Binh', 'binh@gmail.com', 'demo_hash_456', '0901000002', 'CUSTOMER'),
(3, 'Le Minh Khang', 'khang@gmail.com', 'demo_hash_789', '0901000003', 'CUSTOMER'),
(4, 'Admin Shoes', 'admin@shoes.com', 'admin_demo_hash', '0901000000', 'ADMIN');

-- Brands
INSERT OR IGNORE INTO brands (brand_id, name, description)
VALUES
(1, 'Nike', 'Sportswear and lifestyle footwear'),
(2, 'Adidas', 'Sports and casual footwear'),
(3, 'New Balance', 'Performance and lifestyle footwear'),
(4, 'Converse', 'Classic casual sneakers');

-- Categories
INSERT OR IGNORE INTO categories (category_id, name, description)
VALUES
(1, 'Sneakers', 'Giày sneaker thời trang'),
(2, 'Running', 'Giày chạy bộ'),
(3, 'Basketball', 'Giày bóng rổ'),
(4, 'Casual', 'Giày casual hằng ngày');

-- Products
INSERT OR IGNORE INTO products
(product_id, brand_id, category_id, name, description, base_price, status)
VALUES
(1, 1, 1, 'Nike Air Force 1 Low', 'Giày sneaker cổ điển, phù hợp sử dụng hằng ngày.', 2790000, 'ACTIVE'),
(2, 2, 1, 'Adidas Samba OG', 'Thiết kế retro, đế cao su và phong cách casual.', 2600000, 'ACTIVE'),
(3, 3, 2, 'New Balance 530', 'Giày chạy bộ/lifestyle nhẹ và thoải mái.', 2350000, 'ACTIVE'),
(4, 1, 3, 'Nike Air Jordan 1 Low', 'Giày bóng rổ phong cách low-top.', 3200000, 'ACTIVE'),
(5, 4, 4, 'Converse Chuck Taylor All Star', 'Mẫu sneaker canvas cổ điển.', 1800000, 'ACTIVE');

-- Product variants
INSERT OR IGNORE INTO product_variants
(variant_id, product_id, sku, size, color, price, stock_quantity)
VALUES
(1, 1, 'AF1-WHT-39', '39', 'White', 2790000, 15),
(2, 1, 'AF1-WHT-40', '40', 'White', 2790000, 20),
(3, 1, 'AF1-BLK-41', '41', 'Black', 2850000, 10),
(4, 2, 'SAMBA-BLK-39', '39', 'Black/White', 2600000, 12),
(5, 2, 'SAMBA-WHT-40', '40', 'White/Black', 2600000, 8),
(6, 3, 'NB530-SIL-40', '40', 'Silver', 2350000, 14),
(7, 3, 'NB530-WHT-41', '41', 'White/Grey', 2350000, 11),
(8, 4, 'AJ1-RED-40', '40', 'Red/Black', 3200000, 7),
(9, 4, 'AJ1-BLK-41', '41', 'Black/White', 3200000, 9),
(10, 5, 'CTAS-BLK-39', '39', 'Black', 1800000, 18),
(11, 5, 'CTAS-WHT-40', '40', 'White', 1800000, 16);

-- Product images
INSERT OR IGNORE INTO product_images
(image_id, product_id, image_url, is_primary, sort_order)
VALUES
(1, 1, 'https://images.example.com/nike-af1-white-1.jpg', 1, 1),
(2, 1, 'https://images.example.com/nike-af1-white-2.jpg', 0, 2),
(3, 2, 'https://images.example.com/adidas-samba-black-1.jpg', 1, 1),
(4, 2, 'https://images.example.com/adidas-samba-black-2.jpg', 0, 2),
(5, 3, 'https://images.example.com/nb530-silver-1.jpg', 1, 1),
(6, 4, 'https://images.example.com/air-jordan-1-low-1.jpg', 1, 1),
(7, 5, 'https://images.example.com/converse-ctas-black-1.jpg', 1, 1);

-- Addresses
INSERT OR IGNORE INTO addresses
(address_id, user_id, recipient_name, phone, address_line, ward, district, city, is_default)
VALUES
(1, 1, 'Nguyen Van An', '0901000001', '123 Nguyen Trai', 'Phuong 3', 'Quan 5', 'Ho Chi Minh City', 1),
(2, 2, 'Tran Thi Binh', '0901000002', '45 Le Loi', 'Ben Nghe', 'Quan 1', 'Ho Chi Minh City', 1),
(3, 3, 'Le Minh Khang', '0901000003', '78 Dien Bien Phu', 'Da Kao', 'Quan 1', 'Ho Chi Minh City', 1);

-- Carts
INSERT OR IGNORE INTO carts (cart_id, user_id)
VALUES
(1, 1),
(2, 2),
(3, 3);

-- Cart items
INSERT OR IGNORE INTO cart_items (cart_item_id, cart_id, variant_id, quantity)
VALUES
(1, 1, 2, 1),
(2, 1, 6, 1),
(3, 2, 4, 2),
(4, 3, 10, 1);

-- Orders
INSERT OR IGNORE INTO orders
(order_id, user_id, address_id, order_date, status, subtotal, shipping_fee, discount_amount, total_amount, note)
VALUES
(1, 1, 1, '2026-09-20 10:30:00', 'DELIVERED', 2790000, 30000, 0, 2820000, 'Giao giờ hành chính'),
(2, 2, 2, '2026-09-22 15:10:00', 'SHIPPED', 5200000, 30000, 200000, 5030000, NULL),
(3, 3, 3, '2026-09-24 09:20:00', 'PENDING', 1800000, 30000, 0, 1830000, 'Gọi trước khi giao');

-- Order items
INSERT OR IGNORE INTO order_items
(order_item_id, order_id, variant_id, product_name, sku, size, color, unit_price, quantity, line_total)
VALUES
(1, 1, 2, 'Nike Air Force 1 Low', 'AF1-WHT-40', '40', 'White', 2790000, 1, 2790000),
(2, 2, 4, 'Adidas Samba OG', 'SAMBA-BLK-39', '39', 'Black/White', 2600000, 2, 5200000),
(3, 3, 10, 'Converse Chuck Taylor All Star', 'CTAS-BLK-39', '39', 'Black', 1800000, 1, 1800000);

-- Payments
INSERT OR IGNORE INTO payments
(payment_id, order_id, method, status, transaction_code, paid_at)
VALUES
(1, 1, 'COD', 'PAID', NULL, '2026-09-22 16:00:00'),
(2, 2, 'MOMO', 'PAID', 'MOMO-DEMO-0002', '2026-09-22 15:12:00'),
(3, 3, 'COD', 'PENDING', NULL, NULL);

-- Reviews
INSERT OR IGNORE INTO reviews
(review_id, user_id, product_id, order_id, rating, comment, created_at)
VALUES
(1, 1, 1, 1, 5, 'Giày đẹp, đi rất êm và đúng size.', '2026-09-23 09:00:00'),
(2, 2, 2, 2, 4, 'Form đẹp, đóng gói tốt.', '2026-09-25 14:30:00');

-- Favorites
INSERT OR IGNORE INTO favorites
(favorite_id, user_id, product_id)
VALUES
(1, 1, 4),
(2, 1, 2),
(3, 2, 1),
(4, 3, 3);

COMMIT;

-- ============================================================
-- SAMPLE QUERIES
-- ============================================================

-- Q1. Danh sách sản phẩm + brand + category
SELECT
    p.product_id,
    p.name AS product_name,
    b.name AS brand,
    c.name AS category,
    p.base_price,
    p.status
FROM products p
JOIN brands b ON b.brand_id = p.brand_id
JOIN categories c ON c.category_id = p.category_id
ORDER BY p.product_id;

-- Q2. Danh sách variant còn hàng
SELECT
    p.name AS product_name,
    pv.sku,
    pv.size,
    pv.color,
    pv.price,
    pv.stock_quantity
FROM product_variants pv
JOIN products p ON p.product_id = pv.product_id
WHERE pv.stock_quantity > 0
ORDER BY p.name, pv.size;

-- Q3. Lấy ảnh chính của từng sản phẩm
SELECT
    p.product_id,
    p.name,
    pi.image_url
FROM products p
JOIN product_images pi
    ON pi.product_id = p.product_id
   AND pi.is_primary = 1;

-- Q4. Tìm sản phẩm theo tên
SELECT *
FROM products
WHERE name LIKE '%Nike%';

-- Q5. Sản phẩm theo category
SELECT
    p.product_id,
    p.name,
    p.base_price
FROM products p
JOIN categories c ON c.category_id = p.category_id
WHERE c.name = 'Sneakers';

-- Q6. Chi tiết đơn hàng của user
SELECT
    o.order_id,
    o.order_date,
    o.status,
    oi.product_name,
    oi.size,
    oi.color,
    oi.unit_price,
    oi.quantity,
    oi.line_total
FROM orders o
JOIN order_items oi ON oi.order_id = o.order_id
WHERE o.user_id = 1
ORDER BY o.order_date DESC;

-- Q7. Tổng tiền đơn hàng
SELECT
    o.order_id,
    u.full_name,
    o.status,
    o.subtotal,
    o.shipping_fee,
    o.discount_amount,
    o.total_amount
FROM orders o
JOIN users u ON u.user_id = o.user_id
ORDER BY o.order_date DESC;

-- Q8. Top sản phẩm được mua nhiều nhất
SELECT
    product_name,
    SUM(quantity) AS total_sold
FROM order_items
GROUP BY product_name
ORDER BY total_sold DESC;

-- Q9. Rating trung bình của từng sản phẩm
SELECT
    p.product_id,
    p.name,
    ROUND(AVG(r.rating), 2) AS average_rating,
    COUNT(r.review_id) AS review_count
FROM products p
LEFT JOIN reviews r ON r.product_id = p.product_id
GROUP BY p.product_id, p.name
ORDER BY average_rating DESC;

-- Q10. Kiểm tra tồn kho thấp
SELECT
    p.name,
    pv.sku,
    pv.size,
    pv.color,
    pv.stock_quantity
FROM product_variants pv
JOIN products p ON p.product_id = pv.product_id
WHERE pv.stock_quantity <= 10
ORDER BY pv.stock_quantity ASC;

-- Q11. Giỏ hàng của một user
SELECT
    u.full_name,
    p.name AS product_name,
    pv.sku,
    pv.size,
    pv.color,
    ci.quantity,
    pv.price,
    ci.quantity * pv.price AS item_total
FROM carts c
JOIN users u ON u.user_id = c.user_id
JOIN cart_items ci ON ci.cart_id = c.cart_id
JOIN product_variants pv ON pv.variant_id = ci.variant_id
JOIN products p ON p.product_id = pv.product_id
WHERE u.user_id = 1;

-- Q12. Danh sách sản phẩm user yêu thích
SELECT
    u.full_name,
    p.name AS product_name,
    b.name AS brand,
    p.base_price
FROM favorites f
JOIN users u ON u.user_id = f.user_id
JOIN products p ON p.product_id = f.product_id
JOIN brands b ON b.brand_id = p.brand_id
WHERE f.user_id = 1;
