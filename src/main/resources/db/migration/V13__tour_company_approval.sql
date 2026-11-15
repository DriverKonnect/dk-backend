ALTER TABLE tour_company_profiles
    ADD COLUMN status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN rejection_reason TEXT,
    ADD COLUMN reviewed_at      TIMESTAMP,
    ADD COLUMN reviewed_by      BIGINT REFERENCES users(id);

-- Companies registered before the approval workflow were already live; keep them working.
UPDATE tour_company_profiles p
SET status = CASE WHEN u.is_active THEN 'APPROVED' ELSE 'DEACTIVATED' END
FROM users u
WHERE u.id = p.user_id;

CREATE INDEX IF NOT EXISTS idx_tour_company_profiles_status ON tour_company_profiles(status);
