package com.smartek.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyPlanningResponse {
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private Long trainerId;
    private List<WeeklyPlanningItem> items;
    private WeeklyStats stats;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyPlanningItem {
        private Long planningId;
        private String type; // TRAINING, EXAM, EVENT
        private Long itemId;
        private String title;
        private String description;
        private LocalDate date;
        private String startTime;
        private String endTime;
        private String location;
        private String color;
        private Integer maxParticipants;
        private Integer currentParticipants;
        private String status;
        private boolean published;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyStats {
        private int totalSessions;
        private int trainingSessions;
        private int examSessions;
        private int eventSessions;
        private int publishedSessions;
        private int draftSessions;
        private double totalHours;
    }
}