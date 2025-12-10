-- Create bots table
CREATE TABLE bots (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    competition_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bot_competition FOREIGN KEY (competition_id) REFERENCES competitions(id) ON DELETE CASCADE,
    CONSTRAINT uk_bot_competition_username UNIQUE (competition_id, username)
);

-- Create index for faster lookups
CREATE INDEX idx_bots_competition_id ON bots(competition_id);
