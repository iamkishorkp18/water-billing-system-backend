CREATE TABLE apartments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    ward VARCHAR(100)
);

CREATE TABLE households (
    id BIGSERIAL PRIMARY KEY,
    apartment_id BIGINT NOT NULL REFERENCES apartments(id),
    flat_number VARCHAR(50) NOT NULL,
    flat_size VARCHAR(50),
    occupancy INT
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    household_id BIGINT REFERENCES households(id)
);

CREATE TABLE admin_apartment_assignments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    apartment_id BIGINT NOT NULL REFERENCES apartments(id)
);