CREATE TABLE IF NOT EXISTS tour_driver_applications (
    id                          BIGSERIAL PRIMARY KEY,
    tour_request_id             BIGINT    NOT NULL REFERENCES tour_requests(id),
    driver_id                   BIGINT    NOT NULL REFERENCES users(id),
    note                        TEXT,
    tour_application_status_id  BIGINT    REFERENCES tour_application_statuses(id),
    is_withdrawn                BOOLEAN   NOT NULL DEFAULT FALSE,
    applied_at                  TIMESTAMP,
    updated_at                  TIMESTAMP,
    UNIQUE (tour_request_id, driver_id)
);
