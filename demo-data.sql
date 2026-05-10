-- =============================================================
-- DEMO DATA — Orderly Analytics
-- Inserta datos de Mayo 2026 y Abril 2026 para probar Analytics
-- Ejecutar en psql o pgAdmin contra db_orderly
-- Los IDs empiezan en 9000 para evitar conflictos con datos reales
-- =============================================================

-- -----------------------------------------------
-- CASH SESSIONS (cierres de turno)
-- -----------------------------------------------
INSERT INTO cash_sessions (id, business_date, shift_no, opened_at, closed_at,
    cash_start, cash_end_expected, cash_end_actual, difference,
    total_sales_cash, total_sales_card, status)
VALUES
-- Abril 2026
(9001, '2026-04-05', 1, '2026-04-05 09:00:00', '2026-04-05 17:30:00',
    200.00, 820.00, 815.00, -5.00, 620.00, 780.00, 'CLOSED'),
(9002, '2026-04-12', 1, '2026-04-12 09:00:00', '2026-04-12 18:00:00',
    200.00, 760.00, 762.00, 2.00, 560.00, 650.00, 'CLOSED'),
(9003, '2026-04-20', 1, '2026-04-20 09:15:00', '2026-04-20 17:45:00',
    200.00, 890.00, 888.00, -2.00, 690.00, 920.00, 'CLOSED'),

-- Mayo 2026
(9004, '2026-05-03', 1, '2026-05-03 09:00:00', '2026-05-03 18:00:00',
    200.00, 1250.00, 1248.00, -2.00, 1050.00, 1400.00, 'CLOSED'),
(9005, '2026-05-07', 1, '2026-05-07 09:00:00', '2026-05-07 17:30:00',
    200.00, 980.00, 982.00, 2.00, 780.00, 1100.00, 'CLOSED'),
(9006, '2026-05-10', 1, '2026-05-10 09:00:00', '2026-05-10 17:00:00',
    200.00, 1100.00, 1098.00, -2.00, 900.00, 1250.00, 'CLOSED')
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------
-- ORDERS (pedidos cerrados/pagados)
-- -----------------------------------------------
INSERT INTO orders (id, datetime, state, payment_method, total,
    id_employee, id_client, id_restable)
VALUES
-- Abril 2026 — 9 pedidos
(9101, '2026-04-05 10:30:00', 'PAID', 'CASH',   42.50,  NULL, NULL, NULL),
(9102, '2026-04-05 12:45:00', 'PAID', 'CARD',   67.80,  NULL, NULL, NULL),
(9103, '2026-04-05 14:20:00', 'PAID', 'CASH',   35.00,  NULL, NULL, NULL),
(9104, '2026-04-12 11:00:00', 'PAID', 'CARD',   89.50,  NULL, NULL, NULL),
(9105, '2026-04-12 13:30:00', 'PAID', 'CASH',   55.20,  NULL, NULL, NULL),
(9106, '2026-04-12 15:45:00', 'PAID', 'CARD',   48.00,  NULL, NULL, NULL),
(9107, '2026-04-20 12:00:00', 'PAID', 'CASH',   73.40,  NULL, NULL, NULL),
(9108, '2026-04-20 14:00:00', 'PAID', 'CARD',   91.20,  NULL, NULL, NULL),
(9109, '2026-04-20 16:30:00', 'PAID', 'CARD',   62.00,  NULL, NULL, NULL),

-- Mayo 2026 — 18 pedidos
(9201, '2026-05-03 10:00:00', 'PAID', 'CASH',   58.50,  NULL, NULL, NULL),
(9202, '2026-05-03 11:30:00', 'PAID', 'CARD',   84.20,  NULL, NULL, NULL),
(9203, '2026-05-03 13:00:00', 'PAID', 'CASH',   45.80,  NULL, NULL, NULL),
(9204, '2026-05-03 14:30:00', 'PAID', 'CARD',   112.00, NULL, NULL, NULL),
(9205, '2026-05-03 16:00:00', 'PAID', 'CASH',   39.50,  NULL, NULL, NULL),
(9206, '2026-05-03 17:00:00', 'PAID', 'CARD',   76.30,  NULL, NULL, NULL),
(9207, '2026-05-07 10:30:00', 'PAID', 'CARD',   95.60,  NULL, NULL, NULL),
(9208, '2026-05-07 12:00:00', 'PAID', 'CASH',   67.40,  NULL, NULL, NULL),
(9209, '2026-05-07 13:30:00', 'PAID', 'CARD',   53.80,  NULL, NULL, NULL),
(9210, '2026-05-07 15:00:00', 'PAID', 'CASH',   88.20,  NULL, NULL, NULL),
(9211, '2026-05-07 16:30:00', 'PAID', 'CARD',   41.50,  NULL, NULL, NULL),
(9212, '2026-05-10 09:30:00', 'PAID', 'CASH',   72.80,  NULL, NULL, NULL),
(9213, '2026-05-10 11:00:00', 'PAID', 'CARD',   118.50, NULL, NULL, NULL),
(9214, '2026-05-10 12:30:00', 'PAID', 'CASH',   49.20,  NULL, NULL, NULL),
(9215, '2026-05-10 13:45:00', 'PAID', 'CARD',   85.70,  NULL, NULL, NULL),
(9216, '2026-05-10 14:30:00', 'PAID', 'CASH',   63.10,  NULL, NULL, NULL),
(9217, '2026-05-10 15:30:00', 'PAID', 'CARD',   94.40,  NULL, NULL, NULL),
(9218, '2026-05-10 16:45:00', 'PAID', 'CARD',   57.90,  NULL, NULL, NULL)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------
-- ORDER DETAILS (para top productos)
-- paid=true + id_cash_session SET → aparecen en analytics
-- -----------------------------------------------
INSERT INTO order_details (id, name, id_order, id_cash_session, comment,
    amount, unit_price, created_at, status, payment_method, paid, batch_id)
VALUES
-- Abril 2026 (sesiones 9001-9003)
(9301, 'Café con leche',      9101, 9001, NULL, 2, 2.20,  '2026-04-05 10:30:00', 'SERVED', 'CASH', true, NULL),
(9302, 'Tostada con tomate',  9101, 9001, NULL, 2, 4.50,  '2026-04-05 10:30:00', 'SERVED', 'CASH', true, NULL),
(9303, 'Chuletón',            9102, 9001, NULL, 1, 28.00, '2026-04-05 12:45:00', 'SERVED', 'CARD', true, NULL),
(9304, 'Ensalada mixta',      9102, 9001, NULL, 2, 8.50,  '2026-04-05 12:45:00', 'SERVED', 'CARD', true, NULL),
(9305, 'Cerveza',             9103, 9001, NULL, 3, 2.50,  '2026-04-05 14:20:00', 'SERVED', 'CASH', true, NULL),
(9306, 'Gambas al ajillo',    9104, 9002, NULL, 1, 14.50, '2026-04-12 11:00:00', 'SERVED', 'CARD', true, NULL),
(9307, 'Cerveza',             9104, 9002, NULL, 2, 2.50,  '2026-04-12 11:00:00', 'SERVED', 'CARD', true, NULL),
(9308, 'Tortilla española',   9105, 9002, NULL, 2, 9.80,  '2026-04-12 13:30:00', 'SERVED', 'CASH', true, NULL),
(9309, 'Café con leche',      9106, 9002, NULL, 3, 2.20,  '2026-04-12 15:45:00', 'SERVED', 'CARD', true, NULL),
(9310, 'Chuletón',            9107, 9003, NULL, 2, 28.00, '2026-04-20 12:00:00', 'SERVED', 'CASH', true, NULL),
(9311, 'Cerveza',             9108, 9003, NULL, 4, 2.50,  '2026-04-20 14:00:00', 'SERVED', 'CARD', true, NULL),
(9312, 'Gambas al ajillo',    9109, 9003, NULL, 1, 14.50, '2026-04-20 16:30:00', 'SERVED', 'CARD', true, NULL),

-- Mayo 2026 (sesiones 9004-9006)
(9401, 'Café con leche',      9201, 9004, NULL, 2, 2.20,  '2026-05-03 10:00:00', 'SERVED', 'CASH', true, NULL),
(9402, 'Cerveza',             9201, 9004, NULL, 3, 2.50,  '2026-05-03 10:00:00', 'SERVED', 'CASH', true, NULL),
(9403, 'Chuletón',            9202, 9004, NULL, 1, 28.00, '2026-05-03 11:30:00', 'SERVED', 'CARD', true, NULL),
(9404, 'Tortilla española',   9202, 9004, NULL, 2, 9.80,  '2026-05-03 11:30:00', 'SERVED', 'CARD', true, NULL),
(9405, 'Gambas al ajillo',    9203, 9004, NULL, 2, 14.50, '2026-05-03 13:00:00', 'SERVED', 'CASH', true, NULL),
(9406, 'Ensalada mixta',      9204, 9004, NULL, 3, 8.50,  '2026-05-03 14:30:00', 'SERVED', 'CARD', true, NULL),
(9407, 'Cerveza',             9204, 9004, NULL, 5, 2.50,  '2026-05-03 14:30:00', 'SERVED', 'CARD', true, NULL),
(9408, 'Café con leche',      9205, 9004, NULL, 1, 2.20,  '2026-05-03 16:00:00', 'SERVED', 'CASH', true, NULL),
(9409, 'Chuletón',            9206, 9004, NULL, 1, 28.00, '2026-05-03 17:00:00', 'SERVED', 'CARD', true, NULL),
(9410, 'Cerveza',             9207, 9005, NULL, 4, 2.50,  '2026-05-07 10:30:00', 'SERVED', 'CARD', true, NULL),
(9411, 'Tortilla española',   9207, 9005, NULL, 2, 9.80,  '2026-05-07 10:30:00', 'SERVED', 'CARD', true, NULL),
(9412, 'Gambas al ajillo',    9208, 9005, NULL, 1, 14.50, '2026-05-07 12:00:00', 'SERVED', 'CASH', true, NULL),
(9413, 'Café con leche',      9208, 9005, NULL, 3, 2.20,  '2026-05-07 12:00:00', 'SERVED', 'CASH', true, NULL),
(9414, 'Chuletón',            9209, 9005, NULL, 2, 28.00, '2026-05-07 13:30:00', 'SERVED', 'CARD', true, NULL),
(9415, 'Ensalada mixta',      9210, 9005, NULL, 2, 8.50,  '2026-05-07 15:00:00', 'SERVED', 'CASH', true, NULL),
(9416, 'Cerveza',             9211, 9005, NULL, 3, 2.50,  '2026-05-07 16:30:00', 'SERVED', 'CARD', true, NULL),
(9417, 'Café con leche',      9212, 9006, NULL, 2, 2.20,  '2026-05-10 09:30:00', 'SERVED', 'CASH', true, NULL),
(9418, 'Gambas al ajillo',    9212, 9006, NULL, 2, 14.50, '2026-05-10 09:30:00', 'SERVED', 'CASH', true, NULL),
(9419, 'Chuletón',            9213, 9006, NULL, 1, 28.00, '2026-05-10 11:00:00', 'SERVED', 'CARD', true, NULL),
(9420, 'Cerveza',             9213, 9006, NULL, 6, 2.50,  '2026-05-10 11:00:00', 'SERVED', 'CARD', true, NULL),
(9421, 'Tortilla española',   9214, 9006, NULL, 3, 9.80,  '2026-05-10 12:30:00', 'SERVED', 'CASH', true, NULL),
(9422, 'Ensalada mixta',      9215, 9006, NULL, 1, 8.50,  '2026-05-10 13:45:00', 'SERVED', 'CARD', true, NULL),
(9423, 'Gambas al ajillo',    9216, 9006, NULL, 3, 14.50, '2026-05-10 14:30:00', 'SERVED', 'CASH', true, NULL),
(9424, 'Cerveza',             9217, 9006, NULL, 4, 2.50,  '2026-05-10 15:30:00', 'SERVED', 'CARD', true, NULL),
(9425, 'Café con leche',      9218, 9006, NULL, 2, 2.20,  '2026-05-10 16:45:00', 'SERVED', 'CARD', true, NULL)
ON CONFLICT (id) DO NOTHING;

-- =============================================================
-- RESUMEN ESPERADO EN LA APP:
-- Mayo 2026: ingresos 5.480 € (cash 2.730 € + card 3.750 €)
--            18 pedidos, ticket medio ~304 €
-- Top 5: Cerveza 25, Café con leche 10, Chuletón 5,
--         Gambas 8, Tortilla 7, Ensalada 6
--
-- Abril 2026 (comparativo mes anterior):
--            ingresos 3.870 € (cash 1.870 € + card 2.350 €)
--            9 pedidos
-- =============================================================
