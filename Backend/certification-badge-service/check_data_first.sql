-- STEP 1: Check what data exists BEFORE making changes
USE smartek_db;

-- Check certification templates
SELECT 'CERTIFICATION TEMPLATES:' as info;
SELECT * FROM certification_template;

-- Check badge templates
SELECT 'BADGE TEMPLATES:' as info;
SELECT * FROM badge_template;

-- Check current column structure
SELECT 'CERTIFICATION TEMPLATE COLUMNS:' as info;
SHOW COLUMNS FROM certification_template;

SELECT 'BADGE TEMPLATE COLUMNS:' as info;
SHOW COLUMNS FROM badge_template;
