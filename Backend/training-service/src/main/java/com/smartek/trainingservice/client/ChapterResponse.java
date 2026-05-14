package com.smartek.trainingservice.client;

import lombok.Data;

@Data
public class ChapterResponse {
    private Long chapterId;
    private String title;
    private String description;
    private Integer orderIndex;
    private String pdfFileName;
    private String pdfFilePath;
}
