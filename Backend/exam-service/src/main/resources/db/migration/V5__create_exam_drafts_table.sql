-- Table pour sauvegarder les brouillons de réponses
CREATE TABLE IF NOT EXISTS exam_drafts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer TEXT,
    saved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_draft (exam_id, user_id, question_id),
    INDEX idx_exam_user (exam_id, user_id)
);
