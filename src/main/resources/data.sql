INSERT INTO users (name, email, password, role) VALUES
	-- 顧客（ログイン ID: customer○@example.com）
	-- パスワード: password
	('顧客 A', 'customerA@example.com', '$2a$10$hmNacI1iYIX48GNChEaT2OATYJYjcZnMHQmxTnKBCQXdoYweNH/j2', 'CUSTOMER'),
	('顧客 B', 'customerB@example.com', '$2a$10$hmNacI1iYIX48GNChEaT2OATYJYjcZnMHQmxTnKBCQXdoYweNH/j2', 'CUSTOMER'),
	('顧客 C', 'customerC@example.com', '$2a$10$hmNacI1iYIX48GNChEaT2OATYJYjcZnMHQmxTnKBCQXdoYweNH/j2', 'CUSTOMER'),
	-- スタッフ（ログイン ID: staff○@example.com）
	-- パスワード: staffpass
	('スタッフ A', 'staffA@example.com', '$2a$10$EGgvH6/BC0kSZ7uR9Ag3YeN3Ooyo486L/vdsOHkwFo3xP3Ue38ME.', 'STAFF'),
	('スタッフ B', 'staffB@example.com', '$2a$10$EGgvH6/BC0kSZ7uR9Ag3YeN3Ooyo486L/vdsOHkwFo3xP3Ue38ME.', 'STAFF'),
	('スタッフ C', 'staffC@example.com', '$2a$10$EGgvH6/BC0kSZ7uR9Ag3YeN3Ooyo486L/vdsOHkwFo3xP3Ue38ME.', 'STAFF'),
	-- 管理者（ログイン ID: admin○@example.com）
	-- パスワード: adminpass
	('管理者 A', 'adminA@example.com', '$2a$10$U6D7tZ/UDOaK9OSST5OHmuPnVuxldZKz7MTUrHZlh4Ldq.6ek31ea', 'ADMIN');
	
INSERT INTO shift (staff_id, record_date, start_time, end_time) VALUES
	((SELECT id FROM users WHERE email = 'staffB@example.com'), CURRENT_DATE + INTERVAL '1 day', '09:00:00', '17:00:00');