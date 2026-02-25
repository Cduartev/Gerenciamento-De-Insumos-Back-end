-- ============================================================
-- Script de Seed — Gerenciamento de Insumos
-- Banco: PostgreSQL 17
-- ============================================================

-- Matérias-Primas
INSERT INTO materias_primas (codigo, nome, quantidade_estoque, unidade_medida)
VALUES
    ('MP-001', 'Farinha de Trigo',    50.0000, 'QUILOGRAMA'),
    ('MP-002', 'Açúcar Refinado',     30.0000, 'QUILOGRAMA'),
    ('MP-003', 'Chocolate em Pó',     10.0000, 'QUILOGRAMA'),
    ('MP-004', 'Leite Integral',      20.0000, 'LITRO'),
    ('MP-005', 'Ovo',               100.0000, 'UNIDADE'),
    ('MP-006', 'Manteiga',            15.0000, 'QUILOGRAMA'),
    ('MP-007', 'Fermento em Pó',       5.0000, 'QUILOGRAMA');

-- Produtos
INSERT INTO produtos (codigo, nome, valor)
VALUES
    ('PROD-001', 'Bolo de Chocolate',     45.00),
    ('PROD-002', 'Pão Caseiro',           12.00),
    ('PROD-003', 'Brigadeiro (100 un.)',   80.00);

-- Composição: Bolo de Chocolate (PROD-001)
--   2kg farinha, 1kg açúcar, 0.5kg chocolate, 0.5L leite, 4 ovos, 0.25kg manteiga, 0.05kg fermento
INSERT INTO itens_composicao_produto (produto_id, materia_prima_id, quantidade_necessaria)
VALUES
    ((SELECT id FROM produtos WHERE codigo = 'PROD-001'), (SELECT id FROM materias_primas WHERE codigo = 'MP-001'),  2.0000),
    ((SELECT id FROM produtos WHERE codigo = 'PROD-001'), (SELECT id FROM materias_primas WHERE codigo = 'MP-002'),  1.0000),
    ((SELECT id FROM produtos WHERE codigo = 'PROD-001'), (SELECT id FROM materias_primas WHERE codigo = 'MP-003'),  0.5000),
    ((SELECT id FROM produtos WHERE codigo = 'PROD-001'), (SELECT id FROM materias_primas WHERE codigo = 'MP-004'),  0.5000),
    ((SELECT id FROM produtos WHERE codigo = 'PROD-001'), (SELECT id FROM materias_primas WHERE codigo = 'MP-005'),  4.0000),
    ((SELECT id FROM produtos WHERE codigo = 'PROD-001'), (SELECT id FROM materias_primas WHERE codigo = 'MP-006'),  0.2500),
    ((SELECT id FROM produtos WHERE codigo = 'PROD-001'), (SELECT id FROM materias_primas WHERE codigo = 'MP-007'),  0.0500);

-- Composição: Pão Caseiro (PROD-002)
--   1kg farinha, 0.05kg açúcar, 0.3L leite, 1 ovo, 0.1kg manteiga, 0.03kg fermento
INSERT INTO itens_composicao_produto (produto_id, materia_prima_id, quantidade_necessaria)
VALUES
    ((SELECT id FROM produtos WHERE codigo = 'PROD-002'), (SELECT id FROM materias_primas WHERE codigo = 'MP-001'),  1.0000),
    ((SELECT id FROM produtos WHERE codigo = 'PROD-002'), (SELECT id FROM materias_primas WHERE codigo = 'MP-002'),  0.0500),
    ((SELECT id FROM produtos WHERE codigo = 'PROD-002'), (SELECT id FROM materias_primas WHERE codigo = 'MP-004'),  0.3000),
    ((SELECT id FROM produtos WHERE codigo = 'PROD-002'), (SELECT id FROM materias_primas WHERE codigo = 'MP-005'),  1.0000),
    ((SELECT id FROM produtos WHERE codigo = 'PROD-002'), (SELECT id FROM materias_primas WHERE codigo = 'MP-006'),  0.1000),
    ((SELECT id FROM produtos WHERE codigo = 'PROD-002'), (SELECT id FROM materias_primas WHERE codigo = 'MP-007'),  0.0300);

-- Composição: Brigadeiro - 100 unidades (PROD-003)
--   2kg açúcar, 1.5kg chocolate, 2L leite, 0.5kg manteiga
INSERT INTO itens_composicao_produto (produto_id, materia_prima_id, quantidade_necessaria)
VALUES
    ((SELECT id FROM produtos WHERE codigo = 'PROD-003'), (SELECT id FROM materias_primas WHERE codigo = 'MP-002'),  2.0000),
    ((SELECT id FROM produtos WHERE codigo = 'PROD-003'), (SELECT id FROM materias_primas WHERE codigo = 'MP-003'),  1.5000),
    ((SELECT id FROM produtos WHERE codigo = 'PROD-003'), (SELECT id FROM materias_primas WHERE codigo = 'MP-004'),  2.0000),
    ((SELECT id FROM produtos WHERE codigo = 'PROD-003'), (SELECT id FROM materias_primas WHERE codigo = 'MP-006'),  0.5000);
