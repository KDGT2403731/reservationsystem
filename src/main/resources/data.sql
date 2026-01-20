INSERT INTO users (name, email, password, role) VALUES
	-- 顧客（ログイン ID: customer○@example.com / 仮パスワード: password）
	('顧客 A', 'customerA@example.com', '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KnzzVgXXbVxzy5QFZO', 'CUSTOMER'),
	('顧客 B', 'customerB@example.com', '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KnzzVgXXbVxzy5QFZO', 'CUSTOMER'),
	('顧客 C', 'customerC@example.com', '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KnzzVgXXbVxzy5QFZO', 'CUSTOMER'),
	-- スタッフ（ログイン ID: staff○@example.com / 仮パスワード: staffpass）
	('スタッフ A', 'staffA@example.com', '$2a$10$0p5u6ggT29TE1IKg8vkfReJNLxj1WKxVzCkW6Nw5DwhKV0DzXyYmC', 'STAFF'),
	('スタッフ B', 'staffB@example.com', '$2a$10$0p5u6ggT29TE1IKg8vkfReJNLxj1WKxVzCkW6Nw5DwhKV0DzXyYmC', 'STAFF'),
	('スタッフ C', 'staffC@example.com', '$2a$10$0p5u6ggT29TE1IKg8vkfReJNLxj1WKxVzCkW6Nw5DwhKV0DzXyYmC', 'STAFF'),
	-- 管理者（ログイン ID: admin○@example.com / 仮パスワード: adminpass）
	('管理者 A', 'adminA@example.com', '$2a$10$FGLkTw8OHBxHlLVqNRk0TuQB1jNdKVjz2Zz9Q7s5J0Q0Y7fK1vJ4y', 'ADMIN');
	
INSERT INTO shift (staff_id, record_date, start_time, end_time) VALUES
	((SELECT id FROM users WHERE email = 'staffA@example.com'), CURRENT_DATE, '09:00:00', '17:00:00'),
	((SELECT id FROM users WHERE email = 'staffB@example.com'), CURRENT_DATE, '09:00:00', '17:00:00');