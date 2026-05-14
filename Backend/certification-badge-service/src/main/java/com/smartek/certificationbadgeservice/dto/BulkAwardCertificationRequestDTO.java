package com.smartek.certificationbadgeservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkAwardCertificationRequestDTO {
    
    @NotNull(message = "Certification template ID is required")
    private Long certificationTemplateId;
    
    @NotEmpty(message = "Learner IDs list cannot be empty")
    @Size(max = 100, message = "Cannot award to more than 100 learners at once")
    private List<Long> learnerIds;
    
    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;
    
    private LocalDate expiryDate;
    
    @Size(max = 500, message = "Certificate URL must not exceed 500 characters")
    private String certificateUrl;
    
    private Long awardedBy;
}
