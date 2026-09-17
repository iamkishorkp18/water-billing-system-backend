CREATE TABLE shared_expenses (
    id BIGSERIAL PRIMARY KEY,
    apartment_id BIGINT NOT NULL REFERENCES apartments(id),
    billing_month VARCHAR(7) NOT NULL,
    description VARCHAR(255) NOT NULL,
    total_amount NUMERIC(10,2) NOT NULL
);