package com.smartek.planning.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyPlanningItem {
    private Long planningId;
    private String type;
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
    private Boolean published;
    
    // Propriétés pour les inscriptions
    private String registrationStatus; // REGISTERED, WAITING_LIST, CANCELLED
    private Integer waitingListPosition;
    private Boolean isRegistered;
}