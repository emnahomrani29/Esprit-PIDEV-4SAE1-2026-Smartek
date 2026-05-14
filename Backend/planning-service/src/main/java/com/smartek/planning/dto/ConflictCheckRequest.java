package com.smartek.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConflictCheckRequest {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long trainerId; // ID du formateur
    private String roomId; // ID de la salle
    private Long excludePlanningId; // Pour exclure un planning lors de la modification
}
