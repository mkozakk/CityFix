-- Database is already created by docker-compose (POSTGRES_DB: cityfix)
-- Just connect to it and initialize schema

-- Note: POSTGRES_DB in docker-compose already creates the "cityfix" database
-- This script initializes tables and indexes

-- =========================
-- Users table
-- =========================
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    reports_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for users
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- =========================
-- Reports table
-- =========================
CREATE TABLE IF NOT EXISTS reports (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'OPEN',
    category VARCHAR(100),
    priority VARCHAR(50) DEFAULT 'MEDIUM',
    latitude FLOAT,
    longitude FLOAT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- Indexes
-- =========================
CREATE INDEX IF NOT EXISTS idx_reports_user_id
    ON reports(user_id);

CREATE INDEX IF NOT EXISTS idx_reports_status
    ON reports(status);

CREATE INDEX IF NOT EXISTS idx_reports_coordinates
    ON reports(latitude, longitude);

-- =========================
-- Foreign Keys
-- =========================
-- Add foreign key for reports -> users
DO $$
BEGIN
    ALTER TABLE reports ADD CONSTRAINT fk_reports_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

