ALTER TABLE tour_assignments
    ADD COLUMN IF NOT EXISTS per_km_rate_snapshot DECIMAL(10,2);
