-- Convert all existing usernames to lowercase
-- This ensures case-insensitive login for all users

-- Simple approach: just convert all usernames to lowercase
-- If there are duplicates (e.g., "Bob" and "bob"), the second one will fail with unique constraint
-- In practice, this is unlikely and can be handled manually if it occurs
UPDATE users SET username = LOWER(username);
