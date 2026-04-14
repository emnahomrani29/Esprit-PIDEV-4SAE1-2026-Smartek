package com.smartek.certificationbadgeservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificationTemplateDTO {
    
    private Long id;
    
    @NotBlank(message = "Certification title is required")
    @Size(max = 200, message = "Certification title must not exceed 200 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private Long examId;
}
