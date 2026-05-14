package com.smartek.certificationbadgeservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkAwardBadgeRequestDTO {
    
    @NotNull(message = "Badge template ID is required")
    private Long badgeTemplateId;
    
    @NotEmpty(message = "Learner IDs list cannot be empty")
    @Size(max = 100, message = "Cannot award to more than 100 learners at once")
    private List<Long> learnerIds;
    
    private Long awardedBy;
}
