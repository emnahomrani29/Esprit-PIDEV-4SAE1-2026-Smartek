CREATE TABLE IF NOT EXISTS exam_enrollments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exam_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    is_unlocked BOOLEAN NOT NULL DEFAULT FALSE,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    unlocked_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_exam (user_id, exam_id)
);

CREATE INDEX idx_exam_enrollments_user_id ON exam_enrollments(user_id);
CREATE INDEX idx_exam_enrollments_exam_id ON exam_enrollments(exam_id);
CREATE INDEX idx_exam_enrollments_course_id ON exam_enrollments(course_id);
