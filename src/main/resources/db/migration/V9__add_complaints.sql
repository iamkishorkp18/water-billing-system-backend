CREATE TABLE complaints (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL REFERENCES households(id),
    resident_email VARCHAR(255) NOT NULL,
    complaint_type VARCHAR(50) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_date DATE NOT NULL,
    resolved_date DATE
);