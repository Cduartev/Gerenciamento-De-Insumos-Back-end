-- ============================================================
-- Seed Script
-- ============================================================

-- Default USUARIO (password is 'admin123')
INSERT INTO users (name, email, password, role) 
VALUES ('Administrador', 'admin@admin.com', '$2a$10$Z/b37bO6bZpYI4r42K0uEuV3SgVn6H2.AIL/0p5iQZ2L3b2J0H2F2', 'ADMIN');

INSERT INTO raw_materials (code, name, stock_quantity, unit_of_measurement)
VALUES
    ('RM-001', 'Farinha de Trigo',    50.0000, 'KILOGRAM'),
    ('RM-002', 'Açúcar Refinado',    30.0000, 'KILOGRAM'),
    ('RM-003', 'Cacau em Pó',        10.0000, 'KILOGRAM'),
    ('RM-004', 'Leite Integral',     20.0000, 'LITER'),
    ('RM-005', 'Ovo',               100.0000, 'UNIT'),
    ('RM-006', 'Manteiga',           15.0000, 'KILOGRAM'),
    ('RM-007', 'Fermento em Pó',      5.0000, 'KILOGRAM');

-- Products (Nomes em Português)
INSERT INTO products (code, name, price)
VALUES
    ('PROD-001', 'Bolo de Chocolate',     45.00),
    ('PROD-002', 'Pão Caseiro',           12.00),
    ('PROD-003', 'Brigadeiro (100 un.)',  80.00);

-- Composition: Bolo de Chocolate (PROD-001)
INSERT INTO product_composition_items (product_id, raw_material_id, required_quantity)
VALUES
    ((SELECT id FROM products WHERE code = 'PROD-001'), (SELECT id FROM raw_materials WHERE code = 'RM-001'),  2.0000),
    ((SELECT id FROM products WHERE code = 'PROD-001'), (SELECT id FROM raw_materials WHERE code = 'RM-002'),  1.0000),
    ((SELECT id FROM products WHERE code = 'PROD-001'), (SELECT id FROM raw_materials WHERE code = 'RM-003'),  0.5000),
    ((SELECT id FROM products WHERE code = 'PROD-001'), (SELECT id FROM raw_materials WHERE code = 'RM-004'),  0.5000),
    ((SELECT id FROM products WHERE code = 'PROD-001'), (SELECT id FROM raw_materials WHERE code = 'RM-005'),  4.0000),
    ((SELECT id FROM products WHERE code = 'PROD-001'), (SELECT id FROM raw_materials WHERE code = 'RM-006'),  0.2500),
    ((SELECT id FROM products WHERE code = 'PROD-001'), (SELECT id FROM raw_materials WHERE code = 'RM-007'),  0.0500);

-- Composition: Pão Caseiro (PROD-002)
INSERT INTO product_composition_items (product_id, raw_material_id, required_quantity)
VALUES
    ((SELECT id FROM products WHERE code = 'PROD-002'), (SELECT id FROM raw_materials WHERE code = 'RM-001'),  1.0000),
    ((SELECT id FROM products WHERE code = 'PROD-002'), (SELECT id FROM raw_materials WHERE code = 'RM-002'),  0.0500),
    ((SELECT id FROM products WHERE code = 'PROD-002'), (SELECT id FROM raw_materials WHERE code = 'RM-004'),  0.3000),
    ((SELECT id FROM products WHERE code = 'PROD-002'), (SELECT id FROM raw_materials WHERE code = 'RM-005'),  1.0000),
    ((SELECT id FROM products WHERE code = 'PROD-002'), (SELECT id FROM raw_materials WHERE code = 'RM-006'),  0.1000),
    ((SELECT id FROM products WHERE code = 'PROD-002'), (SELECT id FROM raw_materials WHERE code = 'RM-007'),  0.0300);

-- Composition: Brigadeiro - 100 units (PROD-003)
INSERT INTO product_composition_items (product_id, raw_material_id, required_quantity)
VALUES
    ((SELECT id FROM products WHERE code = 'PROD-003'), (SELECT id FROM raw_materials WHERE code = 'RM-002'),  2.0000),
    ((SELECT id FROM products WHERE code = 'PROD-003'), (SELECT id FROM raw_materials WHERE code = 'RM-003'),  1.5000),
    ((SELECT id FROM products WHERE code = 'PROD-003'), (SELECT id FROM raw_materials WHERE code = 'RM-004'),  2.0000),
    ((SELECT id FROM products WHERE code = 'PROD-003'), (SELECT id FROM raw_materials WHERE code = 'RM-006'),  0.5000);