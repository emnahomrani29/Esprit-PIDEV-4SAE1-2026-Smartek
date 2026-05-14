-- Table pour les entretiens
CREATE TABLE IF NOT EXISTS interviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    offer_id BIGINT NOT NULL,
    learner_id BIGINT NOT NULL,
    learner_name VARCHAR(255) NOT NULL,
    learner_email VARCHAR(255) NOT NULL,
    interview_date DATETIME NOT NULL,
    location VARCHAR(500) NOT NULL,
    meeting_link VARCHAR(500),
    notes VARCHAR(1000),
    status VARCHAR(50) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);

-- Index pour améliorer les performances
CREATE INDEX idx_interviews_application_id ON interviews(application_id);
CREATE INDEX idx_interviews_offer_id ON interviews(offer_id);
CREATE INDEX idx_interviews_learner_id ON interviews(learner_id);
CREATE INDEX idx_interviews_created_by ON interviews(created_by);
CREATE INDEX idx_interviews_interview_date ON interviews(interview_date);
