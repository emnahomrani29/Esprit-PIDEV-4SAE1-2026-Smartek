package com.smartek.offersservice.service;

import com.smartek.offersservice.dto.ApplicationRequest;
import com.smartek.offersservice.dto.ApplicationResponse;
import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.entity.Offer;
import com.smartek.offersservice.exception.BusinessException;
import com.smartek.offersservice.exception.ResourceNotFoundException;
import com.smartek.offersservice.mapper.ApplicationMapper;
import com.smartek.offersservice.repository.ApplicationRepository;
import com.smartek.offersservice.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final OfferRepository offerRepository;
    private final ApplicationMapper applicationMapper;
    private final ApplicationScoringService scoringService;

    @Transactional
    public ApplicationResponse applyToOffer(ApplicationRequest request) {
        if (applicationRepository.existsByOfferIdAndLearnerId(request.getOfferId(), request.getLearnerId())) {
            throw new BusinessException("Vous avez déjà postulé à cette offre");
        }

        Offer offer = offerRepository.findById(request.getOfferId())
                .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée: " + request.getOfferId()));

        if (!"ACTIVE".equals(offer.getStatus())) {
            throw new BusinessException("Cette offre n'est plus disponible");
        }

        Application application = applicationMapper.toEntity(request);
        application.setOffer(offer);

        int score = scoringService.calculateScore(application, offer, request.getYearsOfExperience());
        application.setScore(score);

        Application saved = applicationRepository.save(application);
        return applicationMapper.toResponse(saved);
    }

    public List<ApplicationResponse> getApplicationsByOffer(Long offerId) {
        return applicationRepository.findByOfferId(offerId).stream()
                .map(applicationMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByOfferSortedByScore(Long offerId) {
        return applicationRepository.findByOfferIdOrderByScoreDesc(offerId).stream()
                .map(applicationMapper::toResponse).collect(Collectors.toList());
    }

    public List<ApplicationResponse> getApplicationsByLearner(Long learnerId) {
        return applicationRepository.findByLearnerId(learnerId).stream()
                .map(applicationMapper::toResponse).collect(Collectors.toList());
    }

    public boolean hasApplied(Long offerId, Long learnerId) {
        return applicationRepository.existsByOfferIdAndLearnerId(offerId, learnerId);
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId, String status, String recruiterNote) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée: " + applicationId));
        application.setStatus(status);
        if (recruiterNote != null) application.setRecruiterNote(recruiterNote);
        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    @Transactional
    public ApplicationResponse withdrawApplication(Long applicationId, Long learnerId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée: " + applicationId));
        if (!application.getLearnerId().equals(learnerId)) {
            throw new BusinessException("Vous ne pouvez pas retirer cette candidature");
        }
        application.setStatus("WITHDRAWN");
        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getMatchAnalysis(Long applicationId, Integer yearsOfExperience) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée: " + applicationId));
        Offer offer = application.getOffer();
        if (offer == null && application.getOfferId() != null) {
            offer = offerRepository.findById(application.getOfferId()).orElse(null);
        }
        if (offer == null) {
            return java.util.Map.of("error", "Offre non trouvée");
        }
        // Force l'initialisation de requiredSkills pour éviter LazyInitializationException
        if (offer.getRequiredSkills() != null) {
            offer.getRequiredSkills().size();
        }
        return scoringService.analyzeMatch(application, offer, yearsOfExperience);
    }
}
