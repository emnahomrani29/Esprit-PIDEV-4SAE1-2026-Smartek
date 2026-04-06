package com.smartek.skillevidenceservice.dto;

import com.smartek.skillevidenceservice.entity.EvidenceStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {
    
    @NotNull(message = "Status is required")
    private EvidenceStatus status;
    
    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 100, message = "Score must not exceed 100")
    private Integer score; // Required if status is APPROVED
    
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String adminComment; // Required if status is REJECTED
    
    private Long reviewerId; // Set from authentication context
}
