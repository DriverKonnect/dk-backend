CREATE TABLE IF NOT EXISTS tour_assignments (
    id              BIGSERIAL PRIMARY KEY,
    tour_request_id BIGINT    NOT NULL UNIQUE REFERENCES tour_requests(id),
    driver_id       BIGINT    NOT NULL REFERENCES users(id),
    assigned_at     TIMESTAMP,
    updated_at      TIMESTAMP
);
