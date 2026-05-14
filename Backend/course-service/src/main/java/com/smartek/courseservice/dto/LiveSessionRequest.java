package com.smartek.courseservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveSessionRequest {
    
    @NotNull(message = "Le cours ID est obligatoire")
    private Long courseId;
    
    @NotBlank(message = "Le titre est obligatoire")
    private String title;
    
    private String description;
    
    @NotNull(message = "Le trainer ID est obligatoire")
    private Long trainerId;
    
    @NotNull(message = "L'heure de début est obligatoire")
    private LocalDateTime startTime;
    
    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalDateTime endTime;
    
    private Integer maxParticipants;
    
    // Le roomId sera généré automatiquement côté backend
}
