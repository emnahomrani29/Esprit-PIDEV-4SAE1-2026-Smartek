package com.smartek.trainingservice.dto;

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
public class TrainingResponse {
    private Long trainingId;
    private String title;
    private String description;
    private String category;
    private String level;
    private LocalDate duration;
    private List<Long> courseIds;
    private List<CourseInfo> courses; // Informations détaillées des cours
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CourseInfo {
        private Long courseId;
        private String title;
        private String content;
        private LocalDate duration;
        private List<ChapterInfo> chapters;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChapterInfo {
        private Long chapterId;
        private String title;
        private String description;
        private Integer orderIndex;
        private String pdfFileName;
        private String pdfFilePath;
    }
}
