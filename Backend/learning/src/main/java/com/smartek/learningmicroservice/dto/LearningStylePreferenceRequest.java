package com.smartek.learningmicroservice.dto;

import com.smartek.learningmicroservice.entity.LearningStyleType;
import jakarta.validation.constraints.NotNull;

public class LearningStylePreferenceRequest {

    @NotNull(message = "Preferred learning style is required")
    private LearningStyleType preferredStyle;

    private Boolean videoPreferred = false;
    private Boolean textPreferred = false;
    private Boolean practicalWorkPreferred = false;

    @NotNull(message = "Learner ID is required")
    private Long learnerId;

    private String learnerName;

    public LearningStylePreferenceRequest() {}

    public LearningStylePreferenceRequest(LearningStyleType preferredStyle, Boolean videoPreferred,
                                          Boolean textPreferred, Boolean practicalWorkPreferred,
                                          Long learnerId, String learnerName) {
        this.preferredStyle = preferredStyle;
        this.videoPreferred = videoPreferred;
        this.textPreferred = textPreferred;
        this.practicalWorkPreferred = practicalWorkPreferred;
        this.learnerId = learnerId;
        this.learnerName = learnerName;
    }

    public LearningStyleType getPreferredStyle() { return preferredStyle; }
    public void setPreferredStyle(LearningStyleType preferredStyle) { this.preferredStyle = preferredStyle; }

    public Boolean getVideoPreferred() { return videoPreferred; }
    public void setVideoPreferred(Boolean videoPreferred) { this.videoPreferred = videoPreferred; }

    public Boolean getTextPreferred() { return textPreferred; }
    public void setTextPreferred(Boolean textPreferred) { this.textPreferred = textPreferred; }

    public Boolean getPracticalWorkPreferred() { return practicalWorkPreferred; }
    public void setPracticalWorkPreferred(Boolean practicalWorkPreferred) { this.practicalWorkPreferred = practicalWorkPreferred; }

    public Long getLearnerId() { return learnerId; }
    public void setLearnerId(Long learnerId) { this.learnerId = learnerId; }

    public String getLearnerName() { return learnerName; }
    public void setLearnerName(String learnerName) { this.learnerName = learnerName; }
}
