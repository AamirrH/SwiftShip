INSERT INTO orders (id, order_status, price) VALUES
(1, 'CONFIRMED', 3647.00),
(2, 'PENDING', 5998.00),
(3, 'DELIVERED', 10798.00),
(4, 'CANCELLED', 1299.00),
(5, 'CONFIRMED', 8298.00)
ON CONFLICT (id) DO NOTHING;

INSERT INTO order_item (id, product_id, quantity, order_id) VALUES
(1, 1, 1, 1),
(2, 3, 1, 1),
(3, 5, 2, 1),
(4, 4, 3, 2),
(5, 10, 1, 3),
(6, 8, 1, 3),
(7, 9, 1, 3),
(8, 5, 1, 4),
(9, 7, 1, 5),
(10, 1, 1, 5)
ON CONFLICT (id) DO NOTHING;

SELECT setval('orders_id_seq', COALESCE((SELECT MAX(id) FROM orders), 1));
SELECT setval('order_item_id_seq', COALESCE((SELECT MAX(id) FROM order_item), 1));
