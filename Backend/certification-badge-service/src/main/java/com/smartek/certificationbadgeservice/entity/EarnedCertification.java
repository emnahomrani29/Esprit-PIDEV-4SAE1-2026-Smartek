package com.smartek.certificationbadgeservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "earned_certification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EarnedCertification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_template_id", nullable = false)
    private CertificationTemplate certificationTemplate;
    
    @Column(name = "learner_id", nullable = false)
    private Long learnerId;
    
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    @Size(max = 500, message = "Certificate URL must not exceed 500 characters")
    @Column(name = "certificate_url", length = 500)
    private String certificateUrl;
    
    @Column(name = "awarded_by", nullable = false)
    private Long awardedBy;
    
    @Column(name = "exam_id", length = 100)
    private String examId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (issueDate == null) {
            issueDate = LocalDate.now();
        }
    }
    
    @Transient
    public boolean isExpired() {
        return expiryDate != null && LocalDate.now().isAfter(expiryDate);
    }
}
