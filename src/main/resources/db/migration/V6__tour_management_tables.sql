CREATE TABLE IF NOT EXISTS tour_requests (
    id                   BIGSERIAL    PRIMARY KEY,
    tour_company_id      BIGINT       NOT NULL REFERENCES tour_company_profiles(id),
    tour_name            VARCHAR(255) NOT NULL,
    trip_type            VARCHAR(50)  NOT NULL,
    traveller_nationality VARCHAR(50) NOT NULL,
    start_date           DATE         NOT NULL,
    end_date             DATE         NOT NULL,
    days                 INT          NOT NULL,
    nights               INT          NOT NULL,
    pax_count            INT          NOT NULL,
    vehicle_type_id      BIGINT       NOT NULL REFERENCES vehicle_types(id),
    estimated_km         DECIMAL(10, 2),
    specific_requirements TEXT,
    special_concerns      TEXT,
    payment_term         VARCHAR(50)  NOT NULL,
    status               VARCHAR(50)  NOT NULL DEFAULT 'DRAFT',
    created_at           TIMESTAMP,
    updated_at           TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tour_locations (
    id             BIGSERIAL    PRIMARY KEY,
    tour_request_id BIGINT      NOT NULL REFERENCES tour_requests(id),
    location_type  VARCHAR(20)  NOT NULL,
    address        VARCHAR(500) NOT NULL,
    latitude       DECIMAL(10, 7),
    longitude      DECIMAL(10, 7),
    sequence_order INT          NOT NULL,
    created_at     TIMESTAMP
);
