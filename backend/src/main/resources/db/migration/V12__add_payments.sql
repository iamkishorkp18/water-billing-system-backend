CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL REFERENCES bills(id),
    household_id BIGINT NOT NULL REFERENCES households(id),
    amount NUMERIC(10,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    transaction_id VARCHAR(50) NOT NULL,
    payment_date DATE NOT NULL
);