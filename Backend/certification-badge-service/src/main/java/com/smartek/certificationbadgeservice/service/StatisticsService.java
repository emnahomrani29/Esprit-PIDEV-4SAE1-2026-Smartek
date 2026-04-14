package com.smartek.certificationbadgeservice.service;

import com.smartek.certificationbadgeservice.dto.BadgeStatisticsDTO;
import com.smartek.certificationbadgeservice.dto.CertificationStatisticsDTO;
import com.smartek.certificationbadgeservice.dto.LearnerStatisticsDTO;
import com.smartek.certificationbadgeservice.entity.BadgeTemplate;
import com.smartek.certificationbadgeservice.entity.CertificationTemplate;
import com.smartek.certificationbadgeservice.entity.EarnedCertification;
import com.smartek.certificationbadgeservice.repository.BadgeTemplateRepository;
import com.smartek.certificationbadgeservice.repository.CertificationTemplateRepository;
import com.smartek.certificationbadgeservice.repository.EarnedBadgeRepository;
import com.smartek.certificationbadgeservice.repository.EarnedCertificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {
    
    private final BadgeTemplateRepository badgeTemplateRepository;
    private final CertificationTemplateRepository certificationTemplateRepository;
    private final EarnedBadgeRepository earnedBadgeRepository;
    private final EarnedCertificationRepository earnedCertificationRepository;
    
    @Transactional(readOnly = true)
    public List<BadgeStatisticsDTO> getBadgeStatistics() {
        MDC.put("operation", "GET_BADGE_STATISTICS");
        try {
            log.info("Retrieving badge statistics");
            List<BadgeTemplate> allBadgeTemplates = badgeTemplateRepository.findAll();
            
            List<BadgeStatisticsDTO> statistics = allBadgeTemplates.stream()
                    .map(template -> {
                        long count = earnedBadgeRepository.countByBadgeTemplateId(template.getId());
                        return new BadgeStatisticsDTO(
                                template.getId(),
                                template.getName(),
                                count
                        );
                    })
                    .collect(Collectors.toList());
            
            log.info("Successfully retrieved badge statistics for {} templates", statistics.size());
            return statistics;
        } catch (Exception e) {
            log.error("Error retrieving badge statistics", e);
            throw e;
        } finally {
            MDC.remove("operation");
        }
    }
    
    @Transactional(readOnly = true)
    public List<CertificationStatisticsDTO> getCertificationStatistics() {
        MDC.put("operation", "GET_CERTIFICATION_STATISTICS");
        try {
            log.info("Retrieving certification statistics");
            List<CertificationTemplate> allCertificationTemplates = certificationTemplateRepository.findAll();
            
            List<CertificationStatisticsDTO> statistics = allCertificationTemplates.stream()
                    .map(template -> {
                        long count = earnedCertificationRepository.countByCertificationTemplateId(template.getId());
                        return new CertificationStatisticsDTO(
                                template.getId(),
                                template.getTitle(),
                                count
                        );
                    })
                    .collect(Collectors.toList());
            
            log.info("Successfully retrieved certification statistics for {} templates", statistics.size());
            return statistics;
        } catch (Exception e) {
            log.error("Error retrieving certification statistics", e);
            throw e;
        } finally {
            MDC.remove("operation");
        }
    }
    
    @Transactional(readOnly = true)
    public LearnerStatisticsDTO getLearnerStatistics(Long learnerId) {
        MDC.put("operation", "GET_LEARNER_STATISTICS");
        try {
            log.info("Retrieving statistics for learner {}", learnerId);
            
            // Count total badges
            long totalBadges = earnedBadgeRepository.findByLearnerId(learnerId).size();
            
            // Get all certifications for the learner
            List<EarnedCertification> certifications = earnedCertificationRepository.findByLearnerId(learnerId);
            
            // Count active certifications (not expired)
            long activeCertifications = certifications.stream()
                    .filter(cert -> !cert.isExpired())
                    .count();
            
            // Count expired certifications
            long expiredCertifications = certifications.stream()
                    .filter(EarnedCertification::isExpired)
                    .count();
            
            log.info("Successfully retrieved statistics for learner {}: {} badges, {} active certifications, {} expired certifications", 
                    learnerId, totalBadges, activeCertifications, expiredCertifications);
            
            return new LearnerStatisticsDTO(
                    learnerId,
                    totalBadges,
                    activeCertifications,
                    expiredCertifications
            );
        } catch (Exception e) {
            log.error("Error retrieving statistics for learner {}", learnerId, e);
            throw e;
        } finally {
            MDC.remove("operation");
        }
    }
}
