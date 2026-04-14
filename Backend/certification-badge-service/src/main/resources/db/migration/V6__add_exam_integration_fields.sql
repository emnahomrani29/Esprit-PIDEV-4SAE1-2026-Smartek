-- Add course_id to certification_template for linking certifications to courses
ALTER TABLE certification_template ADD COLUMN course_id BIGINT;

-- Add course_id and minimum_score to badge_template for automatic badge awarding
ALTER TABLE badge_template ADD COLUMN course_id BIGINT;
ALTER TABLE badge_template ADD COLUMN minimum_score DOUBLE DEFAULT 80.0;

-- Add indexes for better query performance
CREATE INDEX idx_certification_template_course_id ON certification_template(course_id);
CREATE INDEX idx_badge_template_course_id ON badge_template(course_id);
CREATE INDEX idx_earned_certification_learner_template ON earned_certification(learner_id, certification_template_id);
CREATE INDEX idx_earned_badge_learner_template ON earned_badge(learner_id, badge_template_id);
