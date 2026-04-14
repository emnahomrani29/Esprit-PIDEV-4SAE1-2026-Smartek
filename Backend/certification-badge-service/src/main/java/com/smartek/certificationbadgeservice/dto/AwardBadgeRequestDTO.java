package com.smartek.certificationbadgeservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AwardBadgeRequestDTO {
    
    @NotNull(message = "Badge template ID is required")
    private Long badgeTemplateId;
    
    @NotNull(message = "Learner ID is required")
    private Long learnerId;
    
    private Long awardedBy;
}
