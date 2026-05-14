package com.smartek.event.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @NotBlank(message = "Location is required")
    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    // Capacités hybrides
    @Min(value = 0, message = "Physical capacity must be at least 0")
    private Integer physicalCapacity = 0;

    @Min(value = 0, message = "Online capacity must be at least 0")
    private Integer onlineCapacity = 0;

    // Anciens champs pour compatibilité
    @NotNull(message = "Max participations is required")
    @Min(value = 1, message = "Max participations must be at least 1")
    private Integer maxParticipations;

    // Mode de l'événement
    private String mode = "PHYSICAL"; // PHYSICAL, ONLINE, HYBRID

    // Gestion des paiements
    @DecimalMin(value = "0.0", message = "Price must be positive")
    private BigDecimal price = BigDecimal.ZERO;

    private Boolean isPaid = false;

    // Métadonnées
    private Long createdBy;
}
