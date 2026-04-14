package com.smartek.trainingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingRequest {
    
    @NotBlank(message = "Le titre est obligatoire")
    private String title;
    
    private String description;
    
    @NotBlank(message = "La catégorie est obligatoire")
    private String category;
    
    @NotBlank(message = "Le niveau est obligatoire")
    private String level;
    
    @NotNull(message = "La durée est obligatoire")
    private LocalDate duration;
    
    private List<Long> courseIds;
    
    private Long createdBy; // ID du trainer qui crée la formation
}
