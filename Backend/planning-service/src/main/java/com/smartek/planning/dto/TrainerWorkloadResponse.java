package com.smartek.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkloadResponse {
    private Long trainerId;
    private LocalDate date;
    private int totalHours;
    private int totalMinutes;
    private int sessionCount;
    private boolean overloaded; // Renommé sans "is" pour éviter les problèmes Lombok
    private int maxDailyHours = 8; // Limite configurable
    private String warning; // Message d'avertissement si surcharge
}
