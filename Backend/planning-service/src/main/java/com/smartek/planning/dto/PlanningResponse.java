package com.smartek.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanningResponse {
    private Long planningId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String title;
    private String description;
    private String eventType;
    private String location;
    private String color;
}
