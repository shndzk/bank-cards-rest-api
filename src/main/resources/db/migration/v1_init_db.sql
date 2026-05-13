-- changeset author:init_banking_tables
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(60) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS bank_cards (
    id BIGSERIAL PRIMARY KEY,
    card_number VARCHAR(255) NOT NULL,
    owner_id BIGINT NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    balance NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_card_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_bank_cards_owner_id ON bank_cards(owner_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_bank_cards_card_number ON bank_cards(card_number);

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (username, password, role, is_blocked)
VALUES ('admin', crypt('admin', gen_salt('bf', 10)), 'ROLE_ADMIN', FALSE)
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, role, is_blocked)
VALUES ('user', crypt('admin', gen_salt('bf', 10)), 'ROLE_USER', FALSE)
ON CONFLICT (username) DO NOTHING;
