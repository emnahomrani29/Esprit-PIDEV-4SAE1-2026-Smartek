package com.smartek.learningmicroservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_style_preferences")
public class LearningStylePreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LearningStyleType preferredStyle;

    private Boolean videoPreferred = false;
    private Boolean textPreferred = false;
    private Boolean practicalWorkPreferred = false;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @Column(nullable = false)
    private Long learnerId;

    @Column(nullable = true)
    private String learnerName;

    public LearningStylePreference() {}

    public LearningStylePreference(Long id, LearningStyleType preferredStyle, Boolean videoPreferred,
                                   Boolean textPreferred, Boolean practicalWorkPreferred,
                                   LocalDateTime lastUpdated, Long learnerId, String learnerName) {
        this.id = id;
        this.preferredStyle = preferredStyle;
        this.videoPreferred = videoPreferred;
        this.textPreferred = textPreferred;
        this.practicalWorkPreferred = practicalWorkPreferred;
        this.lastUpdated = lastUpdated;
        this.learnerId = learnerId;
        this.learnerName = learnerName;
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LearningStyleType getPreferredStyle() { return preferredStyle; }
    public void setPreferredStyle(LearningStyleType preferredStyle) { this.preferredStyle = preferredStyle; }

    public Boolean getVideoPreferred() { return videoPreferred; }
    public void setVideoPreferred(Boolean videoPreferred) { this.videoPreferred = videoPreferred; }

    public Boolean getTextPreferred() { return textPreferred; }
    public void setTextPreferred(Boolean textPreferred) { this.textPreferred = textPreferred; }

    public Boolean getPracticalWorkPreferred() { return practicalWorkPreferred; }
    public void setPracticalWorkPreferred(Boolean practicalWorkPreferred) { this.practicalWorkPreferred = practicalWorkPreferred; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public Long getLearnerId() { return learnerId; }
    public void setLearnerId(Long learnerId) { this.learnerId = learnerId; }

    public String getLearnerName() { return learnerName; }
    public void setLearnerName(String learnerName) { this.learnerName = learnerName; }

    // Builder pattern
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private LearningStyleType preferredStyle;
        private Boolean videoPreferred = false;
        private Boolean textPreferred = false;
        private Boolean practicalWorkPreferred = false;
        private LocalDateTime lastUpdated;
        private Long learnerId;
        private String learnerName;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder preferredStyle(LearningStyleType preferredStyle) { this.preferredStyle = preferredStyle; return this; }
        public Builder videoPreferred(Boolean videoPreferred) { this.videoPreferred = videoPreferred; return this; }
        public Builder textPreferred(Boolean textPreferred) { this.textPreferred = textPreferred; return this; }
        public Builder practicalWorkPreferred(Boolean practicalWorkPreferred) { this.practicalWorkPreferred = practicalWorkPreferred; return this; }
        public Builder lastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; return this; }
        public Builder learnerId(Long learnerId) { this.learnerId = learnerId; return this; }
        public Builder learnerName(String learnerName) { this.learnerName = learnerName; return this; }

        public LearningStylePreference build() {
            return new LearningStylePreference(id, preferredStyle, videoPreferred, textPreferred,
                    practicalWorkPreferred, lastUpdated, learnerId, learnerName);
        }
    }
}
