package com.smartek.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterResponse {
    
    private Long chapterId;
    private String title;
    private String description;
    private Integer orderIndex;
    private String pdfFileName;
    private String pdfFilePath;
    private Long courseId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;
}
