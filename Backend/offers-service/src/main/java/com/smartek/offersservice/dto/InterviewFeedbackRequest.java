package com.smartek.offersservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InterviewFeedbackRequest {

    @NotNull
    private Long interviewId;

    @NotNull
    private Long applicationId;

    @NotNull
    @Min(1) @Max(5)
    private Integer rating;

    private String strengths;
    private String weaknesses;
    private String generalComment;

    @NotNull
    private String decision; // HIRED, REJECTED, PENDING

    @NotNull
    private Long submittedBy;
}
