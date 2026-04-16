package com.smartek.offersservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "offers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offer {

    // Kept as nested enums for test compatibility, but stored as String in DB
    public enum OfferStatus { ACTIVE, CLOSED, DRAFT, EXPIRED }
    public enum ExperienceLevel { JUNIOR, MID, SENIOR, EXPERT }

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
    private String contractType;

    private String salary;
    private Integer salaryMin;
    private Integer salaryMax;
    private String domain;

    // Stored as String for backward compatibility
    private String experienceLevel;

    @Builder.Default
    private Boolean remote = false;

    @Builder.Default
    private Integer positions = 1;

    @Builder.Default
    @Column(nullable = false)
    private Long viewCount = 0L;

    @NotNull(message = "Company ID is required")
    @Column(nullable = false)
    private Long companyId;

    // Stored as String for backward compatibility
    @Builder.Default
    @Column(nullable = false)
    private String status = "ACTIVE";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "offer_required_skills", joinColumns = @JoinColumn(name = "offer_id"))
    @Column(name = "skill")
    @Builder.Default
    private Set<String> requiredSkills = new HashSet<>();

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
        if (viewCount == null) viewCount = 0L;
        if (positions == null) positions = 1;
        if (remote == null) remote = false;
        if (status == null) status = "ACTIVE";
        if (requiredSkills == null) requiredSkills = new HashSet<>();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isOpen() {
        if (!"ACTIVE".equals(status)) return false;
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) return false;
        return true;
    }
}
