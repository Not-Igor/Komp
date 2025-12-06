-- Add scores_submitted column to matches table
ALTER TABLE matches ADD COLUMN scores_submitted BOOLEAN NOT NULL DEFAULT false;

-- Create match_scores table
CREATE TABLE match_scores (
    id BIGSERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    score INTEGER NOT NULL,
    confirmed BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT fk_match_score_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE,
    CONSTRAINT fk_match_score_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_match_user UNIQUE (match_id, user_id)
);

-- Create indexes for better performance
CREATE INDEX idx_match_scores_match_id ON match_scores(match_id);
CREATE INDEX idx_match_scores_user_id ON match_scores(user_id);
