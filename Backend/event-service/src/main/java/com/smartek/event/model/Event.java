package com.smartek.event.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Start date is required")
    @Column(nullable = false)
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Column(nullable = false)
    private LocalDateTime endDate;

    @NotBlank(message = "Location is required")
    @Size(max = 255, message = "Location must not exceed 255 characters")
    @Column(nullable = false)
    private String location;

    // Gestion des capacités hybrides
    @NotNull(message = "Physical capacity is required")
    @Min(value = 0, message = "Physical capacity must be at least 0")
    @Column(nullable = false)
    private Integer physicalCapacity = 0;

    @NotNull(message = "Online capacity is required")
    @Min(value = 0, message = "Online capacity must be at least 0")
    @Column(nullable = false)
    private Integer onlineCapacity = 0;

    @Column(nullable = false)
    private Integer physicalRegistered = 0;

    @Column(nullable = false)
    private Integer onlineRegistered = 0;

    // Anciens champs pour compatibilité
    @NotNull(message = "Max participations is required")
    @Min(value = 1, message = "Max participations must be at least 1")
    @Column(nullable = false)
    private Integer maxParticipations;

    @Column(nullable = false)
    private Integer currentParticipations = 0;

    // Workflow des statuts
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.DRAFT;

    // Gestion des paiements
    @Column(precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean isPaid = false;

    // Mode de l'événement
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventMode mode = EventMode.PHYSICAL;

    // Métadonnées
    @Column(nullable = false)
    private Long createdBy;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        // Initialiser les capacités hybrides si nécessaire
        if (physicalCapacity == null && onlineCapacity == null) {
            if (mode == EventMode.PHYSICAL) {
                physicalCapacity = maxParticipations;
                onlineCapacity = 0;
            } else if (mode == EventMode.ONLINE) {
                physicalCapacity = 0;
                onlineCapacity = maxParticipations;
            } else { // HYBRID
                physicalCapacity = maxParticipations / 2;
                onlineCapacity = maxParticipations / 2;
            }
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Méthodes utilitaires
    public Integer getTotalCapacity() {
        return physicalCapacity + onlineCapacity;
    }

    public Integer getTotalRegistered() {
        return physicalRegistered + onlineRegistered;
    }

    public Boolean isFull() {
        return getTotalRegistered() >= getTotalCapacity();
    }

    public Boolean hasAvailableCapacity(EventMode registrationMode) {
        if (registrationMode == EventMode.PHYSICAL) {
            return physicalRegistered < physicalCapacity;
        } else if (registrationMode == EventMode.ONLINE) {
            return onlineRegistered < onlineCapacity;
        }
        return false;
    }
}
