-- Create bot_scores table for storing bot scores in matches
CREATE TABLE bot_scores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_id BIGINT NOT NULL,
    bot_id BIGINT NOT NULL,
    score INTEGER,
    confirmed BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_bot_scores_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE,
    CONSTRAINT fk_bot_scores_bot FOREIGN KEY (bot_id) REFERENCES bots(id) ON DELETE CASCADE
);

-- Add indexes for better query performance
CREATE INDEX idx_bot_scores_match ON bot_scores(match_id);
CREATE INDEX idx_bot_scores_bot ON bot_scores(bot_id);
