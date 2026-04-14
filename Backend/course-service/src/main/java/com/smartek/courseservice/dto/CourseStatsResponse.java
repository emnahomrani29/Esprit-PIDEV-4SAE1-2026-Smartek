package com.smartek.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseStatsResponse {
    private Long userId;
    private Integer totalEnrolled;
    private Integer inProgress;
    private Integer completed;
    private Double completionRate;
    private Integer totalChapters;
    private Integer completedChapters;
}
