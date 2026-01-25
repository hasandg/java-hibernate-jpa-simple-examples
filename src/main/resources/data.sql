-- ============================================
-- INITIAL DATA FOR ECOMMERCE DATABASE
-- ============================================

-- Insert Tags
INSERT INTO tags (name)
VALUES ('Electronics'),
       ('Clothing'),
       ('Home & Garden'),
       ('Sports'),
       ('Books'),
       ('Toys'),
       ('Fashion'),
       ('Accessories');

-- Insert Categories
INSERT INTO categories (name, description)
VALUES ('Electronics', 'Electronic devices and gadgets'),
       ('Clothing', 'Apparel and fashion items'),
       ('Home & Kitchen', 'Home improvement and kitchen essentials'),
       ('Sports & Outdoors', 'Sports equipment and outdoor gear'),
       ('Books', 'Books and reading materials'),
       ('Toys & Games', 'Toys and games for all ages');

-- Insert Product Images
INSERT INTO product_images (image_url, alt_text, display_order)
VALUES ('https://example.com/images/laptop.jpg', 'Laptop computer', 1),
       ('https://example.com/images/smartphone.jpg', 'Smartphone device', 1),
       ('https://example.com/images/tshirt.jpg', 'Cotton t-shirt', 1),
       ('https://example.com/images/jeans.jpg', 'Blue jeans', 1),
       ('https://example.com/images/coffee-maker.jpg', 'Coffee maker', 1),
       ('https://example.com/images/tennis-racket.jpg', 'Tennis racket', 1),
       ('https://example.com/images/book.jpg', 'Book cover', 1),
       ('https://example.com/images/puzzle.jpg', 'Jigsaw puzzle', 1);

-- Insert Addresses
INSERT INTO addresses (street, city, state, zip_code, country)
VALUES ('123 Main Street', 'New York', 'NY', '10001', 'USA'),
       ('456 Oak Avenue', 'Los Angeles', 'CA', '90001', 'USA'),
       ('789 Pine Road', 'Chicago', 'IL', '60601', 'USA'),
       ('321 Elm Street', 'Houston', 'TX', '77001', 'USA'),
       ('654 Maple Drive', 'Phoenix', 'AZ', '85001', 'USA');

-- Insert Customers
INSERT INTO customers (first_name, last_name, email, phone, address_id)
VALUES ('John', 'Doe', 'john.doe@example.com', '555-0101', 1),
       ('Jane', 'Smith', 'jane.smith@example.com', '555-0102', 2),
       ('Bob', 'Johnson', 'bob.johnson@example.com', '555-0103', 3),
       ('Alice', 'Williams', 'alice.williams@example.com', '555-0104', 4),
       ('Charlie', 'Brown', 'charlie.brown@example.com', '555-0105', 5);

-- Insert Products
INSERT INTO products (name, description, price, stock, category_id, product_image_id)
VALUES ('Laptop Pro 15', 'High-performance laptop with 16GB RAM', 1299.99, 50, 1, 1),
       ('Smartphone X', 'Latest smartphone with advanced camera', 899.99, 100, 1, 2),
       ('Cotton T-Shirt', 'Comfortable cotton t-shirt in various colors', 19.99, 200, 2, 3),
       ('Classic Blue Jeans', 'Durable denim jeans', 49.99, 150, 2, 4),
       ('Coffee Maker Deluxe', 'Programmable coffee maker with timer', 79.99, 75, 3, 5),
       ('Tennis Racket Pro', 'Professional tennis racket', 129.99, 30, 4, 6),
       ('Programming Guide', 'Complete guide to programming', 29.99, 100, 5, 7),
       ('1000 Piece Puzzle', 'Challenging jigsaw puzzle', 14.99, 80, 6, 8);

-- Insert Product Reviews
INSERT INTO product_reviews (reviewer_name, rating, comment, review_date, product_id)
VALUES ('John Reviewer', 5, 'Excellent laptop, very fast and reliable!', '2024-01-15 10:30:00', 1),
       ('Sarah Customer', 4, 'Great phone, camera quality is amazing', '2024-01-20 14:20:00', 2),
       ('Mike User', 5, 'Comfortable shirt, great quality', '2024-02-01 09:15:00', 3),
       ('Lisa Shopper', 4, 'Good jeans, fits well', '2024-02-05 16:45:00', 4),
       ('Tom Buyer', 5, 'Best coffee maker I have ever owned', '2024-02-10 11:00:00', 5),
       ('Emma Player', 4, 'Great racket for intermediate players', '2024-02-15 13:30:00', 6),
       ('David Reader', 5, 'Very comprehensive programming guide', '2024-02-20 10:00:00', 7),
       ('Olivia Puzzle', 4, 'Fun and challenging puzzle', '2024-02-25 15:20:00', 8);

-- Insert Product Tags (Many-to-Many relationship)
INSERT INTO product_tags (product_id, tag_id)
VALUES (1, 1),
       (2, 1),
       (2, 7),
       (3, 2),
       (3, 7),
       (4, 2),
       (4, 7),
       (5, 3),
       (6, 4),
       (7, 5),
       (8, 6);

-- Insert Orders
INSERT INTO orders (order_number, total_amount, status, order_date)
VALUES ('ORD-2024-001', 1299.99, 'DELIVERED', '2024-01-10 10:00:00'),
       ('ORD-2024-002', 969.98, 'SHIPPED', '2024-01-15 14:30:00'),
       ('ORD-2024-003', 69.98, 'PROCESSING', '2024-02-01 09:00:00'),
       ('ORD-2024-004', 159.98, 'PENDING', '2024-02-05 11:20:00'),
       ('ORD-2024-005', 29.99, 'DELIVERED', '2024-02-10 16:45:00');

-- Insert Order Details
INSERT INTO order_details (notes, shipping_method, order_id)
VALUES ('Handle with care', 'Express Shipping', 1),
       ('Gift wrapping requested', 'Standard Shipping', 2),
       ('Leave at front door', 'Standard Shipping', 3),
       ('No special instructions', 'Express Shipping', 4),
       ('Contact customer before delivery', 'Standard Shipping', 5);

-- Insert Order Items
INSERT INTO order_items (quantity, unit_price, order_id, product_id)
VALUES (1, 1299.99, 1, 1),
       (1, 899.99, 2, 2),
       (1, 69.98, 2, 5),
       (2, 19.99, 3, 3),
       (1, 49.99, 3, 4),
       (1, 79.99, 4, 5),
       (1, 79.99, 4, 5),
       (1, 29.99, 5, 7);
