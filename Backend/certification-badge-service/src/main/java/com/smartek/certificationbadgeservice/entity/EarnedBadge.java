package com.smartek.certificationbadgeservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "earned_badge",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"badge_template_id", "learner_id"}
       ))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EarnedBadge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_template_id", nullable = false)
    private BadgeTemplate badgeTemplate;
    
    @Column(name = "learner_id", nullable = false)
    private Long learnerId;
    
    @Column(name = "award_date", nullable = false)
    private LocalDate awardDate;
    
    @Column(name = "awarded_by", nullable = false)
    private Long awardedBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (awardDate == null) {
            awardDate = LocalDate.now();
        }
    }
}
