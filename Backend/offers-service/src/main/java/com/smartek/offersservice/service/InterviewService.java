package com.smartek.offersservice.service;

import com.smartek.offersservice.dto.InterviewRequest;
import com.smartek.offersservice.dto.InterviewResponse;
import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.entity.Interview;
import com.smartek.offersservice.repository.ApplicationRepository;
import com.smartek.offersservice.repository.InterviewRepository;
import com.smartek.offersservice.exception.BusinessException;
import com.smartek.offersservice.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {
    
    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    
    @Transactional
    public InterviewResponse createInterview(InterviewRequest request) {
        log.info("Creating interview for application ID: {}", request.getApplicationId());
        
        // Vérifier que la candidature existe
        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée avec l'id: " + request.getApplicationId()));
        
        // Vérifier que la candidature est acceptée
        if (!"ACCEPTED".equals(application.getStatus())) {
            throw new BusinessException("La candidature doit être acceptée pour planifier un entretien");
        }
        
        Interview interview = Interview.builder()
                .applicationId(request.getApplicationId())
                .offerId(application.getOfferId())
                .learnerId(application.getLearnerId())
                .learnerName(application.getLearnerName())
                .learnerEmail(application.getLearnerEmail())
                .interviewDate(request.getInterviewDate())
                .location(request.getLocation())
                .meetingLink(request.getMeetingLink())
                .notes(request.getNotes())
                .status(Interview.InterviewStatus.SCHEDULED)
                .createdBy(request.getCreatedBy())
                .build();
        
        Interview savedInterview = interviewRepository.save(interview);
        log.info("Interview created successfully with ID: {}", savedInterview.getId());
        
        return InterviewResponse.fromEntity(savedInterview);
    }
    
    public List<InterviewResponse> getInterviewsByOffer(Long offerId) {
        log.info("Fetching interviews for offer ID: {}", offerId);
        return interviewRepository.findByOfferId(offerId).stream()
                .map(InterviewResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<InterviewResponse> getInterviewsByLearner(Long learnerId) {
        log.info("Fetching interviews for learner ID: {}", learnerId);
        return interviewRepository.findByLearnerId(learnerId).stream()
                .map(InterviewResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<InterviewResponse> getInterviewsByApplication(Long applicationId) {
        log.info("Fetching interviews for application ID: {}", applicationId);
        return interviewRepository.findByApplicationId(applicationId).stream()
                .map(InterviewResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<InterviewResponse> getAllInterviews() {
        log.info("Fetching all interviews");
        return interviewRepository.findAll().stream()
                .map(InterviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<InterviewResponse> getInterviewsByCompany(Long companyId) {
        log.info("Fetching interviews for company ID: {}", companyId);
        return interviewRepository.findByOffer_CompanyId(companyId).stream()
                .map(InterviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public InterviewResponse updateInterviewStatus(Long interviewId, String status) {
        log.info("Updating interview ID: {} to status: {}", interviewId, status);
        
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Entretien non trouvé avec l'id: " + interviewId));
        
        interview.setStatus(Interview.InterviewStatus.valueOf(status));
        Interview updatedInterview = interviewRepository.save(interview);
        
        log.info("Interview status updated successfully");
        return InterviewResponse.fromEntity(updatedInterview);
    }
    
    @Transactional
    public InterviewResponse updateInterview(Long interviewId, InterviewRequest request) {
        log.info("Updating interview ID: {}", interviewId);
        
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Entretien non trouvé avec l'id: " + interviewId));
        
        interview.setInterviewDate(request.getInterviewDate());
        interview.setLocation(request.getLocation());
        interview.setMeetingLink(request.getMeetingLink());
        interview.setNotes(request.getNotes());
        
        Interview updatedInterview = interviewRepository.save(interview);
        
        log.info("Interview updated successfully");
        return InterviewResponse.fromEntity(updatedInterview);
    }
    
    @Transactional
    public void deleteInterview(Long interviewId) {
        log.info("Deleting interview ID: {}", interviewId);
        interviewRepository.deleteById(interviewId);
        log.info("Interview deleted successfully");
    }
}
