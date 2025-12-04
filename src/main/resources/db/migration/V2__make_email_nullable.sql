-- Make email column nullable
-- This allows users to register without providing an email address

-- For H2 database
ALTER TABLE users ALTER COLUMN email SET NULL;

-- Note: For PostgreSQL in production, Flyway will handle this automatically
-- PostgreSQL syntax: ALTER TABLE users ALTER COLUMN email DROP NOT NULL;
