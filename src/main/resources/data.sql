INSERT INTO users (name, email, password, role) VALUES
	-- 顧客（ログイン ID: customer○@example.com）
	-- パスワード: password（BCrypt ハッシュ化済み）
	('顧客 A', 'customerA@example.com', '$2a$10$slYQmyNdGzin7olVN3p5aOSvJT2W7SmO8ZwYTW6/VJb0HbOlbKK.a', 'CUSTOMER'),
	('顧客 B', 'customerB@example.com', '$2a$10$slYQmyNdGzin7olVN3p5aOSvJT2W7SmO8ZwYTW6/VJb0HbOlbKK.a', 'CUSTOMER'),
	('顧客 C', 'customerC@example.com', '$2a$10$slYQmyNdGzin7olVN3p5aOSvJT2W7SmO8ZwYTW6/VJb0HbOlbKK.a', 'CUSTOMER'),
	-- スタッフ（ログイン ID: staff○@example.com）
	-- パスワード: staffpass（BCrypt ハッシュ化済み）
	('スタッフ A', 'staffA@example.com', '$2a$10$aJLa9i9EaZ0M0J.VcMTGH.5xkD3gKMXPvYRs5nV5kKm2v8mzKJJcK', 'STAFF'),
	('スタッフ B', 'staffB@example.com', '$2a$10$aJLa9i9EaZ0M0J.VcMTGH.5xkD3gKMXPvYRs5nV5kKm2v8mzKJJcK', 'STAFF'),
	('スタッフ C', 'staffC@example.com', '$2a$10$aJLa9i9EaZ0M0J.VcMTGH.5xkD3gKMXPvYRs5nV5kKm2v8mzKJJcK', 'STAFF'),
	-- 管理者（ログイン ID: admin○@example.com）
	-- パスワード: adminpass（BCrypt ハッシュ化済み）
	('管理者 A', 'adminA@example.com', '$2a$10$6bqzFxTM8Vp1C5X3L9mQvOPkN7qR2S8.H1uJ0aK4mB3dE5fG6hI9S', 'ADMIN');
	
INSERT INTO shift (staff_id, record_date, start_time, end_time) VALUES
	((SELECT id FROM users WHERE email = 'staffA@example.com'), CURRENT_DATE, '09:00:00', '17:00:00'),
	((SELECT id FROM users WHERE email = 'staffB@example.com'), CURRENT_DATE, '09:00:00', '17:00:00');