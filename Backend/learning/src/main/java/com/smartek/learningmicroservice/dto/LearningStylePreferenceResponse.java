package com.smartek.learningmicroservice.dto;

import com.smartek.learningmicroservice.entity.LearningStyleType;

import java.time.LocalDateTime;

public class LearningStylePreferenceResponse {

    private Long id;
    private LearningStyleType preferredStyle;
    private Boolean videoPreferred;
    private Boolean textPreferred;
    private Boolean practicalWorkPreferred;
    private LocalDateTime lastUpdated;
    private Long learnerId;
    private String learnerName;

    public LearningStylePreferenceResponse() {}

    public LearningStylePreferenceResponse(Long id, LearningStyleType preferredStyle, Boolean videoPreferred,
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
}
