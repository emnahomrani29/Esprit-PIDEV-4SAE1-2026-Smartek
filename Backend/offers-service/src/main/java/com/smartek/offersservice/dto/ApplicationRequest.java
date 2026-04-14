package com.smartek.offersservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequest {
    
    @NotNull(message = "Offer ID is required")
    private Long offerId;
    
    @NotNull(message = "Learner ID is required")
    private Long learnerId;
    
    private String learnerName;
    
    private String learnerEmail;
    
    private String coverLetter;
    
    private String cvBase64;
    
    private String cvFileName;
}
