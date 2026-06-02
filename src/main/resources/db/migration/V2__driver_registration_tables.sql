CREATE TABLE IF NOT EXISTS driver_applications (
    id                         BIGSERIAL PRIMARY KEY,
    application_number         VARCHAR(50) UNIQUE,
    user_id                    BIGINT REFERENCES users(id),
    full_name                  VARCHAR(255) NOT NULL,
    date_of_birth              DATE         NOT NULL,
    phone                      VARCHAR(20)  NOT NULL,
    whatsapp                   VARCHAR(20),
    nic_number                 VARCHAR(20)  NOT NULL UNIQUE,
    languages_spoken           TEXT         NOT NULL,
    years_of_experience        INTEGER      NOT NULL,
    availability               VARCHAR(50)  NOT NULL,
    status                     VARCHAR(50)  NOT NULL DEFAULT 'DRAFT',
    privacy_policy_accepted    BOOLEAN      NOT NULL DEFAULT FALSE,
    privacy_policy_accepted_at TIMESTAMP,
    privacy_policy_version     VARCHAR(20),
    submitted_at               TIMESTAMP,
    created_at                 TIMESTAMP,
    updated_at                 TIMESTAMP
);

CREATE TABLE IF NOT EXISTS driver_documents (
    id             BIGSERIAL PRIMARY KEY,
    application_id BIGINT       NOT NULL REFERENCES driver_applications(id) ON DELETE CASCADE,
    document_type  VARCHAR(50)  NOT NULL,
    file_name      VARCHAR(255) NOT NULL,
    file_path      VARCHAR(500) NOT NULL,
    uploaded_at    TIMESTAMP    NOT NULL
);
