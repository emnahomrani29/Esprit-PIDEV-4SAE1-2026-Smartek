package com.smartek.learningmicroservice.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "learning_paths")
public class LearningPath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pathId;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Long learnerId;

    @Column(nullable = false)
    private String learnerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LearningPathStatus status;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private Integer progress; // Pourcentage 0-100

    public LearningPath() {}

    public LearningPath(Long pathId, String title, String description, Long learnerId,
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

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = LearningPathStatus.PLANIFIE;
        }
        if (this.progress == null) {
            this.progress = 0;
        }
        if (this.startDate == null) {
            this.startDate = LocalDate.now();
        }
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

    // Builder pattern
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long pathId;
        private String title;
        private String description;
        private Long learnerId;
        private String learnerName;
        private LearningPathStatus status;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer progress;

        public Builder pathId(Long pathId) { this.pathId = pathId; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder learnerId(Long learnerId) { this.learnerId = learnerId; return this; }
        public Builder learnerName(String learnerName) { this.learnerName = learnerName; return this; }
        public Builder status(LearningPathStatus status) { this.status = status; return this; }
        public Builder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public Builder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public Builder progress(Integer progress) { this.progress = progress; return this; }

        public LearningPath build() {
            return new LearningPath(pathId, title, description, learnerId, learnerName,
                    status, startDate, endDate, progress);
        }
    }
}
