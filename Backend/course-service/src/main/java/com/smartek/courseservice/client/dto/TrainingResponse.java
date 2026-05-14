package com.smartek.courseservice.client.dto;

import java.util.List;

public class TrainingResponse {
    private Long trainingId;
    private String title;
    private List<Long> courseIds;
    
    public TrainingResponse() {}
    
    public Long getTrainingId() {
        return trainingId;
    }
    
    public void setTrainingId(Long trainingId) {
        this.trainingId = trainingId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public List<Long> getCourseIds() {
        return courseIds;
    }
    
    public void setCourseIds(List<Long> courseIds) {
        this.courseIds = courseIds;
    }
}
