package com.smartek.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotSuggestionRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private int durationMinutes; // Durée souhaitée en minutes
    private Long trainerId; // ID du formateur (optionnel)
    private String roomId; // ID de la salle (optionnel)
    private int maxSuggestions = 5; // Nombre maximum de suggestions
}
