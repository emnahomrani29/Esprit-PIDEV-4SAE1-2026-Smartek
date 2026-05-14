package com.smartek.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotSuggestion {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private int score; // Score de qualité du créneau (0-100)
    private String reason; // Raison du score (ex: "Pas de conflit, créneau optimal")
}
