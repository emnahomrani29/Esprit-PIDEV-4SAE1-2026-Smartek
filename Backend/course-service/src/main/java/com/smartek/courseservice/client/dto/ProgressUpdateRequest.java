package com.smartek.courseservice.client.dto;

public class ProgressUpdateRequest {
    private int progress;
    
    public ProgressUpdateRequest() {}
    
    public ProgressUpdateRequest(int progress) {
        this.progress = progress;
    }
    
    public int getProgress() {
        return progress;
    }
    
    public void setProgress(int progress) {
        this.progress = progress;
    }
}
