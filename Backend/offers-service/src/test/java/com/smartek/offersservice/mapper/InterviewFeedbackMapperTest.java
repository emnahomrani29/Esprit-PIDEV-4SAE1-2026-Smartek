package com.smartek.offersservice.mapper;

import com.smartek.offersservice.dto.InterviewFeedbackResponse;
import com.smartek.offersservice.entity.InterviewFeedback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InterviewFeedbackMapper — Tests unitaires")
class InterviewFeedbackMapperTest {

    private InterviewFeedbackMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new InterviewFeedbackMapper();
    }

    @Test
    @DisplayName("toResponse() mappe tous les champs correctement")
    void toResponse_shouldMapAllFields() {
        LocalDateTime now = LocalDateTime.now();
        InterviewFeedback feedback = InterviewFeedback.builder()
                .id(1L)
                .interviewId(10L)
                .applicationId(5L)
                .rating(4)
                .strengths("Excellente communication")
                .weaknesses("Peu d'expérience en microservices")
                .generalComment("Candidat prometteur")
                .decision(InterviewFeedback.FeedbackDecision.HIRED)
                .submittedBy(3L)
                .build();
        // Simuler @PrePersist
        feedback.setCreatedAt(now);

        InterviewFeedbackResponse response = mapper.toResponse(feedback);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getInterviewId()).isEqualTo(10L);
        assertThat(response.getApplicationId()).isEqualTo(5L);
        assertThat(response.getRating()).isEqualTo(4);
        assertThat(response.getStrengths()).isEqualTo("Excellente communication");
        assertThat(response.getWeaknesses()).isEqualTo("Peu d'expérience en microservices");
        assertThat(response.getGeneralComment()).isEqualTo("Candidat prometteur");
        assertThat(response.getDecision()).isEqualTo("HIRED");
        assertThat(response.getSubmittedBy()).isEqualTo(3L);
        assertThat(response.getCreatedAt()).isEqualTo(now);
    }

    @ParameterizedTest(name = "Décision {0} → mappée correctement")
    @EnumSource(InterviewFeedback.FeedbackDecision.class)
    @DisplayName("toResponse() mappe toutes les décisions correctement")
    void toResponse_shouldMapAllDecisions(InterviewFeedback.FeedbackDecision decision) {
        InterviewFeedback feedback = InterviewFeedback.builder()
                .id(1L).interviewId(10L).applicationId(5L)
                .rating(3).decision(decision).submittedBy(3L).build();

        InterviewFeedbackResponse response = mapper.toResponse(feedback);

        assertThat(response.getDecision()).isEqualTo(decision.name());
    }

    @Test
    @DisplayName("toResponse() retourne null pour la décision si decision est null")
    void toResponse_shouldReturnNullDecision_whenDecisionIsNull() {
        InterviewFeedback feedback = new InterviewFeedback();
        feedback.setId(2L);
        feedback.setInterviewId(11L);
        feedback.setApplicationId(6L);
        feedback.setRating(3);
        feedback.setSubmittedBy(4L);
        // decision intentionnellement null

        InterviewFeedbackResponse response = mapper.toResponse(feedback);

        assertThat(response.getDecision()).isNull();
    }

    @Test
    @DisplayName("toResponse() gère les champs optionnels null")
    void toResponse_shouldHandleNullOptionalFields() {
        InterviewFeedback feedback = InterviewFeedback.builder()
                .id(3L).interviewId(12L).applicationId(7L)
                .rating(2).decision(InterviewFeedback.FeedbackDecision.REJECTED)
                .submittedBy(5L).build();
        // strengths, weaknesses, generalComment sont null

        InterviewFeedbackResponse response = mapper.toResponse(feedback);

        assertThat(response.getStrengths()).isNull();
        assertThat(response.getWeaknesses()).isNull();
        assertThat(response.getGeneralComment()).isNull();
    }
}
