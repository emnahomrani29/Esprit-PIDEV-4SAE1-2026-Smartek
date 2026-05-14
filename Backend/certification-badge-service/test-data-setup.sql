-- ============================================
-- Test Data Setup for Exam Auto-Award System
-- ============================================
-- This script creates sample certification and badge templates
-- linked to exam ID 102 for testing the auto-award functionality.

USE smartek_db;

-- Clean up existing test data (optional - comment out if you want to keep existing data)
-- DELETE FROM earned_badge WHERE badge_template_id IN (SELECT id FROM badge_template WHERE exam_id = 102);
-- DELETE FROM earned_certification WHERE certification_template_id IN (SELECT id FROM certification_template WHERE exam_id = 102);
-- DELETE FROM badge_template WHERE exam_id = 102;
-- DELETE FROM certification_template WHERE exam_id = 102;

-- ============================================
-- 1. Create Certification Template for Exam 102
-- ============================================
INSERT INTO certification_template (title, description, exam_id, created_at, updated_at)
VALUES (
    'Spring Boot Fundamentals Certification',
    'Awarded for passing the Spring Boot Fundamentals exam with a score of 60% or higher',
    102,
    NOW(),
    NOW()
);

-- ============================================
-- 2. Create Badge Templates with Different Tiers
-- ============================================

-- Bronze Badge (60-74%)
INSERT INTO badge_template (name, description, exam_id, minimum_score, created_at, updated_at)
VALUES (
    'Spring Boot Bronze Badge',
    'Awarded for scoring 60% or higher on the Spring Boot exam',
    102,
    60.0,
    NOW(),
    NOW()
);

-- Silver Badge (75-89%)
INSERT INTO badge_template (name, description, exam_id, minimum_score, created_at, updated_at)
VALUES (
    'Spring Boot Silver Badge',
    'Awarded for scoring 75% or higher on the Spring Boot exam',
    102,
    75.0,
    NOW(),
    NOW()
);

-- Gold Badge (90-100%)
INSERT INTO badge_template (name, description, exam_id, minimum_score, created_at, updated_at)
VALUES (
    'Spring Boot Gold Badge',
    'Awarded for scoring 90% or higher on the Spring Boot exam',
    102,
    90.0,
    NOW(),
    NOW()
);

-- ============================================
-- Verify the data was inserted
-- ============================================
SELECT 'Certification Templates:' as '';
SELECT id, title, exam_id, minimum_score FROM certification_template WHERE exam_id = 102;

SELECT 'Badge Templates:' as '';
SELECT id, name, exam_id, minimum_score FROM badge_template WHERE exam_id = 102 ORDER BY minimum_score;

SELECT '✅ Test data setup complete!' as '';
SELECT 'You can now test the auto-award system using exam_id = 102' as '';
