-- Categories
INSERT INTO categories (id, name, description, created_at, created_by) 
VALUES ('1', 'Electronics', 'Electronic devices and gadgets', CURRENT_TIMESTAMP, 'system');

-- Users
INSERT INTO users (id, email, phone_num, first_name, last_name, address, weight, height, password, enabled, photo, role, token_version, created_at, created_by) 
VALUES ('1', 'admin@example.com', '1234567890', 'Admin', 'User', '123 Admin St', 70, 175, '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', true, 'https://example.com/photo.jpg', 'ADMIN', 0, CURRENT_TIMESTAMP, 'system');

-- Products (now category_id exists)
INSERT INTO products (id, name, description, cost, total, price, discount_percent, enable, in_stock, main_image_url, average_rating, review_count, created_time, update_time, created_at, created_by, category_id) 
VALUES ('1', 'Smartphone X', 'Latest smartphone with amazing features', 400.00, 50, 599.99, 10.0, true, true, 'https://example.com/smartphone.jpg', 4.5, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', '1');

-- Orders
INSERT INTO orders (id, first_name, last_name, phone_number, status, delivery_date, order_date, payment_method, shipping_cost, product_cost, sub_total, total, customer_id, created_at, created_by)
VALUES ('1', 'John', 'Doe', '+1234567890', 'COMPLETED', CURRENT_TIMESTAMP + INTERVAL '7 days', CURRENT_TIMESTAMP, 'COD', 10.0, 599.99, 599.99, 609.99, '1', CURRENT_TIMESTAMP, 'system');

-- Reviews
INSERT INTO review (id, comment, headline, rating, order_id, created_at, created_by)
VALUES ('1', 'Great product, arrived on time!', 'Excellent purchase', 5, '1', CURRENT_TIMESTAMP, 'system');

-- Update sequences after insertions
SELECT setval('product_color_sizes_seq', (SELECT MAX(id::integer) FROM product_color_sizes), false);
SELECT setval('products_seq', (SELECT MAX(id::integer) FROM products), false);