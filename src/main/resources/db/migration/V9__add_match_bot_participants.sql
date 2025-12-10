-- Create match_bot_participants join table
CREATE TABLE match_bot_participants (
    match_id BIGINT NOT NULL,
    bot_id BIGINT NOT NULL,
    PRIMARY KEY (match_id, bot_id),
    CONSTRAINT fk_match_bot_participants_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE,
    CONSTRAINT fk_match_bot_participants_bot FOREIGN KEY (bot_id) REFERENCES bots(id) ON DELETE CASCADE
);

-- Add index for better query performance
CREATE INDEX idx_match_bot_participants_match ON match_bot_participants(match_id);
CREATE INDEX idx_match_bot_participants_bot ON match_bot_participants(bot_id);
