CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    apartment_id BIGINT REFERENCES apartments(id),
    household_id BIGINT REFERENCES households(id),
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    created_date DATE NOT NULL
);