-- Create matches table
CREATE TABLE matches (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    competition_id BIGINT NOT NULL,
    match_number INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    started_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_competition FOREIGN KEY (competition_id) REFERENCES competitions(id) ON DELETE CASCADE
);

-- Create match_participants junction table
CREATE TABLE match_participants (
    match_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (match_id, user_id),
    CONSTRAINT fk_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_matches_competition_id ON matches(competition_id);
CREATE INDEX idx_matches_status ON matches(status);
CREATE INDEX idx_match_participants_user_id ON match_participants(user_id);
