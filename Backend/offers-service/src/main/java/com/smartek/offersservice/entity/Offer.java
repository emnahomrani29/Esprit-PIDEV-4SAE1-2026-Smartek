package com.smartek.offersservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "offers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Offer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;
    
    @NotBlank(message = "Description is required")
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotBlank(message = "Company name is required")
    @Column(nullable = false)
    private String companyName;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    @NotBlank(message = "Contract type is required")
    private String contractType; // CDI, CDD, Stage, Alternance, etc.
    
    private String salary;
    
    // Salaire min/max pour le filtrage numérique
    private Integer salaryMin;
    private Integer salaryMax;
    
    // Domaine / catégorie de l'offre
    private String domain;
    
    // Niveau d'expérience requis
    private String experienceLevel; // JUNIOR, MID, SENIOR
    
    // Télétravail
    private Boolean remote = false;
    
    // Nombre de postes disponibles
    private Integer positions = 1;
    
    // Nombre de vues
    @Column(nullable = false)
    private Long viewCount = 0L;
    
    @NotNull(message = "Company ID is required")
    @Column(nullable = false)
    private Long companyId;
    
    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, CLOSED, DRAFT, EXPIRED
    
    // Date d'expiration automatique
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
