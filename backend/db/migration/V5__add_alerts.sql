CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL REFERENCES households(id),
    alert_type VARCHAR(50) NOT NULL,
    message VARCHAR(500) NOT NULL,
    created_date DATE NOT NULL,
    resolved BOOLEAN NOT NULL DEFAULT FALSE
);