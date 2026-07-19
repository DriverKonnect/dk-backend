ALTER TABLE tour_requests
    ADD COLUMN IF NOT EXISTS amount         DECIMAL(12, 2),
    ADD COLUMN IF NOT EXISTS payment_status VARCHAR(20);

ALTER TABLE tour_assignments
    ADD COLUMN IF NOT EXISTS rating INT CHECK (rating >= 1 AND rating <= 5);
