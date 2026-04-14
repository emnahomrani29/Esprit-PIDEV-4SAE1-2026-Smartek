-- Données de test pour SMARTEK
-- IMPORTANT: Exécutez d'abord PasswordHashGenerator.java pour obtenir le hash correct
-- Ou utilisez ce hash pré-généré pour "password123"

USE smartek_db;

-- Nettoyer les données existantes (optionnel - décommenter si nécessaire)
-- DELETE FROM users;

-- Insérer des utilisateurs de test pour chaque rôle
-- Mot de passe pour tous: password123

INSERT INTO users (first_name, email, password, phone, experience, role) VALUES
-- LEARNER (Apprenant) - COMPTE PRINCIPAL POUR LES TESTS
('Alice Learner', 'learner@smartek.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0612345678', 2, 'LEARNER'),
('Bob Student', 'student@smartek.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0612345679', 1, 'LEARNER'),

-- ADMIN
('Admin User', 'admin@smartek.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0612345680', 5, 'ADMIN'),

-- TRAINER (Formateur)
('Charlie Trainer', 'trainer@smartek.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0612345681', 8, 'TRAINER'),

-- RH_SMARTEK
('Diana RH', 'rh.smartek@smartek.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0612345682', 6, 'RH_SMARTEK'),

-- RH_COMPANY
('Eve Company', 'rh.company@smartek.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0612345683', 4, 'RH_COMPANY'),

-- PARTNER
('Frank Partner', 'partner@smartek.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0612345684', 3, 'PARTNER');

-- Vérifier les insertions
SELECT user_id, first_name, email, role, experience FROM users;

-- Afficher uniquement les LEARNER
SELECT user_id, first_name, email, role, experience FROM users WHERE role = 'LEARNER';
