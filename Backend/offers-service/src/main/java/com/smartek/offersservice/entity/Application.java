package com.smartek.offersservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
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
    
    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, ACCEPTED, REJECTED
    
    @Column(name = "applied_at", nullable = false, updatable = false)
    private LocalDateTime appliedAt;
    
    @PrePersist
    protected void onCreate() {
        appliedAt = LocalDateTime.now();
    }
}
