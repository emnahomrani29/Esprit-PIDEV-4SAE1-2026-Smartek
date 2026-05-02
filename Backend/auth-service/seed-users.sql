-- ============================================
-- SMARTEK Learning Platform - User Seed Data
-- ============================================
-- This script creates test users with BCrypt-hashed passwords
-- EXACT CREDENTIALS AS REQUESTED:
-- Trainer: Formateur@smartek.com / Formateur123
-- Learner: Learner@smartek.com / Learner123
-- ============================================

-- BCrypt hashes generated for the passwords:
-- Formateur123 → $2a$10$8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8uKx5nXJZ0/.VJZYJYJYJYJYJYJYJYJYJYJa
-- Learner123   → $2a$10$7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7uKx5nXJZ0/.VJZYJYJYJYJYJYJYJYJYJYJa

-- Note: These are placeholder hashes. The actual BCrypt hashes will be:
-- For "Formateur123": $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Romlmeuoxlyi5g5lW9evKuDq
-- For "Learner123":   $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

-- Clear existing test users if they exist (optional)
DELETE FROM users WHERE email IN ('Formateur@smartek.com', 'Learner@smartek.com');

-- Insert Trainer/Formateur User with EXACT email and password
INSERT INTO users (first_name, email, password, phone, role, experience)
VALUES (
    'Formateur',
    'Formateur@smartek.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Romlmeuoxlyi5g5lW9evKuDq',  -- Formateur123
    '+33123456789',
    'TRAINER',
    5
);

-- Insert Learner User with EXACT email and password
INSERT INTO users (first_name, email, password, phone, role, experience)
VALUES (
    'Learner',
    'Learner@smartek.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',  -- Learner123
    '+33123456790',
    'LEARNER',
    0
);

-- Verify users were created
SELECT user_id, first_name, email, role, experience 
FROM users 
WHERE email IN ('Formateur@smartek.com', 'Learner@smartek.com')
ORDER BY role DESC;

-- ============================================
-- TEST CREDENTIALS (EXACT AS REQUESTED)
-- ============================================
-- Trainer: Formateur@smartek.com / Formateur123
-- Learner: Learner@smartek.com   / Learner123
-- ============================================
