-- ============================================================
--  SMARTEK - Initialisation des bases de donnees MySQL
--  Ce script est execute automatiquement au premier demarrage
--  du conteneur MySQL via docker-compose
-- ============================================================

CREATE DATABASE IF NOT EXISTS smartek_auth;
CREATE DATABASE IF NOT EXISTS smartek_events;
CREATE DATABASE IF NOT EXISTS smartek_planning;
CREATE DATABASE IF NOT EXISTS smartek_training;
CREATE DATABASE IF NOT EXISTS smartek_offers;
CREATE DATABASE IF NOT EXISTS smartek_course;
CREATE DATABASE IF NOT EXISTS smartek_exam;
CREATE DATABASE IF NOT EXISTS smartek_skill_evidence;
CREATE DATABASE IF NOT EXISTS smartek_learning;
CREATE DATABASE IF NOT EXISTS smartek_sponsor;
CREATE DATABASE IF NOT EXISTS smartek_certification;

-- Accorder tous les privileges a root depuis n'importe quel host
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
