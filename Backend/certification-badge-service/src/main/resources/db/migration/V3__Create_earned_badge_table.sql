-- Create earned_badge table with unique constraint
CREATE TABLE earned_badge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    badge_template_id BIGINT NOT NULL,
    learner_id BIGINT NOT NULL,
    award_date DATE NOT NULL,
    awarded_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_earned_badge_template FOREIGN KEY (badge_template_id) 
        REFERENCES badge_template(id),
    CONSTRAINT uk_earned_badge_learner UNIQUE (badge_template_id, learner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
