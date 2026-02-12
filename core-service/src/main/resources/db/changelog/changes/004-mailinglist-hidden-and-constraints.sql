-- liquibase formatted sql

-- changeset bidwat:22
ALTER TABLE mailing_lists ADD COLUMN IF NOT EXISTS hidden BOOLEAN NOT NULL DEFAULT FALSE;

-- changeset bidwat:23
UPDATE mailing_lists SET hidden = FALSE WHERE hidden IS NULL;
