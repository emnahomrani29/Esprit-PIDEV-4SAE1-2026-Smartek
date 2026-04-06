package com.smartek.learningmicroservice.dto;

import com.smartek.learningmicroservice.entity.LearningStyleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningStylePreferenceResponse {

    private Long id;
    private LearningStyleType preferredStyle;
    private Boolean videoPreferred;
    private Boolean textPreferred;
    private Boolean practicalWorkPreferred;
    private LocalDateTime lastUpdated;
    private Long learnerId;
    private String learnerName;
}
