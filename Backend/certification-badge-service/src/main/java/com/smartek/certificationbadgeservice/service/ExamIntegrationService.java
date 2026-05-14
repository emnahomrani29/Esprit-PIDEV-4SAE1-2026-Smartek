package com.smartek.certificationbadgeservice.service;

import com.smartek.certificationbadgeservice.dto.ExamProcessingResultDTO;
import com.smartek.certificationbadgeservice.dto.ExamResultDTO;
import com.smartek.certificationbadgeservice.entity.BadgeTemplate;
import com.smartek.certificationbadgeservice.entity.CertificationTemplate;
import com.smartek.certificationbadgeservice.entity.EarnedBadge;
import com.smartek.certificationbadgeservice.entity.EarnedCertification;
import com.smartek.certificationbadgeservice.repository.BadgeTemplateRepository;
import com.smartek.certificationbadgeservice.repository.CertificationTemplateRepository;
import com.smartek.certificationbadgeservice.repository.EarnedBadgeRepository;
import com.smartek.certificationbadgeservice.repository.EarnedCertificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for integrating with the exam service.
 * Automatically awards certifications and badges based on exam results.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamIntegrationService {
    
    private final CertificationTemplateRepository certificationTemplateRepository;
    private final BadgeTemplateRepository badgeTemplateRepository;
    private final EarnedCertificationRepository earnedCertificationRepository;
    private final EarnedBadgeRepository earnedBadgeRepository;
    
    private static final double PASSING_SCORE = 60.0;
    private static final double BADGE_SCORE_THRESHOLD = 60.0; // Badge awarded at same threshold as certification
    
    /**
     * Process exam results and automatically award certifications and badges.
     * 
     * @param examResult the exam result data
     * @return processing result with details of what was awarded
     */
    @Transactional
    public ExamProcessingResultDTO processExamResult(ExamResultDTO examResult) {
        log.info("Processing exam result for learner {} on exam {}", 
                examResult.getLearnerId(), examResult.getExamId());
        
        double percentage = examResult.getPercentage();
        boolean passed = percentage >= PASSING_SCORE;
        
        ExamProcessingResultDTO.ExamProcessingResultDTOBuilder resultBuilder = ExamProcessingResultDTO.builder()
                .learnerId(examResult.getLearnerId())
                .examId(examResult.getExamId())
                .percentage(percentage)
                .passed(passed);
        
        // Award certification if passed
        Long certificationId = null;
        if (passed) {
            certificationId = awardCertification(examResult.getLearnerId(), examResult.getExamId());
            resultBuilder.certificationAwarded(certificationId != null)
                    .certificationId(certificationId);
        }
        
        // Award badge if passing score (same as certification)
        Long badgeId = null;
        if (percentage >= BADGE_SCORE_THRESHOLD) {
            badgeId = awardBadge(examResult.getLearnerId(), examResult.getExamId(), percentage);
            resultBuilder.badgeAwarded(badgeId != null)
                    .badgeId(badgeId);
        }
        
        // Build message
        String message = buildResultMessage(passed, certificationId != null, badgeId != null, percentage);
        resultBuilder.message(message);
        
        log.info("Exam processing complete: {}", message);
        return resultBuilder.build();
    }
    
    /**
     * Award certification to learner for completing an exam.
     */
    private Long awardCertification(Long learnerId, Long examId) {
        try {
            // Find certification template for this exam
            Optional<CertificationTemplate> templateOpt = certificationTemplateRepository
                    .findByExamId(examId);
            
            if (templateOpt.isEmpty()) {
                log.warn("No certification template found for exam {}", examId);
                return null;
            }
            
            CertificationTemplate template = templateOpt.get();
            
            // Check if learner already has this certification
            boolean alreadyHas = earnedCertificationRepository
                    .existsByLearnerIdAndCertificationTemplateId(learnerId, template.getId());
            
            if (alreadyHas) {
                log.info("Learner {} already has certification for exam {}", learnerId, examId);
                return null;
            }
            
            // Award certification
            EarnedCertification earned = new EarnedCertification();
            earned.setLearnerId(learnerId);
            earned.setCertificationTemplate(template);
            earned.setIssueDate(LocalDate.now());
            earned.setExpiryDate(LocalDate.now().plusYears(2)); // Valid for 2 years
            earned.setAwardedBy(0L); // System-awarded (0 = automatic)
            
            EarnedCertification saved = earnedCertificationRepository.save(earned);
            log.info("Awarded certification {} to learner {}", template.getTitle(), learnerId);
            
            return saved.getId();
        } catch (Exception e) {
            log.error("Error awarding certification to learner {} for exam {}", learnerId, examId, e);
            return null;
        }
    }
    
    /**
     * Award badge to learner for high exam score.
     */
    private Long awardBadge(Long learnerId, Long examId, double percentage) {
        try {
            // Find badge templates for this exam with score threshold <= achieved percentage
            List<BadgeTemplate> eligibleBadges = badgeTemplateRepository
                    .findByExamIdAndMinimumScoreLessThanEqual(examId, percentage);
            
            if (eligibleBadges.isEmpty()) {
                log.info("No eligible badges found for exam {} with score {}", examId, percentage);
                return null;
            }
            
            // Award the highest level badge (highest minimum score)
            BadgeTemplate template = eligibleBadges.stream()
                    .max((b1, b2) -> Double.compare(b1.getMinimumScore(), b2.getMinimumScore()))
                    .orElse(eligibleBadges.get(0));
            
            // Check if learner already has this badge
            boolean alreadyHas = earnedBadgeRepository
                    .existsByLearnerIdAndBadgeTemplateId(learnerId, template.getId());
            
            if (alreadyHas) {
                log.info("Learner {} already has badge {}", learnerId, template.getName());
                return null;
            }
            
            // Award badge
            EarnedBadge earned = new EarnedBadge();
            earned.setLearnerId(learnerId);
            earned.setBadgeTemplate(template);
            earned.setAwardDate(LocalDate.now());
            earned.setAwardedBy(0L); // System-awarded (0 = automatic)
            
            EarnedBadge saved = earnedBadgeRepository.save(earned);
            log.info("Awarded badge {} to learner {} for score {}%", template.getName(), learnerId, percentage);
            
            return saved.getId();
        } catch (Exception e) {
            log.error("Error awarding badge to learner {} for exam {}", learnerId, examId, e);
            return null;
        }
    }
    
    private String buildResultMessage(boolean passed, boolean certAwarded, boolean badgeAwarded, double percentage) {
        StringBuilder msg = new StringBuilder();
        msg.append(String.format("Exam score: %.2f%%. ", percentage));
        
        if (passed) {
            msg.append("Passed! ");
            if (certAwarded) {
                msg.append("Certification awarded. ");
            }
            if (badgeAwarded) {
                msg.append("Badge awarded for high achievement!");
            }
        } else {
            msg.append("Did not pass (minimum 60% required).");
        }
        
        return msg.toString();
    }
}
