package com.smartek.planning.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "planning_registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanningRegistration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long registrationId;
    
    @Column(nullable = false)
    private Long planningId;
    
    @Column(nullable = false)
    private Long learnerId;
    
    @Column(nullable = false)
    private LocalDateTime registrationDate;
    
    @Column(length = 20)
    private String status; // REGISTERED, CANCELLED, WAITING_LIST
    
    @Column
    private Integer waitingListPosition;
    
    @Column
    private String registrationMode; // ONLINE, ONSITE, HYBRID
    
    @Column
    private String notes;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        registrationDate = LocalDateTime.now();
        if (status == null) {
            status = "REGISTERED";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}