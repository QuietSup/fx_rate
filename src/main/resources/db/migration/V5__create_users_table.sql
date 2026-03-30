-- Basic users table for HTTP Basic auth and role-based authorization
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(100) NOT NULL,
    password_hash VARCHAR(200) NOT NULL,
    role          VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'STANDARD'))
);

COMMENT ON TABLE users IS 'Application users used for authentication/authorization';
COMMENT ON COLUMN users.role IS 'ADMIN | STANDARD';

