-- RA2: Datos de prueba para tests unitarios
-- Datos mínimos y controlados para validar funcionalidad JDBC

INSERT INTO users (id, name, email, department, role, active, created_at, updated_at) VALUES
(1, 'Test User 1', 'test1@example.com', 'IT', 'Developer', true, '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
(2, 'Test User 2', 'test2@example.com', 'HR', 'Manager', true, '2024-01-02 11:00:00', '2024-01-02 11:00:00'),
(3, 'Test User 3', 'test3@example.com', 'IT', 'Analyst', false, '2024-01-03 12:00:00', '2024-01-03 12:00:00');

-- Actualizar la secuencia para que los próximos inserts usen ID >= 4
ALTER TABLE users ALTER COLUMN id RESTART WITH 4;

INSERT INTO user_statistics (user_id, login_count, last_login) VALUES
(1, 10, '2024-02-01 08:00:00'),
(2, 5, '2024-02-02 09:00:00');
