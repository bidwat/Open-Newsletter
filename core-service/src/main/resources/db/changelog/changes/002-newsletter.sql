-- liquibase formatted sql

-- changeset bidwat:3
CREATE TABLE mailing_lists (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- changeset bidwat:4
CREATE TABLE contacts (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, email)
);

-- changeset bidwat:5
CREATE TABLE mailing_list_contacts (
    id SERIAL PRIMARY KEY,
    mailing_list_id INTEGER NOT NULL REFERENCES mailing_lists(id),
    contact_id INTEGER NOT NULL REFERENCES contacts(id),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (mailing_list_id, contact_id)
);

-- changeset bidwat:6
CREATE TABLE campaigns (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    mailing_list_id INTEGER NOT NULL REFERENCES mailing_lists(id),
    name VARCHAR(150) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    html_content TEXT,
    text_content TEXT,
    html_s3_key VARCHAR(500),
    text_s3_key VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP
);

-- changeset bidwat:7
CREATE TABLE delivery_history (
    id SERIAL PRIMARY KEY,
    campaign_id INTEGER NOT NULL REFERENCES campaigns(id),
    user_id INTEGER NOT NULL REFERENCES users(id),
    contact_id INTEGER NOT NULL REFERENCES contacts(id),
    status VARCHAR(20) NOT NULL,
    error_message VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- changeset bidwat:8
CREATE TABLE replies (
    id SERIAL PRIMARY KEY,
    campaign_id INTEGER REFERENCES campaigns(id),
    user_id INTEGER NOT NULL REFERENCES users(id),
    contact_email VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    body TEXT,
    raw_s3_key VARCHAR(500),
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- changeset bidwat:9
CREATE INDEX idx_mailing_lists_user_id ON mailing_lists(user_id);

-- changeset bidwat:10
CREATE INDEX idx_contacts_user_id ON contacts(user_id);

-- changeset bidwat:11
CREATE INDEX idx_campaigns_user_id ON campaigns(user_id);

-- changeset bidwat:12
CREATE INDEX idx_delivery_history_user_created_at ON delivery_history(user_id, created_at);
