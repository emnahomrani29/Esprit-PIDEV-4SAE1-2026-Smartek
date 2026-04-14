package com.smartek.skillevidenceservice.dto;

import com.smartek.skillevidenceservice.entity.EvidenceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillEvidenceRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String fileUrl;

    private String description;

    @NotNull(message = "Learner ID is required")
    private Long learnerId;

    @NotBlank(message = "Learner name is required")
    private String learnerName;

    @NotBlank(message = "Learner email is required")
    private String learnerEmail;

    private EvidenceCategory category;
}