INSERT INTO users (name, email, password, role) VALUES
	-- 顧客（ログイン ID: customer○@example.com）
	('顧客 A', 'customerA@example.com', 'password', 'CUSTOMER'),
	('顧客 B', 'customerB@example.com', 'password', 'CUSTOMER'),
	('顧客 C', 'customerC@example.com', 'password', 'CUSTOMER'),
	-- スタッフ（ログイン ID: staff○@example.com）
	('スタッフ A', 'staffA@example.com', 'staffpass', 'STAFF'),
	('スタッフ B', 'staffB@example.com', 'staffpass', 'STAFF'),
	('スタッフ C', 'staffC@example.com', 'staffpass', 'STAFF'),
	-- 管理者（ログイン ID: admin○@example.com）
	('管理者 A', 'adminA@example.com', 'adminpass', 'ADMIN');
	
INSERT INTO shift (staff_id, record_date, start_time, end_time) VALUES
	((SELECT id FROM users WHERE email = 'staffA@example.com'), CURRENT_DATE, '09:00:00', '17:00:00'),
	((SELECT id FROM users WHERE email = 'staffB@example.com'), CURRENT_DATE, '09:00:00', '17:00:00');