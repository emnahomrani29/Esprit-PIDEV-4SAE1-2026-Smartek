package com.smartek.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventStatusRequest {
    private String status; // DRAFT, VALIDATION_PENDING, PUBLISHED, FULL, IN_PROGRESS, COMPLETED, ARCHIVED
    private String reason; // Raison du changement de statut (optionnel)
}
