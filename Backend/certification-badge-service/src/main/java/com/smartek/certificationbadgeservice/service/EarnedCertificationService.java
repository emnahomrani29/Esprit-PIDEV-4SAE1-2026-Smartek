package com.smartek.certificationbadgeservice.service;

import com.smartek.certificationbadgeservice.dto.*;
import com.smartek.certificationbadgeservice.entity.CertificationTemplate;
import com.smartek.certificationbadgeservice.entity.EarnedCertification;
import com.smartek.certificationbadgeservice.exception.ResourceNotFoundException;
import com.smartek.certificationbadgeservice.exception.ValidationException;
import com.smartek.certificationbadgeservice.mapper.EarnedCertificationMapper;
import com.smartek.certificationbadgeservice.repository.CertificationTemplateRepository;
import com.smartek.certificationbadgeservice.repository.EarnedCertificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EarnedCertificationService {
    
    private final EarnedCertificationRepository earnedCertificationRepository;
    private final CertificationTemplateRepository certificationTemplateRepository;
    private final EarnedCertificationMapper earnedCertificationMapper;
    
    @Transactional(propagation = Propagation.REQUIRED)
    public EarnedCertificationDTO awardCertification(AwardCertificationRequestDTO request) {
        MDC.put("operation", "AWARD_CERTIFICATION");
        MDC.put("userId", String.valueOf(request.getAwardedBy()));
        try {
            log.info("Awarding certification template {} to learner {}", 
                    request.getCertificationTemplateId(), request.getLearnerId());
            
            // Validate certification template exists
            CertificationTemplate certificationTemplate = certificationTemplateRepository.findById(request.getCertificationTemplateId())
                    .orElseThrow(() -> {
                        log.warn("Certification template not found with id: {}", request.getCertificationTemplateId());
                        return new ResourceNotFoundException("Certification template not found with id: " + request.getCertificationTemplateId());
                    });
            
            // Validate dates
            if (request.getExpiryDate() != null && request.getIssueDate() != null 
                    && request.getExpiryDate().isBefore(request.getIssueDate())) {
                log.warn("Invalid date range: expiry date {} is before issue date {}", 
                        request.getExpiryDate(), request.getIssueDate());
                throw new ValidationException("Expiry date cannot be before issue date");
            }
            
            // Validate certificate URL if provided
            if (request.getCertificateUrl() != null && !request.getCertificateUrl().trim().isEmpty()) {
                validateUrl(request.getCertificateUrl());
            }
            
            // Create earned certification
            EarnedCertification earnedCertification = new EarnedCertification();
            earnedCertification.setCertificationTemplate(certificationTemplate);
            earnedCertification.setLearnerId(request.getLearnerId());
            earnedCertification.setIssueDate(request.getIssueDate());
            earnedCertification.setExpiryDate(request.getExpiryDate());
            earnedCertification.setCertificateUrl(request.getCertificateUrl());
            earnedCertification.setAwardedBy(request.getAwardedBy());
            
            EarnedCertification saved = earnedCertificationRepository.save(earnedCertification);
            
            log.info("Successfully awarded certification {} to learner {} by user {}", 
                    request.getCertificationTemplateId(), request.getLearnerId(), request.getAwardedBy());
            return earnedCertificationMapper.toDTO(saved);
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error awarding certification {} to learner {}", 
                    request.getCertificationTemplateId(), request.getLearnerId(), e);
            throw e;
        } finally {
            MDC.remove("operation");
            MDC.remove("userId");
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public EarnedCertificationDTO autoAwardCertification(Long certificationTemplateId,
                                                         Long learnerId,
                                                         java.time.LocalDate issueDate,
                                                         String examId) {
        MDC.put("operation", "AUTO_AWARD_CERTIFICATION");
        MDC.put("examId", examId);
        try {
            log.info("Auto-awarding certification template {} to learner {} (examId={})",
                    certificationTemplateId, learnerId, examId);
            
            // Prevent duplicates
            if (earnedCertificationRepository.existsByCertificationTemplate_IdAndLearnerId(certificationTemplateId, learnerId)) {
                log.warn("Certification template {} already awarded to learner {}", certificationTemplateId, learnerId);
                throw new ValidationException("Certification already awarded to this learner");
            }
            
            // Validate certification template exists
            CertificationTemplate certificationTemplate = certificationTemplateRepository.findById(certificationTemplateId)
                    .orElseThrow(() -> new ResourceNotFoundException("Certification template not found with id: " + certificationTemplateId));
            
            // Create earned certification
            EarnedCertification earnedCertification = new EarnedCertification();
            earnedCertification.setCertificationTemplate(certificationTemplate);
            earnedCertification.setLearnerId(learnerId);
            earnedCertification.setIssueDate(issueDate);
            earnedCertification.setExamId(examId);
            earnedCertification.setAwardedBy(0L); // SYSTEM
            
            EarnedCertification saved = earnedCertificationRepository.save(earnedCertification);
            log.info("Auto-awarded certification {} to learner {} (examId={})", certificationTemplateId, learnerId, examId);
            return earnedCertificationMapper.toDTO(saved);
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error auto-awarding certification {} to learner {} (examId={})", certificationTemplateId, learnerId, examId, e);
            throw e;
        } finally {
            MDC.remove("operation");
            MDC.remove("examId");
        }
    }
    
    @Transactional
    public BulkAwardResponseDTO bulkAwardCertifications(BulkAwardCertificationRequestDTO request) {
        MDC.put("operation", "BULK_AWARD_CERTIFICATIONS");
        MDC.put("userId", String.valueOf(request.getAwardedBy()));
        try {
            log.info("Bulk awarding certification template {} to {} learners", 
                    request.getCertificationTemplateId(), request.getLearnerIds().size());
            
            List<AwardResultDTO> results = new ArrayList<>();
            
            // Validate certification template exists once
            if (!certificationTemplateRepository.existsById(request.getCertificationTemplateId())) {
                log.warn("Certification template not found with id: {}", request.getCertificationTemplateId());
                // If template doesn't exist, all awards fail
                for (Long learnerId : request.getLearnerIds()) {
                    results.add(AwardResultDTO.failure(learnerId, 
                        "Certification template not found with id: " + request.getCertificationTemplateId()));
                }
                return new BulkAwardResponseDTO(results);
            }
            
            // Validate dates once
            if (request.getExpiryDate() != null && request.getIssueDate() != null 
                    && request.getExpiryDate().isBefore(request.getIssueDate())) {
                log.warn("Invalid date range: expiry date {} is before issue date {}", 
                        request.getExpiryDate(), request.getIssueDate());
                // If dates are invalid, all awards fail
                for (Long learnerId : request.getLearnerIds()) {
                    results.add(AwardResultDTO.failure(learnerId, 
                        "Expiry date cannot be before issue date"));
                }
                return new BulkAwardResponseDTO(results);
            }
            
            // Validate certificate URL once if provided
            if (request.getCertificateUrl() != null && !request.getCertificateUrl().trim().isEmpty()) {
                try {
                    validateUrl(request.getCertificateUrl());
                } catch (ValidationException e) {
                    log.warn("Invalid certificate URL: {}", request.getCertificateUrl());
                    // If URL is invalid, all awards fail
                    for (Long learnerId : request.getLearnerIds()) {
                        results.add(AwardResultDTO.failure(learnerId, e.getMessage()));
                    }
                    return new BulkAwardResponseDTO(results);
                }
            }
            
            // Process each learner individually with independent transactions (savepoint-based)
            for (Long learnerId : request.getLearnerIds()) {
                try {
                    AwardCertificationRequestDTO individualRequest = new AwardCertificationRequestDTO();
                    individualRequest.setCertificationTemplateId(request.getCertificationTemplateId());
                    individualRequest.setLearnerId(learnerId);
                    individualRequest.setIssueDate(request.getIssueDate());
                    individualRequest.setExpiryDate(request.getExpiryDate());
                    individualRequest.setCertificateUrl(request.getCertificateUrl());
                    individualRequest.setAwardedBy(request.getAwardedBy());
                    
                    // Each award is in its own nested transaction (savepoint)
                    awardCertificationWithSavepoint(individualRequest);
                    results.add(AwardResultDTO.success(learnerId));
                    
                } catch (Exception e) {
                    log.error("Error awarding certification to learner {}", learnerId, e);
                    results.add(AwardResultDTO.failure(learnerId, "Failed to award certification: " + e.getMessage()));
                }
            }
            
            BulkAwardResponseDTO response = new BulkAwardResponseDTO(results);
            log.info("Bulk award completed: {} successful, {} failed", 
                    response.getSuccessCount(), response.getFailureCount());
            return response;
        } catch (Exception e) {
            log.error("Error in bulk award certifications operation", e);
            throw e;
        } finally {
            MDC.remove("operation");
            MDC.remove("userId");
        }
    }
    
    /**
     * Award certification with REQUIRES_NEW propagation to create a savepoint.
     * If this fails, only this individual award is rolled back, not the entire bulk operation.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected EarnedCertificationDTO awardCertificationWithSavepoint(AwardCertificationRequestDTO request) {
        // Validate certification template exists
        CertificationTemplate certificationTemplate = certificationTemplateRepository.findById(request.getCertificationTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Certification template not found with id: " + request.getCertificationTemplateId()));
        
        // Create earned certification
        EarnedCertification earnedCertification = new EarnedCertification();
        earnedCertification.setCertificationTemplate(certificationTemplate);
        earnedCertification.setLearnerId(request.getLearnerId());
        earnedCertification.setIssueDate(request.getIssueDate());
        earnedCertification.setExpiryDate(request.getExpiryDate());
        earnedCertification.setCertificateUrl(request.getCertificateUrl());
        earnedCertification.setAwardedBy(request.getAwardedBy());
        
        EarnedCertification saved = earnedCertificationRepository.save(earnedCertification);
        
        return earnedCertificationMapper.toDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public List<EarnedCertificationDTO> findByLearnerId(Long learnerId) {
        MDC.put("operation", "FIND_EARNED_CERTIFICATIONS_BY_LEARNER");
        try {
            log.info("Retrieving earned certifications for learner {}", learnerId);
            List<EarnedCertification> earnedCertifications = earnedCertificationRepository.findByLearnerId(learnerId);
            
            log.info("Successfully retrieved {} earned certifications for learner {}", 
                    earnedCertifications.size(), learnerId);
            return earnedCertifications.stream()
                    .map(earnedCertificationMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving earned certifications for learner {}", learnerId, e);
            throw e;
        } finally {
            MDC.remove("operation");
        }
    }
    
    @Transactional(readOnly = true)
    public Page<EarnedCertificationDTO> findByLearnerIdPaginated(Long learnerId, Pageable pageable) {
        MDC.put("operation", "FIND_EARNED_CERTIFICATIONS_BY_LEARNER_PAGINATED");
        try {
            log.info("Retrieving paginated earned certifications for learner {} - page: {}, size: {}", 
                    learnerId, pageable.getPageNumber(), pageable.getPageSize());
            Page<EarnedCertification> page = earnedCertificationRepository.findByLearnerId(learnerId, pageable);
            Page<EarnedCertificationDTO> dtoPage = page.map(earnedCertificationMapper::toDTO);
            log.info("Successfully retrieved page {} with {} earned certifications for learner {}", 
                    pageable.getPageNumber(), dtoPage.getNumberOfElements(), learnerId);
            return dtoPage;
        } catch (Exception e) {
            log.error("Error retrieving paginated earned certifications for learner {}", learnerId, e);
            throw e;
        } finally {
            MDC.remove("operation");
        }
    }
    
    @Transactional(readOnly = true)
    public EarnedCertificationDTO findByIdWithDetails(Long id) {
        MDC.put("operation", "FIND_EARNED_CERTIFICATION_BY_ID_WITH_DETAILS");
        try {
            log.info("Retrieving earned certification details for id: {}", id);
            EarnedCertification earnedCertification = earnedCertificationRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Earned certification not found with id: " + id));
            
            log.info("Successfully retrieved earned certification details for id: {}", id);
            return earnedCertificationMapper.toDTO(earnedCertification);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving earned certification details for id: {}", id, e);
            throw e;
        } finally {
            MDC.remove("operation");
        }
    }
    
    private void validateUrl(String url) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            log.warn("Invalid certificate URL format: {}", url);
            throw new ValidationException("Invalid certificate URL format: " + url);
        }
    }
}
