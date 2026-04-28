package com.smartek.offersservice.mapper;

import com.smartek.offersservice.dto.InterviewResponse;
import com.smartek.offersservice.entity.Interview;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InterviewMapper — Tests unitaires")
class InterviewMapperTest {

    private InterviewMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new InterviewMapper();
    }

    @Test
    @DisplayName("toResponse() mappe tous les champs correctement")
    void toResponse_shouldMapAllFields() {
        LocalDateTime interviewDate = LocalDateTime.now().plusDays(3);
        LocalDateTime now = LocalDateTime.now();

        Interview interview = Interview.builder()
                .id(10L)
                .applicationId(1L)
                .offerId(100L)
                .learnerId(2L)
                .learnerName("Bob Dupont")
                .learnerEmail("bob@test.com")
                .interviewDate(interviewDate)
                .location("Salle A")
                .meetingLink("https://meet.google.com/abc")
                .notes("Préparer les questions Java")
                .status(Interview.InterviewStatus.SCHEDULED)
                .createdBy(5L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        InterviewResponse response = mapper.toResponse(interview);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getApplicationId()).isEqualTo(1L);
        assertThat(response.getOfferId()).isEqualTo(100L);
        assertThat(response.getLearnerId()).isEqualTo(2L);
        assertThat(response.getLearnerName()).isEqualTo("Bob Dupont");
        assertThat(response.getLearnerEmail()).isEqualTo("bob@test.com");
        assertThat(response.getInterviewDate()).isEqualTo(interviewDate);
        assertThat(response.getLocation()).isEqualTo("Salle A");
        assertThat(response.getMeetingLink()).isEqualTo("https://meet.google.com/abc");
        assertThat(response.getNotes()).isEqualTo("Préparer les questions Java");
        assertThat(response.getStatus()).isEqualTo("SCHEDULED");
        assertThat(response.getCreatedBy()).isEqualTo(5L);
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("toResponse() mappe le statut COMPLETED correctement")
    void toResponse_shouldMapCompletedStatus() {
        Interview interview = Interview.builder()
                .id(11L).applicationId(2L).offerId(101L).learnerId(3L)
                .learnerName("Alice").learnerEmail("alice@test.com")
                .interviewDate(LocalDateTime.now())
                .location("En ligne").status(Interview.InterviewStatus.COMPLETED)
                .createdBy(5L).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        InterviewResponse response = mapper.toResponse(interview);

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("toResponse() mappe le statut CANCELLED correctement")
    void toResponse_shouldMapCancelledStatus() {
        Interview interview = Interview.builder()
                .id(12L).applicationId(3L).offerId(102L).learnerId(4L)
                .learnerName("Carol").learnerEmail("carol@test.com")
                .interviewDate(LocalDateTime.now())
                .location("Paris").status(Interview.InterviewStatus.CANCELLED)
                .createdBy(5L).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        InterviewResponse response = mapper.toResponse(interview);

        assertThat(response.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("toResponse() retourne null pour le statut si status est null")
    void toResponse_shouldReturnNullStatus_whenStatusIsNull() {
        Interview interview = new Interview();
        interview.setId(13L);
        interview.setApplicationId(4L);
        interview.setOfferId(103L);
        interview.setLearnerId(5L);
        interview.setLearnerName("Dave");
        interview.setLearnerEmail("dave@test.com");
        interview.setInterviewDate(LocalDateTime.now());
        interview.setLocation("Lyon");
        interview.setCreatedBy(5L);
        interview.setCreatedAt(LocalDateTime.now());
        interview.setUpdatedAt(LocalDateTime.now());
        // status intentionnellement null

        InterviewResponse response = mapper.toResponse(interview);

        assertThat(response.getStatus()).isNull();
    }

    @Test
    @DisplayName("toResponse() gère meetingLink null")
    void toResponse_shouldHandleNullMeetingLink() {
        Interview interview = Interview.builder()
                .id(14L).applicationId(5L).offerId(104L).learnerId(6L)
                .learnerName("Eve").learnerEmail("eve@test.com")
                .interviewDate(LocalDateTime.now())
                .location("Salle B").status(Interview.InterviewStatus.SCHEDULED)
                .createdBy(5L).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        InterviewResponse response = mapper.toResponse(interview);

        assertThat(response.getMeetingLink()).isNull();
        assertThat(response.getNotes()).isNull();
    }
}
