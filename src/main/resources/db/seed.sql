-- ============================================================
-- Seed Script — Inventory Management
-- Database: PostgreSQL 17
-- ============================================================

-- Raw Materials
INSERT INTO raw_materials (code, name, stock_quantity, unit_of_measurement)
VALUES
    ('RM-001', 'Wheat Flour',         50.0000, 'KILOGRAM'),
    ('RM-002', 'Refined Sugar',       30.0000, 'KILOGRAM'),
    ('RM-003', 'Cocoa Powder',        10.0000, 'KILOGRAM'),
    ('RM-004', 'Whole Milk',          20.0000, 'LITER'),
    ('RM-005', 'Egg',                100.0000, 'UNIT'),
    ('RM-006', 'Butter',              15.0000, 'KILOGRAM'),
    ('RM-007', 'Baking Powder',        5.0000, 'KILOGRAM');

-- Products
INSERT INTO products (code, name, price)
VALUES
    ('PROD-001', 'Chocolate Cake',        45.00),
    ('PROD-002', 'Homemade Bread',        12.00),
    ('PROD-003', 'Brigadeiro (100 un.)',  80.00);

-- Composition: Chocolate Cake (PROD-001)
--   2kg flour, 1kg sugar, 0.5kg cocoa, 0.5L milk, 4 eggs, 0.25kg butter, 0.05kg baking powder
INSERT INTO product_composition_items (product_id, raw_material_id, required_quantity)
VALUES
    ((SELECT id FROM products WHERE code = 'PROD-001'), (SELECT id FROM raw_materials WHERE code = 'RM-001'),  2.0000),
    ((SELECT id FROM products WHERE code = 'PROD-001'), (SELECT id FROM raw_materials WHERE code = 'RM-002'),  1.0000),
    ((SELECT id FROM products WHERE code = 'PROD-001'), (SELECT id FROM raw_materials WHERE code = 'RM-003'),  0.5000),
    ((SELECT id FROM products WHERE code = 'PROD-001'), (SELECT id FROM raw_materials WHERE code = 'RM-004'),  0.5000),
    ((SELECT id FROM products WHERE code = 'PROD-001'), (SELECT id FROM raw_materials WHERE code = 'RM-005'),  4.0000),
    ((SELECT id FROM products WHERE code = 'PROD-001'), (SELECT id FROM raw_materials WHERE code = 'RM-006'),  0.2500),
    ((SELECT id FROM products WHERE code = 'PROD-001'), (SELECT id FROM raw_materials WHERE code = 'RM-007'),  0.0500);

-- Composition: Homemade Bread (PROD-002)
--   1kg flour, 0.05kg sugar, 0.3L milk, 1 egg, 0.1kg butter, 0.03kg baking powder
INSERT INTO product_composition_items (product_id, raw_material_id, required_quantity)
VALUES
    ((SELECT id FROM products WHERE code = 'PROD-002'), (SELECT id FROM raw_materials WHERE code = 'RM-001'),  1.0000),
    ((SELECT id FROM products WHERE code = 'PROD-002'), (SELECT id FROM raw_materials WHERE code = 'RM-002'),  0.0500),
    ((SELECT id FROM products WHERE code = 'PROD-002'), (SELECT id FROM raw_materials WHERE code = 'RM-004'),  0.3000),
    ((SELECT id FROM products WHERE code = 'PROD-002'), (SELECT id FROM raw_materials WHERE code = 'RM-005'),  1.0000),
    ((SELECT id FROM products WHERE code = 'PROD-002'), (SELECT id FROM raw_materials WHERE code = 'RM-006'),  0.1000),
    ((SELECT id FROM products WHERE code = 'PROD-002'), (SELECT id FROM raw_materials WHERE code = 'RM-007'),  0.0300);

-- Composition: Brigadeiro - 100 units (PROD-003)
--   2kg sugar, 1.5kg cocoa, 2L milk, 0.5kg butter
INSERT INTO product_composition_items (product_id, raw_material_id, required_quantity)
VALUES
    ((SELECT id FROM products WHERE code = 'PROD-003'), (SELECT id FROM raw_materials WHERE code = 'RM-002'),  2.0000),
    ((SELECT id FROM products WHERE code = 'PROD-003'), (SELECT id FROM raw_materials WHERE code = 'RM-003'),  1.5000),
    ((SELECT id FROM products WHERE code = 'PROD-003'), (SELECT id FROM raw_materials WHERE code = 'RM-004'),  2.0000),
    ((SELECT id FROM products WHERE code = 'PROD-003'), (SELECT id FROM raw_materials WHERE code = 'RM-006'),  0.5000);
