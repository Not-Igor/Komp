-- Make email column nullable
-- This allows users to register without providing an email address
-- Compatible with both H2 and PostgreSQL

ALTER TABLE users ALTER COLUMN email DROP NOT NULL;
