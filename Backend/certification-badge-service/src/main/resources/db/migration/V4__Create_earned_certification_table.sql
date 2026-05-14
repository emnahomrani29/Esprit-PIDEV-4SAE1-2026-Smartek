-- Create earned_certification table
CREATE TABLE earned_certification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    certification_template_id BIGINT NOT NULL,
    learner_id BIGINT NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE,
    certificate_url VARCHAR(500),
    awarded_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_earned_certification_template FOREIGN KEY (certification_template_id) 
        REFERENCES certification_template(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
