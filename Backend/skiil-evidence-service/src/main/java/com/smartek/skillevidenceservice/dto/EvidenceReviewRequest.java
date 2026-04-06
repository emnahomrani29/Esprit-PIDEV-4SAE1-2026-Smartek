package com.smartek.skillevidenceservice.dto;

import com.smartek.skillevidenceservice.entity.EvidenceStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceReviewRequest {

    @NotNull(message = "Status is required")
    private EvidenceStatus status;

    @Min(value = 0, message = "Score must be between 0 and 100")
    @Max(value = 100, message = "Score must be between 0 and 100")
    private Integer score;

    private String adminComment;

    @NotNull(message = "Reviewer ID is required")
    private Long reviewedBy;
}
