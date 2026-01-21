INSERT INTO users (name, email, password, role) VALUES
	-- 顧客（ログイン ID: customer○@example.com）
	-- パスワード: password
	('顧客 A', 'customerA@example.com', '$2a$10$slYQmyNdGzin7olVN3/p2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUe', 'CUSTOMER'),
	('顧客 B', 'customerB@example.com', '$2a$10$slYQmyNdGzin7olVN3/p2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUe', 'CUSTOMER'),
	('顧客 C', 'customerC@example.com', '$2a$10$slYQmyNdGzin7olVN3/p2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUe', 'CUSTOMER'),
	-- スタッフ（ログイン ID: staff○@example.com）
	-- パスワード: staffpass
	('スタッフ A', 'staffA@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36MxU3dK', 'STAFF'),
	('スタッフ B', 'staffB@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36MxU3dK', 'STAFF'),
	('スタッフ C', 'staffC@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36MxU3dK', 'STAFF'),
	-- 管理者（ログイン ID: admin○@example.com）
	-- パスワード: adminpass
	('管理者 A', 'adminA@example.com', '$2a$10$V9.UD4LS4rm5p0/ptEsPLORu1J2kQwN3cubL9A9cIGNrceBj0a.A', 'ADMIN');
	
INSERT INTO shift (staff_id, record_date, start_time, end_time) VALUES
	((SELECT id FROM users WHERE email = 'staffA@example.com'), CURRENT_DATE, '09:00:00', '17:00:00'),
	((SELECT id FROM users WHERE email = 'staffB@example.com'), CURRENT_DATE, '09:00:00', '17:00:00');