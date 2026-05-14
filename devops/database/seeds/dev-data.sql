-- ════════════════════════════════════════════════════════════════
-- SMARTEK PLATFORM - DONNÉES DE DÉVELOPPEMENT
-- ════════════════════════════════════════════════════════════════
-- Description: Données de test pour l'environnement de développement
-- ════════════════════════════════════════════════════════════════

-- AUTH SERVICE - Utilisateurs de test
USE smartek_auth;

-- Les données spécifiques sont gérées par chaque service
-- via les fichiers data.sql dans src/main/resources

-- Exemple d'utilisateurs de test (à adapter selon votre schéma)
-- INSERT INTO users (username, email, password, role) VALUES
-- ('admin', 'admin@smartek.com', '$2a$10$...', 'ADMIN'),
-- ('user', 'user@smartek.com', '$2a$10$...', 'USER'),
-- ('trainer', 'trainer@smartek.com', '$2a$10$...', 'TRAINER');

-- COURSE SERVICE - Cours de test
USE smartek_course;

-- INSERT INTO courses (title, description, duration, level) VALUES
-- ('Java Spring Boot', 'Formation complète Spring Boot', 40, 'INTERMEDIATE'),
-- ('Angular Avancé', 'Développement Angular avancé', 30, 'ADVANCED'),
-- ('DevOps avec Docker', 'Containerisation et orchestration', 35, 'INTERMEDIATE');

-- EXAM SERVICE - Examens de test
USE smartek_exam;

-- INSERT INTO exams (title, course_id, duration, passing_score) VALUES
-- ('Certification Spring Boot', 1, 120, 70),
-- ('Test Angular', 2, 90, 75),
-- ('Évaluation DevOps', 3, 60, 80);
