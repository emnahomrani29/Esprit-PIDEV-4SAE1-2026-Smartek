package com.smartek.skillevidenceservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "skill_evidence")   // convention snake_case recommandée
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer evidenceId;

    @Column(nullable = false)
    private String title;

    private String fileUrl;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDate uploadDate;

    @Column(nullable = false)
    private Long learnerId;

    @Column(nullable = false)
    private String learnerName;

    @Column(nullable = false)
    private String learnerEmail;

    // Validation fields
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvidenceStatus status;

    @Column
    private Integer score; // 0-100

    @Column(length = 2000)
    private String adminComment;

    @Column
    private Long reviewedBy; // Admin ID

    @Column
    private LocalDate reviewedAt;

    @Enumerated(EnumType.STRING)
    @Column
    private EvidenceCategory category;

    @PrePersist
    protected void onCreate() {
        if (this.uploadDate == null) {
            this.uploadDate = LocalDate.now();
        }
        if (this.status == null) {
            this.status = EvidenceStatus.PENDING;
        }
        if (this.category == null) {
            this.category = EvidenceCategory.OTHER;
        }
    }
}
