package com.smartek.learningmicroservice.dto;

import com.smartek.learningmicroservice.entity.LearningPathStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class LearningPathRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    private String description;

    @NotNull(message = "L'ID de l'apprenant est obligatoire")
    private Long learnerId;

    @NotBlank(message = "Le nom de l'apprenant est obligatoire")
    private String learnerName;

    @NotNull(message = "Le statut est obligatoire")
    private LearningPathStatus status;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Le progrès est obligatoire")
    @Min(value = 0, message = "Le progrès doit être entre 0 et 100")
    @Max(value = 100, message = "Le progrès doit être entre 0 et 100")
    private Integer progress;

    public LearningPathRequest() {}

    public LearningPathRequest(String title, String description, Long learnerId, String learnerName,
                               LearningPathStatus status, LocalDate startDate, LocalDate endDate,
                               Integer progress) {
        this.title = title;
        this.description = description;
        this.learnerId = learnerId;
        this.learnerName = learnerName;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.progress = progress;
    }

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
