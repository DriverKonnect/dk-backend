CREATE TABLE IF NOT EXISTS user_roles (
    role_id BIGSERIAL PRIMARY KEY,
    role    VARCHAR(50) NOT NULL
);

INSERT INTO user_roles (role) VALUES ('ADMIN')        ON CONFLICT DO NOTHING;
INSERT INTO user_roles (role) VALUES ('DRIVER')       ON CONFLICT DO NOTHING;
INSERT INTO user_roles (role) VALUES ('TOUR_COMPANY') ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS users (
    id             BIGSERIAL PRIMARY KEY,
    first_name     VARCHAR(100),
    last_name      VARCHAR(100),
    username       VARCHAR(100) NOT NULL UNIQUE,
    email          VARCHAR(255) NOT NULL UNIQUE,
    role_id        BIGINT       NOT NULL REFERENCES user_roles(role_id),
    password       VARCHAR(255),
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    is_first_login BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(512) NOT NULL UNIQUE,
    expiry_date TIMESTAMP    NOT NULL,
    user_id     BIGINT       NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE
);
