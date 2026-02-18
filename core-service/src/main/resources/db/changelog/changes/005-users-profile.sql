-- liquibase formatted sql

-- changeset bidwat:24
ALTER TABLE users ADD COLUMN IF NOT EXISTS name VARCHAR(150);

-- changeset bidwat:25
ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(80);

-- changeset bidwat:26
UPDATE users
SET username = LOWER(
    REGEXP_REPLACE(
        SPLIT_PART(email, '@', 1),
        '[^a-zA-Z0-9_\.-]',
        '',
        'g'
    )
)
WHERE username IS NULL OR BTRIM(username) = '';

-- changeset bidwat:27
WITH usernames AS (
    SELECT
        id,
        username,
        ROW_NUMBER() OVER (PARTITION BY username ORDER BY id) AS rn
    FROM users
)
UPDATE users u
SET username = u.username || '_' || u.id
FROM usernames un
WHERE u.id = un.id
  AND un.rn > 1;

-- changeset bidwat:28
UPDATE users
SET username = 'user_' || id
WHERE username IS NULL OR BTRIM(username) = '';

-- changeset bidwat:29
ALTER TABLE users ALTER COLUMN username SET NOT NULL;

-- changeset bidwat:30
ALTER TABLE users ADD CONSTRAINT uq_users_username UNIQUE (username);
