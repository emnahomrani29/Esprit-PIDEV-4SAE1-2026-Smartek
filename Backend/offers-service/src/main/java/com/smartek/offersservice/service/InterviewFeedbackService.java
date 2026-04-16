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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewFeedbackService {

    private final InterviewFeedbackRepository feedbackRepository;
    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional
    public InterviewFeedbackResponse submitFeedback(InterviewFeedbackRequest request) {
        Interview interview = interviewRepository.findById(request.getInterviewId())
                .orElseThrow(() -> new ResourceNotFoundException("Entretien non trouvé"));

        // Auto-complete SCHEDULED interviews when feedback is submitted
        if (interview.getStatus() == Interview.InterviewStatus.SCHEDULED) {
            interview.setStatus(Interview.InterviewStatus.COMPLETED);
            interviewRepository.save(interview);
            log.info("Interview {} auto-completed when feedback submitted", interview.getId());
        } else if (interview.getStatus() != Interview.InterviewStatus.COMPLETED) {
            throw new BusinessException("Le feedback ne peut être soumis que pour un entretien terminé");
        }

        if (feedbackRepository.findByInterviewId(request.getInterviewId()).isPresent()) {
            throw new BusinessException("Un feedback existe déjà pour cet entretien");
        }

        InterviewFeedback feedback = InterviewFeedback.builder()
                .interviewId(request.getInterviewId())
                .applicationId(request.getApplicationId())
                .rating(request.getRating())
                .strengths(request.getStrengths())
                .weaknesses(request.getWeaknesses())
                .generalComment(request.getGeneralComment())
                .decision(InterviewFeedback.FeedbackDecision.valueOf(request.getDecision()))
                .submittedBy(request.getSubmittedBy())
                .build();

        InterviewFeedback saved = feedbackRepository.save(feedback);

        // Si décision HIRED ou REJECTED, mettre à jour le statut de la candidature
        if (feedback.getDecision() == InterviewFeedback.FeedbackDecision.HIRED) {
            updateApplicationStatus(request.getApplicationId(), "ACCEPTED");
        } else if (feedback.getDecision() == InterviewFeedback.FeedbackDecision.REJECTED) {
            updateApplicationStatus(request.getApplicationId(), "REJECTED");
        }

        log.info("Feedback submitted for interview {}", request.getInterviewId());
        return InterviewFeedbackResponse.fromEntity(saved);
    }

    public InterviewFeedbackResponse getFeedbackByInterview(Long interviewId) {
        return feedbackRepository.findByInterviewId(interviewId)
                .map(InterviewFeedbackResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback non trouvé pour l'entretien: " + interviewId));
    }

    public List<InterviewFeedbackResponse> getFeedbacksByApplication(Long applicationId) {
        return feedbackRepository.findByApplicationId(applicationId).stream()
                .map(InterviewFeedbackResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private void updateApplicationStatus(Long applicationId, String status) {
        applicationRepository.findById(applicationId).ifPresent(app -> {
            app.setStatus(status);
            applicationRepository.save(app);
            log.info("Application {} status updated to {}", applicationId, status);
        });
    }
}
