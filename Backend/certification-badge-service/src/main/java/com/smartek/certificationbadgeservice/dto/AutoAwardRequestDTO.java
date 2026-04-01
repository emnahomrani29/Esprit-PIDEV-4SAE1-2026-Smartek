package com.smartek.certificationbadgeservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoAwardRequestDTO {
    @NotNull
    private Long certificationTemplateId;
    @NotNull
    private Long learnerId;
    @NotNull
    private String examId;
    @NotNull
    private Double score;
    @NotNull
    private LocalDateTime completionDate;
}
