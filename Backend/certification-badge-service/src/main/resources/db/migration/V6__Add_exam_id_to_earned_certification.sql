-- Add exam_id column to earned_certification for tracking auto-awards from Exam Service
ALTER TABLE earned_certification
    ADD COLUMN exam_id VARCHAR(100) NULL;
