package com.smartek.offersservice.service;

import com.smartek.offersservice.dto.InterviewFeedbackRequest;
import com.smartek.offersservice.dto.InterviewFeedbackResponse;
import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.entity.Interview;
import com.smartek.offersservice.entity.InterviewFeedback;
import com.smartek.offersservice.exception.BusinessException;
import com.smartek.offersservice.exception.ResourceNotFoundException;
import com.smartek.offersservice.repository.ApplicationRepository;
import com.smartek.offersservice.repository.InterviewFeedbackRepository;
import com.smartek.offersservice.repository.InterviewRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InterviewFeedbackService Unit Tests")
class InterviewFeedbackServiceTest {

    @Mock private InterviewFeedbackRepository feedbackRepository;
    @Mock private InterviewRepository interviewRepository;
    @Mock private ApplicationRepository applicationRepository;

    @InjectMocks private InterviewFeedbackService feedbackService;

    private Interview completedInterview;
    private Interview scheduledInterview;
    private Application application;
    private InterviewFeedbackRequest validRequest;
    private InterviewFeedback savedFeedback;

    @BeforeEach
    void setUp() {
        completedInterview = Interview.builder()
                .id(10L).applicationId(1L).offerId(100L).learnerId(2L)
                .status(Interview.InterviewStatus.COMPLETED).build();

        scheduledInterview = Interview.builder()
                .id(11L).applicationId(2L).offerId(100L).learnerId(3L)
                .status(Interview.InterviewStatus.SCHEDULED).build();

        application = new Application();
        application.setId(1L);
        application.setOfferId(100L);
        application.setLearnerId(2L);
        application.setStatus("PENDING");

        validRequest = new InterviewFeedbackRequest();
        validRequest.setInterviewId(10L);
        validRequest.setApplicationId(1L);
        validRequest.setRating(4);
        validRequest.setStrengths("Excellent communication");
        validRequest.setWeaknesses("Needs more Java experience");
        validRequest.setGeneralComment("Good candidate overall");
        validRequest.setDecision("HIRED");
        validRequest.setSubmittedBy(5L);

        savedFeedback = InterviewFeedback.builder()
                .id(100L).interviewId(10L).applicationId(1L)
                .rating(4).decision(InterviewFeedback.FeedbackDecision.HIRED)
                .submittedBy(5L).build();
    }

    @Nested
    @DisplayName("Submit Feedback")
    class SubmitFeedbackTests {

        @Test
        @DisplayName("COMPLETED interview → feedback submitted successfully")
        void completedInterview_feedbackSubmitted() {
            when(interviewRepository.findById(10L)).thenReturn(Optional.of(completedInterview));
            when(feedbackRepository.findByInterviewId(10L)).thenReturn(Optional.empty());
            when(feedbackRepository.save(any())).thenReturn(savedFeedback);
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
            when(applicationRepository.save(any())).thenReturn(application);

            InterviewFeedbackResponse result = feedbackService.submitFeedback(validRequest);

            assertThat(result).isNotNull();
            assertThat(result.getInterviewId()).isEqualTo(10L);
            verify(feedbackRepository).save(any());
        }

        @Test
        @DisplayName("SCHEDULED interview → auto-completed and feedback submitted")
        void scheduledInterview_autoCompletedAndFeedbackSubmitted() {
            validRequest.setInterviewId(11L);
            validRequest.setApplicationId(2L);

            Application app2 = new Application();
            app2.setId(2L);
            app2.setStatus("PENDING");

            InterviewFeedback scheduledFeedback = InterviewFeedback.builder()
                    .id(103L).interviewId(11L).applicationId(2L)
                    .rating(4).decision(InterviewFeedback.FeedbackDecision.HIRED)
                    .submittedBy(5L).build();

            when(interviewRepository.findById(11L)).thenReturn(Optional.of(scheduledInterview));
            when(interviewRepository.save(any())).thenReturn(scheduledInterview);
            when(feedbackRepository.findByInterviewId(11L)).thenReturn(Optional.empty());
            when(feedbackRepository.save(any())).thenReturn(scheduledFeedback);
            when(applicationRepository.findById(2L)).thenReturn(Optional.of(app2));
            when(applicationRepository.save(any())).thenReturn(app2);

            InterviewFeedbackResponse result = feedbackService.submitFeedback(validRequest);

            assertThat(result).isNotNull();
            verify(interviewRepository).save(any()); // auto-complete
            verify(feedbackRepository).save(any());
        }

        @Test
        @DisplayName("Duplicate feedback → BusinessException")
        void duplicateFeedback_throwsBusinessException() {
            when(interviewRepository.findById(10L)).thenReturn(Optional.of(completedInterview));
            when(feedbackRepository.findByInterviewId(10L)).thenReturn(Optional.of(savedFeedback));

            assertThatThrownBy(() -> feedbackService.submitFeedback(validRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("feedback existe déjà");

            verify(feedbackRepository, never()).save(any());
        }

        @Test
        @DisplayName("Interview not found → ResourceNotFoundException")
        void interviewNotFound_throwsResourceNotFoundException() {
            when(interviewRepository.findById(99L)).thenReturn(Optional.empty());
            validRequest.setInterviewId(99L);

            assertThatThrownBy(() -> feedbackService.submitFeedback(validRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Decision → Application Status Sync")
    class DecisionSyncTests {

        @Test
        @DisplayName("HIRED → application set to ACCEPTED")
        void hiredDecision_applicationSetToAccepted() {
            validRequest.setDecision("HIRED");
            when(interviewRepository.findById(10L)).thenReturn(Optional.of(completedInterview));
            when(feedbackRepository.findByInterviewId(10L)).thenReturn(Optional.empty());
            when(feedbackRepository.save(any())).thenReturn(savedFeedback);
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
            when(applicationRepository.save(any())).thenReturn(application);

            feedbackService.submitFeedback(validRequest);

            ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
            verify(applicationRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo("ACCEPTED");
        }

        @Test
        @DisplayName("REJECTED → application set to REJECTED")
        void rejectedDecision_applicationSetToRejected() {
            validRequest.setDecision("REJECTED");
            InterviewFeedback rejectedFeedback = InterviewFeedback.builder()
                    .id(101L).interviewId(10L).applicationId(1L)
                    .rating(2).decision(InterviewFeedback.FeedbackDecision.REJECTED)
                    .submittedBy(5L).build();

            when(interviewRepository.findById(10L)).thenReturn(Optional.of(completedInterview));
            when(feedbackRepository.findByInterviewId(10L)).thenReturn(Optional.empty());
            when(feedbackRepository.save(any())).thenReturn(rejectedFeedback);
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
            when(applicationRepository.save(any())).thenReturn(application);

            feedbackService.submitFeedback(validRequest);

            ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
            verify(applicationRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo("REJECTED");
        }

        @Test
        @DisplayName("PENDING → application status NOT changed")
        void pendingDecision_applicationStatusUnchanged() {
            validRequest.setDecision("PENDING");
            InterviewFeedback pendingFeedback = InterviewFeedback.builder()
                    .id(102L).interviewId(10L).applicationId(1L)
                    .rating(3).decision(InterviewFeedback.FeedbackDecision.PENDING)
                    .submittedBy(5L).build();

            when(interviewRepository.findById(10L)).thenReturn(Optional.of(completedInterview));
            when(feedbackRepository.findByInterviewId(10L)).thenReturn(Optional.empty());
            when(feedbackRepository.save(any())).thenReturn(pendingFeedback);

            feedbackService.submitFeedback(validRequest);

            verify(applicationRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("getFeedbackByInterview — found → returns response")
    void getFeedbackByInterview_found() {
        when(feedbackRepository.findByInterviewId(10L)).thenReturn(Optional.of(savedFeedback));

        InterviewFeedbackResponse result = feedbackService.getFeedbackByInterview(10L);

        assertThat(result).isNotNull();
        assertThat(result.getInterviewId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getFeedbackByInterview — not found → ResourceNotFoundException")
    void getFeedbackByInterview_notFound() {
        when(feedbackRepository.findByInterviewId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> feedbackService.getFeedbackByInterview(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getFeedbacksByApplication → returns list")
    void getFeedbacksByApplication_returnsList() {
        when(feedbackRepository.findByApplicationId(1L)).thenReturn(List.of(savedFeedback));

        List<InterviewFeedbackResponse> result = feedbackService.getFeedbacksByApplication(1L);

        assertThat(result).hasSize(1);
    }
}
