package com.smartek.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyPlanningRequest {
    private LocalDate weekStartDate;
    private Long trainerId;
    private List<WeeklyPlanningItem> items;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyPlanningItem {
        private String type; // TRAINING, EXAM, EVENT
        private Long itemId; // ID du training, exam ou event
        private String title;
        private String description;
        private LocalDate date;
        private String startTime;
        private String endTime;
        private String location;
        private String color;
        private Integer maxParticipants;
        private String status; // DRAFT, PUBLISHED
    }
}