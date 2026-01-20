-- liquibase formatted sql

-- changeset bidwat:1
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    auth0_id VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- changeset bidwat:2
INSERT INTO users (auth0_id, email) VALUES ('auth0|12345', 'test@example.com');