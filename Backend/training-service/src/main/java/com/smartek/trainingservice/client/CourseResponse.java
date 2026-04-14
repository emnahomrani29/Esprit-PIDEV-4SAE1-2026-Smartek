package com.smartek.trainingservice.client;

import lombok.Data;
import java.util.List;

@Data
public class CourseResponse {
    private Long courseId;
    private String title;
    private String content;
    private String duration;
    private List<ChapterResponse> chapters;
}
