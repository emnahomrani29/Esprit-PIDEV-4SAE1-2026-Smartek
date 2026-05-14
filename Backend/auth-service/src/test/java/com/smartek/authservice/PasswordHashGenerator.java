package com.smartek.authservice;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilitaire pour générer des hash BCrypt pour les mots de passe de test
 * Usage: Exécuter cette classe pour générer un hash
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "password123";
        String hash = encoder.encode(password);
        
        System.out.println("=".repeat(60));
        System.out.println("GÉNÉRATEUR DE HASH BCRYPT");
        System.out.println("=".repeat(60));
        System.out.println("Mot de passe: " + password);
        System.out.println("Hash BCrypt: " + hash);
        System.out.println("=".repeat(60));
        System.out.println("\nSQL INSERT:");
        System.out.println("INSERT INTO users (first_name, email, password, phone, experience, role)");
        System.out.println("VALUES ('Alice Learner', 'learner@smartek.com', '" + hash + "', '0612345678', 2, 'LEARNER');");
        System.out.println("=".repeat(60));
        
        // Vérifier que le hash fonctionne
        boolean matches = encoder.matches(password, hash);
        System.out.println("\nVérification: " + (matches ? "✅ OK" : "❌ ERREUR"));
    }
}
