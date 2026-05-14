-- Manual migration: Change course_id to exam_id
USE smartek_db;

-- Change course_id to exam_id in certification_template
ALTER TABLE certification_template CHANGE COLUMN course_id exam_id BIGINT;

-- Change course_id to exam_id in badge_template  
ALTER TABLE badge_template CHANGE COLUMN course_id exam_id BIGINT;

-- Update indexes
DROP INDEX IF EXISTS idx_certification_template_course_id ON certification_template;
CREATE INDEX idx_certification_template_exam_id ON certification_template(exam_id);

DROP INDEX IF EXISTS idx_badge_template_course_id ON badge_template;
CREATE INDEX idx_badge_template_exam_id ON badge_template(exam_id);

-- Verify the changes
SHOW COLUMNS FROM certification_template LIKE 'exam_id';
SHOW COLUMNS FROM badge_template LIKE 'exam_id';
SHOW INDEX FROM certification_template WHERE Key_name = 'idx_certification_template_exam_id';
SHOW INDEX FROM badge_template WHERE Key_name = 'idx_badge_template_exam_id';
