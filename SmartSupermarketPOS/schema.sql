CREATE DATABASE IF NOT EXISTS smartpos;
USE smartpos;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'CASHIER') NOT NULL,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock_qty INT NOT NULL DEFAULT 0,
    barcode VARCHAR(100) UNIQUE,
    image_path VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    terminal_id VARCHAR(50),
    employee_id INT,
    item_total DECIMAL(10, 2) NOT NULL,
    gst_amount DECIMAL(10, 2) NOT NULL,
    discount DECIMAL(10, 2) DEFAULT 0,
    final_amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS transaction_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id INT,
    product_id INT,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    line_total DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Seed initial users
INSERT IGNORE INTO users (employee_id, password_hash, role, name) VALUES 
('admin123', 'admin', 'ADMIN', 'System Admin'),
('cashier01', 'cashier', 'CASHIER', 'John Doe');

-- Seed products with images and unique barcodes
INSERT INTO products (id, name, category, price, stock_qty, barcode, image_path) VALUES 
(1,  'Milk 1L',              'Dairy',     65.00,  100, '100000000001', '/images/milk.png'),
(2,  'Bread',                'Groceries', 40.00,  50,  '100000000002', '/images/bread.png'),
(3,  'Lays Magic Masala',    'Snacks',    20.00,  200, '100000000003', '/images/lays.png'),
(4,  'Coca Cola 500ml',      'Beverages', 40.00,  150, '100000000004', '/images/coke.png'),
(5,  'Eggs (12 pcs)',        'Dairy',     85.00,  40,  '100000000005', '/images/eggs.png'),
(6,  'Basmati Rice 5kg',     'Groceries', 450.00, 60,  '100000000006', '/images/rice.png'),
(7,  'Wheat Atta 5kg',       'Groceries', 240.00, 80,  '100000000007', '/images/wheat_atta.png'),
(8,  'Sunflower Oil 1L',     'Groceries', 165.00, 90,  '100000000008', '/images/sunflower_oil.png'),
(9,  'Toor Dal 1kg',         'Groceries', 150.00, 75,  '100000000009', '/images/toor_dal.png'),
(10, 'Refined Sugar 1kg',    'Groceries', 50.00,  120, '100000000010', '/images/sugar.png'),
(11, 'Butter 100g',          'Dairy',     58.00,  85,  '100000000011', '/images/butter.png'),
(12, 'Paneer 200g',          'Dairy',     95.00,  50,  '100000000012', '/images/paneer.png'),
(13, 'Greek Yogurt 200g',    'Dairy',     45.00,  65,  '100000000013', '/images/yogurt.png'),
(14, 'Cheese Slices 200g',   'Dairy',     140.00, 40,  '100000000014', '/images/cheese.png'),
(15, 'Fresh Cream 250ml',    'Dairy',     70.00,  35,  '100000000015', '/images/cream.png'),
(16, 'Oreo Biscuits 120g',   'Snacks',    35.00,  150, '100000000016', '/images/oreo.png'),
(17, 'Dark Chocolate 100g',  'Snacks',    120.00, 90,  '100000000017', '/images/dark_chocolate.png'),
(18, 'Doritos Nachos 100g',  'Snacks',    50.00,  110, '100000000018', '/images/doritos.png'),
(19, 'Roasted Almonds 200g', 'Snacks',    280.00, 45,  '100000000019', '/images/almonds.png'),
(20, 'Haldiram Bhujia 200g', 'Snacks',    60.00,  130, '100000000020', '/images/bhujia.png'),
(21, 'Orange Juice 1L',      'Beverages', 110.00, 70,  '100000000021', '/images/orange_juice_litre.png'),
(22, 'Green Tea 25 Bags',    'Beverages', 180.00, 60,  '100000000022', '/images/green_tea.png'),
(23, 'Instant Coffee 100g',  'Beverages', 290.00, 55,  '100000000023', '/images/coffee.png'),
(24, 'Red Bull 250ml',       'Beverages', 125.00, 100, '100000000024', '/images/red_bull.png'),
(25, 'Sparkling Water 750ml','Beverages', 80.00,  80,  '100000000025', '/images/sparkling_water.png')
ON DUPLICATE KEY UPDATE 
    name=VALUES(name),
    category=VALUES(category),
    price=VALUES(price),
    stock_qty=VALUES(stock_qty),
    image_path=VALUES(image_path);
