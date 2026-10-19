ALTER TABLE tour_driver_applications
    ADD COLUMN IF NOT EXISTS driver_vehicle_id    BIGINT        REFERENCES driver_vehicles(id),
    ADD COLUMN IF NOT EXISTS per_km_rate_snapshot DECIMAL(10,2);
