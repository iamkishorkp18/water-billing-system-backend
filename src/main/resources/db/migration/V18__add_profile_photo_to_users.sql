CREATE TABLE profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    full_name VARCHAR(255),
    age INTEGER,
    phone_number VARCHAR(50),
    occupancy_type VARCHAR(100),
    family_members INTEGER,
    profile_photo BYTEA,
    profile_photo_type VARCHAR(100),

    CONSTRAINT fk_profiles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);