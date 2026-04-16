package com.smartek.offersservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    public enum ApplicationStatus { PENDING, REVIEWED, ACCEPTED, REJECTED, WITHDRAWN }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id")
    private Offer offer;

    // Dénormalisé pour les requêtes sans jointure
    @Column(name = "offer_id", insertable = false, updatable = false)
    private Long offerId;

    @Column(nullable = false)
    private Long learnerId;

    @Column(nullable = false)
    private String learnerName;

    @Column(nullable = false)
    private String learnerEmail;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    @Column(columnDefinition = "LONGTEXT")
    private String cvBase64;

    private String cvFileName;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Builder.Default
    private int score = 0;

    private String recruiterNote;

    @Column(name = "applied_at", nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        appliedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = ApplicationStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /** Setter de commodité pour le statut depuis String */
    public void setStatus(String statusStr) {
        this.status = ApplicationStatus.valueOf(statusStr);
    }

    /** Getter du statut comme String (pour compatibilité avec le code existant) */
    public String getStatusAsString() {
        return status != null ? status.name() : null;
    }
}
