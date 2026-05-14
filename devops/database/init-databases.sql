-- ════════════════════════════════════════════════════════════════
-- SMARTEK PLATFORM - INITIALISATION DES BASES DE DONNÉES
-- ════════════════════════════════════════════════════════════════

-- Création des bases de données pour chaque microservice
CREATE DATABASE IF NOT EXISTS smartek_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS smartek_events CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS smartek_planning CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS smartek_training CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS smartek_offers CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS smartek_course CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS smartek_exam CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS smartek_skill_evidence CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS smartek_learning CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS smartek_sponsor CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS smartek_certification CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Création d'un utilisateur applicatif (optionnel, pour la production)
-- CREATE USER IF NOT EXISTS 'smartek_user'@'%' IDENTIFIED BY 'smartek_password';

-- Attribution des privilèges
-- GRANT ALL PRIVILEGES ON smartek_*.* TO 'smartek_user'@'%';
-- FLUSH PRIVILEGES;

-- Affichage des bases créées
SHOW DATABASES LIKE 'smartek_%';
