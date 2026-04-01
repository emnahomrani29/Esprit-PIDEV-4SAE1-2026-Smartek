package com.smartek.certificationbadgeservice.service;

import com.smartek.certificationbadgeservice.dto.*;
import com.smartek.certificationbadgeservice.entity.BadgeTemplate;
import com.smartek.certificationbadgeservice.entity.EarnedBadge;
import com.smartek.certificationbadgeservice.exception.DuplicateAwardException;
import com.smartek.certificationbadgeservice.exception.ResourceNotFoundException;
import com.smartek.certificationbadgeservice.mapper.EarnedBadgeMapper;
import com.smartek.certificationbadgeservice.repository.BadgeTemplateRepository;
import com.smartek.certificationbadgeservice.repository.EarnedBadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EarnedBadgeService {
    
    private final EarnedBadgeRepository earnedBadgeRepository;
    private final BadgeTemplateRepository badgeTemplateRepository;
    private final EarnedBadgeMapper earnedBadgeMapper;
    
    @Transactional(propagation = Propagation.REQUIRED)
    public EarnedBadgeDTO awardBadge(AwardBadgeRequestDTO request) {
        MDC.put("operation", "AWARD_BADGE");
        MDC.put("userId", String.valueOf(request.getAwardedBy()));
        try {
            log.info("Awarding badge template {} to learner {}", request.getBadgeTemplateId(), request.getLearnerId());
            
            // Validate badge template exists
            BadgeTemplate badgeTemplate = badgeTemplateRepository.findById(request.getBadgeTemplateId())
                    .orElseThrow(() -> {
                        log.warn("Badge template not found with id: {}", request.getBadgeTemplateId());
                        return new ResourceNotFoundException("Badge template not found with id: " + request.getBadgeTemplateId());
                    });
            
            // Check for duplicate award
            if (earnedBadgeRepository.existsByBadgeTemplateIdAndLearnerId(
                    request.getBadgeTemplateId(), request.getLearnerId())) {
                log.warn("Badge {} already awarded to learner {}", request.getBadgeTemplateId(), request.getLearnerId());
                throw new DuplicateAwardException("Badge already awarded to this learner");
            }
            
            // Create earned badge
            EarnedBadge earnedBadge = new EarnedBadge();
            earnedBadge.setBadgeTemplate(badgeTemplate);
            earnedBadge.setLearnerId(request.getLearnerId());
            earnedBadge.setAwardDate(LocalDate.now());
            earnedBadge.setAwardedBy(request.getAwardedBy());
            
            EarnedBadge saved = earnedBadgeRepository.save(earnedBadge);
            
            log.info("Successfully awarded badge {} to learner {} by user {}", 
                    request.getBadgeTemplateId(), request.getLearnerId(), request.getAwardedBy());
            return earnedBadgeMapper.toDTO(saved);
        } catch (DuplicateAwardException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error awarding badge {} to learner {}", request.getBadgeTemplateId(), request.getLearnerId(), e);
            throw e;
        } finally {
            MDC.remove("operation");
            MDC.remove("userId");
        }
    }
    
    @Transactional
    public BulkAwardResponseDTO bulkAwardBadges(BulkAwardBadgeRequestDTO request) {
        MDC.put("operation", "BULK_AWARD_BADGES");
        MDC.put("userId", String.valueOf(request.getAwardedBy()));
        try {
            log.info("Bulk awarding badge template {} to {} learners", 
                    request.getBadgeTemplateId(), request.getLearnerIds().size());
            
            List<AwardResultDTO> results = new ArrayList<>();
            
            // Validate badge template exists once
            if (!badgeTemplateRepository.existsById(request.getBadgeTemplateId())) {
                log.warn("Badge template not found with id: {}", request.getBadgeTemplateId());
                // If template doesn't exist, all awards fail
                for (Long learnerId : request.getLearnerIds()) {
                    results.add(AwardResultDTO.failure(learnerId, 
                        "Badge template not found with id: " + request.getBadgeTemplateId()));
                }
                return new BulkAwardResponseDTO(results);
            }
            
            // Process each learner individually with independent transactions (savepoint-based)
            for (Long learnerId : request.getLearnerIds()) {
                try {
                    AwardBadgeRequestDTO individualRequest = new AwardBadgeRequestDTO();
                    individualRequest.setBadgeTemplateId(request.getBadgeTemplateId());
                    individualRequest.setLearnerId(learnerId);
                    individualRequest.setAwardedBy(request.getAwardedBy());
                    
                    // Each award is in its own nested transaction (savepoint)
                    awardBadgeWithSavepoint(individualRequest);
                    results.add(AwardResultDTO.success(learnerId));
                    
                } catch (DuplicateAwardException e) {
                    log.warn("Duplicate award for learner {}: {}", learnerId, e.getMessage());
                    results.add(AwardResultDTO.failure(learnerId, e.getMessage()));
                } catch (DataIntegrityViolationException e) {
                    log.warn("Database constraint violation for learner {}", learnerId);
                    results.add(AwardResultDTO.failure(learnerId, "Database constraint violation"));
                } catch (Exception e) {
                    log.error("Error awarding badge to learner {}", learnerId, e);
                    results.add(AwardResultDTO.failure(learnerId, "Failed to award badge: " + e.getMessage()));
                }
            }
            
            BulkAwardResponseDTO response = new BulkAwardResponseDTO(results);
            log.info("Bulk award completed: {} successful, {} failed", 
                    response.getSuccessCount(), response.getFailureCount());
            return response;
        } catch (Exception e) {
            log.error("Error in bulk award badges operation", e);
            throw e;
        } finally {
            MDC.remove("operation");
            MDC.remove("userId");
        }
    }
    
    /**
     * Award badge with REQUIRES_NEW propagation to create a savepoint.
     * If this fails, only this individual award is rolled back, not the entire bulk operation.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected EarnedBadgeDTO awardBadgeWithSavepoint(AwardBadgeRequestDTO request) {
        // Validate badge template exists
        BadgeTemplate badgeTemplate = badgeTemplateRepository.findById(request.getBadgeTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Badge template not found with id: " + request.getBadgeTemplateId()));
        
        // Check for duplicate award
        if (earnedBadgeRepository.existsByBadgeTemplateIdAndLearnerId(
                request.getBadgeTemplateId(), request.getLearnerId())) {
            throw new DuplicateAwardException("Badge already awarded to this learner");
        }
        
        // Create earned badge
        EarnedBadge earnedBadge = new EarnedBadge();
        earnedBadge.setBadgeTemplate(badgeTemplate);
        earnedBadge.setLearnerId(request.getLearnerId());
        earnedBadge.setAwardDate(LocalDate.now());
        earnedBadge.setAwardedBy(request.getAwardedBy());
        
        EarnedBadge saved = earnedBadgeRepository.save(earnedBadge);
        
        return earnedBadgeMapper.toDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public List<EarnedBadgeDTO> findByLearnerId(Long learnerId) {
        MDC.put("operation", "FIND_EARNED_BADGES_BY_LEARNER");
        try {
            log.info("Retrieving earned badges for learner {}", learnerId);
            List<EarnedBadge> earnedBadges = earnedBadgeRepository.findByLearnerId(learnerId);
            
            log.info("Successfully retrieved {} earned badges for learner {}", earnedBadges.size(), learnerId);
            return earnedBadges.stream()
                    .map(earnedBadgeMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving earned badges for learner {}", learnerId, e);
            throw e;
        } finally {
            MDC.remove("operation");
        }
    }
    
    @Transactional(readOnly = true)
    public Page<EarnedBadgeDTO> findByLearnerIdPaginated(Long learnerId, Pageable pageable) {
        MDC.put("operation", "FIND_EARNED_BADGES_BY_LEARNER_PAGINATED");
        try {
            log.info("Retrieving paginated earned badges for learner {} - page: {}, size: {}", 
                    learnerId, pageable.getPageNumber(), pageable.getPageSize());
            Page<EarnedBadge> page = earnedBadgeRepository.findByLearnerId(learnerId, pageable);
            Page<EarnedBadgeDTO> dtoPage = page.map(earnedBadgeMapper::toDTO);
            log.info("Successfully retrieved page {} with {} earned badges for learner {}", 
                    pageable.getPageNumber(), dtoPage.getNumberOfElements(), learnerId);
            return dtoPage;
        } catch (Exception e) {
            log.error("Error retrieving paginated earned badges for learner {}", learnerId, e);
            throw e;
        } finally {
            MDC.remove("operation");
        }
    }
}
