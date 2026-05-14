-- verification_id already exists on both tables (added previously).
-- This migration ensures the unique indexes exist for fast lookups.
-- Uses IF NOT EXISTS to be safe on fresh databases where the column doesn't exist yet.

-- earned_certification
ALTER TABLE earned_certification
    MODIFY COLUMN verification_id VARCHAR(36) NULL;

-- Add unique index if it doesn't already exist
SET @exist_cert = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'earned_certification'
      AND INDEX_NAME = 'idx_earned_certification_verification_code'
);
SET @sql_cert = IF(@exist_cert = 0,
    'CREATE UNIQUE INDEX idx_earned_certification_verification_code ON earned_certification(verification_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql_cert;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- earned_badge
ALTER TABLE earned_badge
    MODIFY COLUMN verification_id VARCHAR(36) NULL;

SET @exist_badge = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'earned_badge'
      AND INDEX_NAME = 'idx_earned_badge_verification_code'
);
SET @sql_badge = IF(@exist_badge = 0,
    'CREATE UNIQUE INDEX idx_earned_badge_verification_code ON earned_badge(verification_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql_badge;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
