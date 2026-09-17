CREATE TABLE water_usage_logs (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL REFERENCES households(id),
    reading_date DATE NOT NULL,
    meter_reading NUMERIC(10,2) NOT NULL
);