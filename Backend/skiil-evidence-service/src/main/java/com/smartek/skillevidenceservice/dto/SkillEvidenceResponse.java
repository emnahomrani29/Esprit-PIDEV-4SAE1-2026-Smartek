package com.smartek.skillevidenceservice.dto;

import com.smartek.skillevidenceservice.entity.EvidenceCategory;
import com.smartek.skillevidenceservice.entity.EvidenceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillEvidenceResponse {

    private Integer evidenceId;
    private String title;
    private String fileUrl;
    private String description;
    private LocalDate uploadDate;
    private Long learnerId;
    private String learnerName;
    private String learnerEmail;
    
    // Validation fields
    private EvidenceStatus status;
    private Integer score;
    private String adminComment;
    private Long reviewedBy;
    private LocalDate reviewedAt;
    private EvidenceCategory category;
}