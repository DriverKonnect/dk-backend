CREATE TABLE IF NOT EXISTS tour_company_profiles (
    id                           BIGSERIAL PRIMARY KEY,
    user_id                      BIGINT       NOT NULL REFERENCES users(id),
    company_name                 VARCHAR(255) NOT NULL,
    business_registration_number VARCHAR(100) NOT NULL UNIQUE,
    contact_person_name          VARCHAR(255) NOT NULL,
    phone                        VARCHAR(20)  NOT NULL,
    created_at                   TIMESTAMP,
    updated_at                   TIMESTAMP
);
