CREATE TABLE tariff_plans (
    id BIGSERIAL PRIMARY KEY,
    apartment_id BIGINT NOT NULL REFERENCES apartments(id),
    tier1_limit NUMERIC(10,2) NOT NULL,
    tier1_rate NUMERIC(10,2) NOT NULL,
    tier2_rate NUMERIC(10,2) NOT NULL
);

CREATE TABLE bills (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL REFERENCES households(id),
    billing_month VARCHAR(7) NOT NULL,
    previous_reading NUMERIC(10,2) NOT NULL,
    current_reading NUMERIC(10,2) NOT NULL,
    consumption NUMERIC(10,2) NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    generated_date DATE NOT NULL
);