CREATE TABLE bulk_water_purchases (
    id BIGSERIAL PRIMARY KEY,
    apartment_id BIGINT NOT NULL REFERENCES apartments(id),
    purchase_date DATE NOT NULL,
    quantity_purchased NUMERIC(12,2) NOT NULL,
    total_cost NUMERIC(12,2) NOT NULL,
    cost_per_unit NUMERIC(12,4) NOT NULL,
    supplier_name VARCHAR(255),
    remarks VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_bulk_purchase_apartment ON bulk_water_purchases(apartment_id);
CREATE INDEX idx_bulk_purchase_date ON bulk_water_purchases(purchase_date);
