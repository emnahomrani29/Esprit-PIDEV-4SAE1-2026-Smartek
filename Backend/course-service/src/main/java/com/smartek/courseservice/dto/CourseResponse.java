package com.smartek.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {
    private Long courseId;
    private String title;
    private String content;
    private LocalDate duration;
    private Long trainerId;
    private List<ChapterResponse> chapters;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;
}
