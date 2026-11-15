-- Platform-wide settings: a single row (id = 1) holding the commission rate (FR-10, FR-22).
CREATE TABLE settings (
    id         INTEGER       NOT NULL PRIMARY KEY,
    fee_rate   NUMERIC(5, 2) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_settings_singleton CHECK (id = 1),
    CONSTRAINT ck_settings_fee_rate CHECK (fee_rate >= 0 AND fee_rate <= 100)
);

INSERT INTO settings (id, fee_rate, updated_at) VALUES (1, 10.00, CURRENT_TIMESTAMP);
