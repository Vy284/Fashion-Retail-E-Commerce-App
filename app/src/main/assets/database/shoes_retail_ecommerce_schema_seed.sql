-- Disable foreign key constraints during setup
PRAGMA foreign_keys = OFF;

-- Drop tables if they exist
DROP TABLE IF EXISTS Notifications;
DROP TABLE IF EXISTS Order_Items;
DROP TABLE IF EXISTS Payment;
DROP TABLE IF EXISTS Orders;
DROP TABLE IF EXISTS Cart_Items;
DROP TABLE IF EXISTS Carts;
DROP TABLE IF EXISTS Wishlists;
DROP TABLE IF EXISTS Addresses;
DROP TABLE IF EXISTS Product_Images;
DROP TABLE IF EXISTS Product_Variants;
DROP TABLE IF EXISTS Products;
DROP TABLE IF EXISTS Categories;
DROP TABLE IF EXISTS Brands;
DROP TABLE IF EXISTS Users;

-- Enable foreign key constraints
PRAGMA foreign_keys = ON;

-- 1. Users Table
CREATE TABLE Users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    phone TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    avatar_url TEXT,
    created_at TEXT DEFAULT (datetime('now', 'localtime'))
);

-- 2. Brands Table
CREATE TABLE Brands (
    brand_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    logo_url TEXT
);

-- 3. Categories Table
CREATE TABLE Categories (
    category_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    icon_url TEXT,
    gender_type TEXT CHECK(gender_type IN ('men', 'women', 'unisex')) DEFAULT 'unisex'
);

-- 4. Products Table
CREATE TABLE Products (
    product_id INTEGER PRIMARY KEY AUTOINCREMENT,
    category_id INTEGER NOT NULL,
    brand_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    base_price INTEGER NOT NULL,
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (category_id) REFERENCES Categories(category_id) ON DELETE CASCADE,
    FOREIGN KEY (brand_id) REFERENCES Brands(brand_id) ON DELETE CASCADE
);

-- 5. Product_Variants Table
CREATE TABLE Product_Variants (
    variant_id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL,
    color TEXT NOT NULL,
    size TEXT NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    price INTEGER NOT NULL,
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE
);

-- 6. Product_Images Table
CREATE TABLE Product_Images (
    image_id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL,
    image_url TEXT NOT NULL,
    sort_order INTEGER DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE
);

-- 7. Addresses Table
CREATE TABLE Addresses (
    address_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    recipient_name TEXT NOT NULL,
    phone TEXT NOT NULL,
    address_text TEXT NOT NULL,
    is_default INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- 8. Wishlists Table
CREATE TABLE Wishlists (
    wishlist_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    added_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE
);

-- 9. Carts Table
CREATE TABLE Carts (
    cart_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL UNIQUE,
    updated_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- 10. Cart_Items Table
CREATE TABLE Cart_Items (
    cart_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    cart_id INTEGER NOT NULL,
    variant_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (cart_id) REFERENCES Carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES Product_Variants(variant_id) ON DELETE CASCADE
);

-- 11. Orders Table
CREATE TABLE Orders (
    order_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    address_id INTEGER,
    order_code TEXT UNIQUE NOT NULL,
    status TEXT CHECK(status IN ('pending', 'processing', 'shipped', 'delivered', 'cancelled')) DEFAULT 'pending',
    subtotal INTEGER NOT NULL,
    shipping_fee INTEGER NOT NULL DEFAULT 0,
    total INTEGER NOT NULL,
    recipient_name_snapshot TEXT NOT NULL,
    phone_snapshot TEXT NOT NULL,
    address_text_snapshot TEXT NOT NULL,
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (address_id) REFERENCES Addresses(address_id) ON DELETE SET NULL
);

-- 12. Payment Table
CREATE TABLE Payment (
    payment_id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL UNIQUE,
    method TEXT CHECK(method IN ('cod', 'credit_card', 'momo', 'vnpay')) DEFAULT 'cod',
    status TEXT CHECK(status IN ('pending', 'completed', 'failed', 'refunded')) DEFAULT 'pending',
    transaction_date TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE
);

-- 13. Order_Items Table
CREATE TABLE Order_Items (
    order_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    variant_id INTEGER,
    product_name_snapshot TEXT NOT NULL,
    variant_size_snapshot TEXT NOT NULL,
    variant_color_snapshot TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    price_snapshot INTEGER NOT NULL,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES Product_Variants(variant_id) ON DELETE SET NULL
);

-- 14. Notifications Table (Theo ERD)
CREATE TABLE IF NOT EXISTS Notifications (
    notification_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    order_id INTEGER,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    is_read INTEGER DEFAULT 0,
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE SET NULL
);

-- SEED DATA MAU
INSERT INTO Users (full_name, email, phone, password_hash, avatar_url) VALUES
('Phương Loan', 'loan@example.com', '0901234567', '123456', 'avatar_1.png');

INSERT INTO Brands (name, logo_url) VALUES
('Nike', 'brand_nike.png'),
('Adidas', 'brand_adidas.png');

INSERT INTO Categories (name, icon_url, gender_type) VALUES
('Running', 'cat_running.png', 'unisex'),
('Sneakers', 'cat_sneakers.png', 'unisex');

INSERT INTO Products (category_id, brand_id, name, description, base_price) VALUES
(1, 1, 'Nike Air Zoom Pegasus', 'Giày chạy bộ êm ái, thoáng khí.', 2500000);

INSERT INTO Product_Variants (product_id, color, size, stock_quantity, price) VALUES
(1, 'Black', '41', 10, 2500000),
(1, 'White', '42', 5, 2500000);

INSERT INTO Orders (user_id, address_id, order_code, status, subtotal, shipping_fee, total, recipient_name_snapshot, phone_snapshot, address_text_snapshot) VALUES
(1, NULL, 'ORD1001', 'processing', 2500000, 30000, 2530000, 'Phương Loan', '0901234567', 'TP. Hồ Chí Minh');

INSERT INTO Notifications (user_id, order_id, title, message, is_read) VALUES
(1, 1, 'Đơn hàng đã được đặt', 'Cảm ơn bạn đã đặt hàng! Đơn hàng #ORD1001 của bạn đang được xử lý.', 1),
(1, 1, 'Đơn hàng đang giao', 'Đơn hàng #ORD1001 của bạn đã được giao cho đơn vị vận chuyển.', 0);