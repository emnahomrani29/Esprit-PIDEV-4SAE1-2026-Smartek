package com.smartek.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private Long eventId;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    
    // Capacités hybrides
    private Integer physicalCapacity;
    private Integer onlineCapacity;
    private Integer physicalRegistered;
    private Integer onlineRegistered;
    
    // Anciens champs pour compatibilité
    private Integer maxParticipations;
    private Integer currentParticipations;
    
    // Workflow et métadonnées
    private String status;
    private String mode;
    private BigDecimal price;
    private Boolean isPaid;
    private Long createdBy;
    
    private Boolean isAvailable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
