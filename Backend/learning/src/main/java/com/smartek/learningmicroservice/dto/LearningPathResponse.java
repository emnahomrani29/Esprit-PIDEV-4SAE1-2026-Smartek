package com.smartek.learningmicroservice.dto;

import com.smartek.learningmicroservice.entity.LearningPathStatus;

import java.time.LocalDate;

public class LearningPathResponse {

    private Long pathId;
    private String title;
    private String description;
    private Long learnerId;
    private String learnerName;
    private LearningPathStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer progress;

    public LearningPathResponse() {}

    public LearningPathResponse(Long pathId, String title, String description, Long learnerId,
                                String learnerName, LearningPathStatus status, LocalDate startDate,
                                LocalDate endDate, Integer progress) {
        this.pathId = pathId;
        this.title = title;
        this.description = description;
        this.learnerId = learnerId;
        this.learnerName = learnerName;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.progress = progress;
    }

    public Long getPathId() { return pathId; }
    public void setPathId(Long pathId) { this.pathId = pathId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getLearnerId() { return learnerId; }
    public void setLearnerId(Long learnerId) { this.learnerId = learnerId; }

    public String getLearnerName() { return learnerName; }
    public void setLearnerName(String learnerName) { this.learnerName = learnerName; }

    public LearningPathStatus getStatus() { return status; }
    public void setStatus(LearningPathStatus status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
}
