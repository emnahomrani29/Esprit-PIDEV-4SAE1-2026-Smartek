package com.smartek.learningmicroservice.dto;

import com.smartek.learningmicroservice.entity.LearningPathStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    private String description;

    @NotNull(message = "L'ID de l'apprenant est obligatoire")
    private Long learnerId;

    @NotBlank(message = "Le nom de l'apprenant est obligatoire")
    private String learnerName;

    @NotNull(message = "Le statut est obligatoire")
    private LearningPathStatus status;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Le progrès est obligatoire")
    @Min(value = 0, message = "Le progrès doit être entre 0 et 100")
    @Max(value = 100, message = "Le progrès doit être entre 0 et 100")
    private Integer progress;
}
