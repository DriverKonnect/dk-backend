ALTER TABLE tour_requests
    ADD COLUMN IF NOT EXISTS advance_percentage INT CHECK (advance_percentage >= 1 AND advance_percentage <= 99);
