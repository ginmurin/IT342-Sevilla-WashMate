-- Add first_name and last_name columns
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS first_name VARCHAR,
    ADD COLUMN IF NOT EXISTS last_name  VARCHAR;

-- Migrate existing data: everything before the first space → first_name, rest → last_name
UPDATE users
SET
    first_name = SPLIT_PART(full_name, ' ', 1),
    last_name  = NULLIF(TRIM(SUBSTRING(full_name FROM POSITION(' ' IN full_name))), '');

-- Drop the old full_name column
ALTER TABLE users
    DROP COLUMN IF EXISTS full_name;
