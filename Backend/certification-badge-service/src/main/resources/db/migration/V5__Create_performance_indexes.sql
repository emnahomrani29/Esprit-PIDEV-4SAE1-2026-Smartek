-- Create indexes for performance optimization

-- Index for querying earned badges by learner
CREATE INDEX idx_earned_badge_learner ON earned_badge(learner_id);

-- Index for querying earned certifications by learner
CREATE INDEX idx_earned_certification_learner ON earned_certification(learner_id);

-- Index for statistics queries on badges
CREATE INDEX idx_earned_badge_template ON earned_badge(badge_template_id);

-- Index for statistics queries on certifications
CREATE INDEX idx_earned_certification_template ON earned_certification(certification_template_id);

-- Index for expiry date queries
CREATE INDEX idx_earned_certification_expiry ON earned_certification(expiry_date);
