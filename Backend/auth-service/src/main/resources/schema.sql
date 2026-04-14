-- Script de création de la base de données SMARTEK
CREATE DATABASE IF NOT EXISTS smartek_db;
USE smartek_db;

-- Table des utilisateurs
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    image BLOB,
    first_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    experience INT DEFAULT 0,
    role VARCHAR(20) NOT NULL,
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
