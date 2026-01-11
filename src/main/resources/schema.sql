DROP TABLE IF EXISTS reservation CASCADE;
-- 既存テーブル shift を依存関係ごと削除
DROP TABLE IF EXISTS shift CASCADE;
-- 既存テーブル users を依存関係ごと削除
DROP TABLE IF EXISTS users CASCADE;

DROP TABLE IF EXISTS waitlist CASCADE;

CREATE TABLE users (
	id SERIAL PRIMARY KEY,
	-- 表示名（必須）
	name VARCHAR(50) NOT NULL,
	-- ログイン ID としても使うメール（ユニーク制約）
	email VARCHAR(255) UNIQUE,
	-- 認証用パスワード（必須）※開発中は平文、実運用はハッシュ想定
	password VARCHAR(255) NOT NULL,
	-- ロール（ADMIN/STAFF/CUSTOMER など）
	role VARCHAR(20) NOT NULL,
	-- 外部連携用の任意フィールド：LINE ID
	line_id VARCHAR(255),
	-- 外部連携用の任意フィールド：Google トークン
	google_token TEXT
);

CREATE TABLE reservation (
	-- 主キー（連番）
	id SERIAL PRIMARY KEY,
	-- 予約した顧客の FK（NOT NULL）
	user_id INT NOT NULL,
	-- 担当スタッフの FK（未割り当て可）
	staff_id INT,
	-- 予約日（必須）
	record_date DATE NOT NULL,
	-- 予約開始時刻（必須）
	time_slot TIME NOT NULL,
	-- メニュー名（任意）
	menu VARCHAR(255),
	-- ステータス（デフォルトは「予約済」）
	status VARCHAR(20) DEFAULT '予約済',
	-- 顧客 FK 制約（users.id 参照）
	FOREIGN KEY (user_id) REFERENCES users(id),
	-- スタッフ FK 制約（users.id 参照）
	FOREIGN KEY (staff_id) REFERENCES users(id)
);

CREATE TABLE waitlist (
	-- 主キー（連番）
	id SERIAL PRIMARY KEY,
	-- 順番待ちした顧客の FK（必須）
	user_id INT NOT NULL,
	-- 順番待ちの対象スタッフの FK（未割り当て可）
	staff_id INT,
	-- 順番待ち登録日（必須）
	wait_date DATE NOT NULL,
	-- 希望開始時刻（必須）
	start_time TIME NOT NULL,
	-- 希望メニュー名（任意）
	reservation_menu VARCHAR(255),
	-- リクエストのステータス（例：リクエスト中、キャンセル済、案内済）
	request_status VARCHAR(50) DEFAULT 'リクエスト中',
	-- リクエスト日時（必須）
	requested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
	-- 通知日時（顧客へ通知した時刻、NULL可）
	notified_at TIMESTAMP WITH TIME ZONE,
	-- リクエストの有効期限時刻（NULL可）
	expiration_time TIMESTAMP WITH TIME ZONE,
	-- 顧客 FK 制約（users.id 参照）
	FOREIGN KEY (user_id) REFERENCES users(id),
	-- スタッフ FK 制約（users.id 参照）
	FOREIGN KEY (staff_id) REFERENCES users(id)
);

CREATE TABLE shift (
	-- 主キー（連番）
	id SERIAL PRIMARY KEY,
	-- 対象スタッフの FK（必須）
	staff_id INT NOT NULL,
	-- シフト日（必須）
	record_date DATE NOT NULL,
	-- シフト開始時刻（NULL 可：柔軟性確保）
	start_time TIME,
	-- シフト終了時刻（NULL 可）
	end_time TIME,
	-- スタッフ FK 制約（users.id 参照）
	FOREIGN KEY (staff_id) REFERENCES users(id)
);