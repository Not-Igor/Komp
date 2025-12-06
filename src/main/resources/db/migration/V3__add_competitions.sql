-- Add competitions tables
-- This migration creates the competitions and competition_participants tables
-- Compatible with both H2 and PostgreSQL

CREATE TABLE IF NOT EXISTS competitions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    icon VARCHAR(10) NOT NULL,
    creator_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS competition_participants (
    competition_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (competition_id, user_id),
    FOREIGN KEY (competition_id) REFERENCES competitions(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_competitions_creator ON competitions(creator_id);
CREATE INDEX IF NOT EXISTS idx_competitions_created_at ON competitions(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_competition_participants_user ON competition_participants(user_id);
