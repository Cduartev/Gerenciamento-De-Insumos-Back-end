-- ============================================================
-- Seed Script — Modelo "Mineração / Vale-like" (DEMO)
-- Banco: PostgreSQL
-- Observação: unidades em KG/L/UN. Preço é fictício (ex.: por tonelada/lote).
-- ============================================================

-- Default USUARIO (password is 'admin123')
INSERT INTO users (name, email, password, role)
VALUES (
           'Administrador Operações',
           'admin@mineracao.com',
           '$2b$10$7549F4FfYtSUHjFYnmTDROmN/hrkLT2h6Xhs4yFdYVfvDL/3TiClC',
           'ADMIN'
       );

-- ============================================================
-- RAW MATERIALS (Insumos / ROM / utilidades)
-- ============================================================
INSERT INTO raw_materials (code, name, stock_quantity, unit_of_measurement)
VALUES
    -- Minérios (ROM / alimentação)
    ('RM-101', 'Minério de Ferro (ROM)',           8000000.0000, 'KILOGRAM'),
    ('RM-102', 'Pellet Feed (Concentrado de Fe)',  2500000.0000, 'KILOGRAM'),
    ('RM-103', 'Minério de Níquel (ROM)',          1200000.0000, 'KILOGRAM'),
    ('RM-104', 'Minério de Cobre (ROM)',            900000.0000, 'KILOGRAM'),

    -- Aditivos / reagentes industriais (seguros p/ demo)
    ('RM-105', 'Calcário (Ajuste/Fluxo)',           300000.0000, 'KILOGRAM'),
    ('RM-106', 'Bentonita (Aglomerante)',            50000.0000, 'KILOGRAM'),

    -- Utilidades / manutenção
    ('RM-107', 'Água Industrial',                  5000000.0000, 'LITER'),
    ('RM-108', 'Diesel (Frota/Operação)',           200000.0000, 'LITER'),
    ('RM-109', 'Óleo Lubrificante Industrial',       20000.0000, 'LITER'),
    ('RM-110', 'Rolamento / Peça de Reposição',        20.00, 'UNIT');

-- ============================================================
-- PRODUCTS (Commodities / produtos típicos)
-- ============================================================
INSERT INTO products (code, name, price)
VALUES
    ('PROD-101', 'Minério de Ferro - Finos (62% Fe)',          480.00),
    ('PROD-102', 'Pelotas de Minério de Ferro',                620.00),
    ('PROD-103', 'Concentrado de Cobre',                      3500.00),
    ('PROD-104', 'Produto de Níquel (Concentrado/Matte)',     8200.00);

-- ============================================================
-- COMPOSITION (BOM / consumo por “lote” de produto)
-- A ideia aqui é: quanto de insumo é necessário para gerar 1000 kg do produto final (demo).
-- ============================================================

-- ------------------------------------------------------------
-- PROD-101: Minério de Ferro - Finos
-- Exemplo: 1500 kg ROM -> 1000 kg finos (perdas/umidade/beneficiamento)
-- ------------------------------------------------------------
INSERT INTO product_composition_items (product_id, raw_material_id, required_quantity)
VALUES
    ((SELECT id FROM products WHERE code = 'PROD-101'), (SELECT id FROM raw_materials WHERE code = 'RM-101'), 1500.0000),
    ((SELECT id FROM products WHERE code = 'PROD-101'), (SELECT id FROM raw_materials WHERE code = 'RM-107'),  200.0000),
    ((SELECT id FROM products WHERE code = 'PROD-101'), (SELECT id FROM raw_materials WHERE code = 'RM-108'),   10.0000),
    ((SELECT id FROM products WHERE code = 'PROD-101'), (SELECT id FROM raw_materials WHERE code = 'RM-109'),    0.5000),
    ((SELECT id FROM products WHERE code = 'PROD-101'), (SELECT id FROM raw_materials WHERE code = 'RM-110'),    0.0100);

-- ------------------------------------------------------------
-- PROD-102: Pelotas de Minério de Ferro
-- Exemplo: 1050 kg pellet feed + 10 kg bentonita + 30 kg calcário + água/diesel
-- ------------------------------------------------------------
INSERT INTO product_composition_items (product_id, raw_material_id, required_quantity)
VALUES
    ((SELECT id FROM products WHERE code = 'PROD-102'), (SELECT id FROM raw_materials WHERE code = 'RM-102'), 1050.0000),
    ((SELECT id FROM products WHERE code = 'PROD-102'), (SELECT id FROM raw_materials WHERE code = 'RM-106'),   10.0000),
    ((SELECT id FROM products WHERE code = 'PROD-102'), (SELECT id FROM raw_materials WHERE code = 'RM-105'),   30.0000),
    ((SELECT id FROM products WHERE code = 'PROD-102'), (SELECT id FROM raw_materials WHERE code = 'RM-107'),   50.0000),
    ((SELECT id FROM products WHERE code = 'PROD-102'), (SELECT id FROM raw_materials WHERE code = 'RM-108'),    5.0000);

-- ------------------------------------------------------------
-- PROD-103: Concentrado de Cobre
-- Exemplo: 5000 kg ROM -> 1000 kg concentrado + água/diesel/lubrificante
-- ------------------------------------------------------------
INSERT INTO product_composition_items (product_id, raw_material_id, required_quantity)
VALUES
    ((SELECT id FROM products WHERE code = 'PROD-103'), (SELECT id FROM raw_materials WHERE code = 'RM-104'), 5000.0000),
    ((SELECT id FROM products WHERE code = 'PROD-103'), (SELECT id FROM raw_materials WHERE code = 'RM-107'), 1000.0000),
    ((SELECT id FROM products WHERE code = 'PROD-103'), (SELECT id FROM raw_materials WHERE code = 'RM-108'),   12.0000),
    ((SELECT id FROM products WHERE code = 'PROD-103'), (SELECT id FROM raw_materials WHERE code = 'RM-109'),    1.0000);

-- ------------------------------------------------------------
-- PROD-104: Produto de Níquel (Concentrado/Matte)
-- Exemplo: 4500 kg ROM -> 1000 kg produto + água/diesel/calcário
-- ------------------------------------------------------------
INSERT INTO product_composition_items (product_id, raw_material_id, required_quantity)
VALUES
    ((SELECT id FROM products WHERE code = 'PROD-104'), (SELECT id FROM raw_materials WHERE code = 'RM-103'), 4500.0000),
    ((SELECT id FROM products WHERE code = 'PROD-104'), (SELECT id FROM raw_materials WHERE code = 'RM-107'),  800.0000),
    ((SELECT id FROM products WHERE code = 'PROD-104'), (SELECT id FROM raw_materials WHERE code = 'RM-108'),   15.0000),
    ((SELECT id FROM products WHERE code = 'PROD-104'), (SELECT id FROM raw_materials WHERE code = 'RM-105'),   40.0000);