-- Convert all existing usernames to lowercase
-- This ensures case-insensitive login for all users

-- First, check for potential conflicts and append a number to duplicates
WITH duplicates AS (
    SELECT id, username, LOWER(username) as lower_username,
           ROW_NUMBER() OVER (PARTITION BY LOWER(username) ORDER BY id) as rn
    FROM users
)
UPDATE users
SET username = CASE 
    WHEN d.rn > 1 THEN LOWER(d.username) || '_' || d.rn
    ELSE LOWER(d.username)
END
FROM duplicates d
WHERE users.id = d.id AND d.rn > 1;

-- Then convert all remaining usernames to lowercase
UPDATE users SET username = LOWER(username);
