package com.smartek.learningmicroservice.dto;

import com.smartek.learningmicroservice.entity.LearningStyleType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningStylePreferenceRequest {

    @NotNull(message = "Preferred learning style is required")
    private LearningStyleType preferredStyle;

    private Boolean videoPreferred = false;
    private Boolean textPreferred = false;
    private Boolean practicalWorkPreferred = false;

    @NotNull(message = "Learner ID is required")
    private Long learnerId;

    private String learnerName;  // optionnel si tu le récupères côté backend via auth
}
