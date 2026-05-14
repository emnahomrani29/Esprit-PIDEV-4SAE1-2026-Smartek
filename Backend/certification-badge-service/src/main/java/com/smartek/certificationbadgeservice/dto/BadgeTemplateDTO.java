package com.smartek.certificationbadgeservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BadgeTemplateDTO {
    
    private Long id;
    
    @NotBlank(message = "Badge name is required")
    @Size(max = 100, message = "Badge name must not exceed 100 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private Long examId;
    
    private Double minimumScore = 60.0; // Default: 60% passing threshold
}
