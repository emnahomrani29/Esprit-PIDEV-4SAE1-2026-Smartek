package com.smartek.skillevidenceservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectionRequest {
    
    @NotBlank(message = "Admin comment is required for rejection")
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String adminComment;
    
    private Long reviewerId; // Set from authentication context
}
