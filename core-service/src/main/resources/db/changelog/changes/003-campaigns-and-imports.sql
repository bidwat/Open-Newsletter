-- liquibase formatted sql

-- changeset bidwat:13
ALTER TABLE mailing_lists ADD COLUMN deleted_at TIMESTAMP;

-- changeset bidwat:14
ALTER TABLE contacts ADD COLUMN deleted_at TIMESTAMP;

-- changeset bidwat:15
ALTER TABLE campaigns ADD COLUMN deleted_at TIMESTAMP;

-- changeset bidwat:16
ALTER TABLE campaigns ALTER COLUMN mailing_list_id DROP NOT NULL;

-- changeset bidwat:17
CREATE TABLE campaign_mailing_lists (
    id SERIAL PRIMARY KEY,
    campaign_id INTEGER NOT NULL REFERENCES campaigns(id),
    mailing_list_id INTEGER NOT NULL REFERENCES mailing_lists(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (campaign_id, mailing_list_id)
);

-- changeset bidwat:18
CREATE TABLE campaign_exclusions (
    id SERIAL PRIMARY KEY,
    campaign_id INTEGER NOT NULL REFERENCES campaigns(id),
    contact_id INTEGER NOT NULL REFERENCES contacts(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (campaign_id, contact_id)
);

-- changeset bidwat:19
INSERT INTO campaign_mailing_lists (campaign_id, mailing_list_id)
SELECT id, mailing_list_id
FROM campaigns
WHERE mailing_list_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- changeset bidwat:20
CREATE INDEX idx_campaign_mailing_lists_campaign_id ON campaign_mailing_lists(campaign_id);

-- changeset bidwat:21
CREATE INDEX idx_campaign_exclusions_campaign_id ON campaign_exclusions(campaign_id);
