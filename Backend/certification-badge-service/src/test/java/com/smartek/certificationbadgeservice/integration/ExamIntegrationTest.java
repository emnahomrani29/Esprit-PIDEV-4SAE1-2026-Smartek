package com.smartek.certificationbadgeservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the exam auto-award system.
 * Tests the complete flow of automatically awarding certifications and badges based on exam scores.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ExamIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private CertificationTemplateRepository certificationTemplateRepository;
    
    @Autowired
    private BadgeTemplateRepository badgeTemplateRepository;
    
    @Autowired
    private EarnedCertificationRepository earnedCertificationRepository;
    
    @Autowired
    private EarnedBadgeRepository earnedBadgeRepository;
    
    private static final Long TEST_EXAM_ID = 102L;
    private static final Long TEST_LEARNER_ID = 2L;
    
    private CertificationTemplate certificationTemplate;
    private BadgeTemplate bronzeBadge;
    private BadgeTemplate silverBadge;
    private BadgeTemplate goldBadge;
    
    @BeforeEach
    public void setUp() {
        // Clean up any existing data
        earnedBadgeRepository.deleteAll();
        earnedCertificationRepository.deleteAll();
        badgeTemplateRepository.deleteAll();
        certificationTemplateRepository.deleteAll();
        
        // Create certification template linked to exam
        certificationTemplate = new CertificationTemplate();
        certificationTemplate.setTitle("Spring Boot Fundamentals Certification");
        certificationTemplate.setDescription("Awarded for passing the Spring Boot exam");
        certificationTemplate.setExamId(TEST_EXAM_ID);
        certificationTemplate = certificationTemplateRepository.save(certificationTemplate);
        
        // Create badge templates with different score thresholds
        bronzeBadge = new BadgeTemplate();
        bronzeBadge.setName("Spring Boot Bronze Badge");
        bronzeBadge.setDescription("Awarded for scoring 60% or higher");
        bronzeBadge.setExamId(TEST_EXAM_ID);
        bronzeBadge.setMinimumScore(60.0);
        bronzeBadge = badgeTemplateRepository.save(bronzeBadge);
        
        silverBadge = new BadgeTemplate();
        silverBadge.setName("Spring Boot Silver Badge");
        silverBadge.setDescription("Awarded for scoring 75% or higher");
        silverBadge.setExamId(TEST_EXAM_ID);
        silverBadge.setMinimumScore(75.0);
        silverBadge = badgeTemplateRepository.save(silverBadge);
        
        goldBadge = new BadgeTemplate();
        goldBadge.setName("Spring Boot Gold Badge");
        goldBadge.setDescription("Awarded for scoring 90% or higher");
        goldBadge.setExamId(TEST_EXAM_ID);
        goldBadge.setMinimumScore(90.0);
        goldBadge = badgeTemplateRepository.save(goldBadge);
    }
    
    /**
     * Test Scenario 1: Score 45% - Nothing awarded (failed exam)
     */
    @Test
    public void testScenario1_FailingScore_NothingAwarded() throws Exception {
        // Arrange
        ExamResultDTO examResult = new ExamResultDTO(TEST_LEARNER_ID, TEST_EXAM_ID, 45.0, 100.0);
        
        // Act
        MvcResult result = mockMvc.perform(post("/api/certifications-badges/exam-integration/process-exam-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(examResult)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.learnerId").value(TEST_LEARNER_ID))
                .andExpect(jsonPath("$.examId").value(TEST_EXAM_ID))
                .andExpect(jsonPath("$.percentage").value(45.0))
                .andExpect(jsonPath("$.passed").value(false))
                .andExpect(jsonPath("$.certificationAwarded").value(false))
                .andExpect(jsonPath("$.badgeAwarded").value(false))
                .andExpect(jsonPath("$.message").exists()) // Message format may vary by locale
                .andReturn();
        
        // Assert - Verify nothing was saved to database
        List<EarnedCertification> earnedCertifications = earnedCertificationRepository.findByLearnerId(TEST_LEARNER_ID);
        assertThat(earnedCertifications).isEmpty();
        
        List<EarnedBadge> earnedBadges = earnedBadgeRepository.findByLearnerId(TEST_LEARNER_ID);
        assertThat(earnedBadges).isEmpty();
        
        System.out.println("✅ Scenario 1 PASSED: Score 45% - Nothing awarded");
    }
    
    /**
     * Test Scenario 2: Score 75% - Certification + Silver Badge awarded
     */
    @Test
    public void testScenario2_SilverScore_CertificationAndSilverBadgeAwarded() throws Exception {
        // Arrange
        ExamResultDTO examResult = new ExamResultDTO(TEST_LEARNER_ID, TEST_EXAM_ID, 75.0, 100.0);
        
        // Act
        MvcResult result = mockMvc.perform(post("/api/certifications-badges/exam-integration/process-exam-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(examResult)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.learnerId").value(TEST_LEARNER_ID))
                .andExpect(jsonPath("$.examId").value(TEST_EXAM_ID))
                .andExpect(jsonPath("$.percentage").value(75.0))
                .andExpect(jsonPath("$.passed").value(true))
                .andExpect(jsonPath("$.certificationAwarded").value(true))
                .andExpect(jsonPath("$.badgeAwarded").value(true))
                .andReturn();
        
        // Parse response
        String responseBody = result.getResponse().getContentAsString();
        ExamProcessingResultDTO response = objectMapper.readValue(responseBody, ExamProcessingResultDTO.class);
        
        // Assert - Verify certification was saved
        List<EarnedCertification> earnedCertifications = earnedCertificationRepository.findByLearnerId(TEST_LEARNER_ID);
        assertThat(earnedCertifications).hasSize(1);
        assertThat(earnedCertifications.get(0).getCertificationTemplate().getId()).isEqualTo(certificationTemplate.getId());
        assertThat(earnedCertifications.get(0).getExpiryDate()).isNotNull();
        
        // Assert - Verify Silver badge was awarded (highest eligible)
        List<EarnedBadge> earnedBadges = earnedBadgeRepository.findByLearnerId(TEST_LEARNER_ID);
        assertThat(earnedBadges).hasSize(1);
        assertThat(earnedBadges.get(0).getBadgeTemplate().getId()).isEqualTo(silverBadge.getId());
        assertThat(earnedBadges.get(0).getBadgeTemplate().getName()).isEqualTo("Spring Boot Silver Badge");
        
        System.out.println("✅ Scenario 2 PASSED: Score 75% - Certification + Silver Badge awarded");
    }
    
    /**
     * Test Scenario 3: Score 92% - Certification + Gold Badge awarded
     */
    @Test
    public void testScenario3_GoldScore_CertificationAndGoldBadgeAwarded() throws Exception {
        // Arrange
        ExamResultDTO examResult = new ExamResultDTO(TEST_LEARNER_ID, TEST_EXAM_ID, 92.0, 100.0);
        
        // Act
        MvcResult result = mockMvc.perform(post("/api/certifications-badges/exam-integration/process-exam-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(examResult)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.learnerId").value(TEST_LEARNER_ID))
                .andExpect(jsonPath("$.examId").value(TEST_EXAM_ID))
                .andExpect(jsonPath("$.percentage").value(92.0))
                .andExpect(jsonPath("$.passed").value(true))
                .andExpect(jsonPath("$.certificationAwarded").value(true))
                .andExpect(jsonPath("$.badgeAwarded").value(true))
                .andReturn();
        
        // Parse response
        String responseBody = result.getResponse().getContentAsString();
        ExamProcessingResultDTO response = objectMapper.readValue(responseBody, ExamProcessingResultDTO.class);
        
        // Assert - Verify certification was saved
        List<EarnedCertification> earnedCertifications = earnedCertificationRepository.findByLearnerId(TEST_LEARNER_ID);
        assertThat(earnedCertifications).hasSize(1);
        assertThat(earnedCertifications.get(0).getCertificationTemplate().getId()).isEqualTo(certificationTemplate.getId());
        
        // Assert - Verify Gold badge was awarded (highest eligible)
        List<EarnedBadge> earnedBadges = earnedBadgeRepository.findByLearnerId(TEST_LEARNER_ID);
        assertThat(earnedBadges).hasSize(1);
        assertThat(earnedBadges.get(0).getBadgeTemplate().getId()).isEqualTo(goldBadge.getId());
        assertThat(earnedBadges.get(0).getBadgeTemplate().getName()).isEqualTo("Spring Boot Gold Badge");
        
        System.out.println("✅ Scenario 3 PASSED: Score 92% - Certification + Gold Badge awarded");
    }
    
    /**
     * Test duplicate prevention: Attempting to award same certification twice should not create duplicate
     */
    @Test
    public void testDuplicatePrevention_SameCertificationNotAwardedTwice() throws Exception {
        // Arrange - First award
        ExamResultDTO examResult = new ExamResultDTO(TEST_LEARNER_ID, TEST_EXAM_ID, 75.0, 100.0);
        
        // Act - First submission
        mockMvc.perform(post("/api/certifications-badges/exam-integration/process-exam-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(examResult)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.certificationAwarded").value(true))
                .andExpect(jsonPath("$.badgeAwarded").value(true));
        
        // Act - Second submission (duplicate)
        mockMvc.perform(post("/api/certifications-badges/exam-integration/process-exam-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(examResult)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.certificationAwarded").value(false))
                .andExpect(jsonPath("$.badgeAwarded").value(false));
        
        // Assert - Verify only one certification and one badge exist
        List<EarnedCertification> earnedCertifications = earnedCertificationRepository.findByLearnerId(TEST_LEARNER_ID);
        assertThat(earnedCertifications).hasSize(1);
        
        List<EarnedBadge> earnedBadges = earnedBadgeRepository.findByLearnerId(TEST_LEARNER_ID);
        assertThat(earnedBadges).hasSize(1);
        
        System.out.println("✅ Duplicate Prevention PASSED: Same certification not awarded twice");
    }
    
    /**
     * Test edge case: Exactly 60% should pass and award certification + bronze badge
     */
    @Test
    public void testEdgeCase_Exactly60Percent_PassesAndAwardsBronze() throws Exception {
        // Arrange
        ExamResultDTO examResult = new ExamResultDTO(TEST_LEARNER_ID, TEST_EXAM_ID, 60.0, 100.0);
        
        // Act
        mockMvc.perform(post("/api/certifications-badges/exam-integration/process-exam-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(examResult)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentage").value(60.0))
                .andExpect(jsonPath("$.passed").value(true))
                .andExpect(jsonPath("$.certificationAwarded").value(true))
                .andExpect(jsonPath("$.badgeAwarded").value(true));
        
        // Assert - Verify Bronze badge was awarded
        List<EarnedBadge> earnedBadges = earnedBadgeRepository.findByLearnerId(TEST_LEARNER_ID);
        assertThat(earnedBadges).hasSize(1);
        assertThat(earnedBadges.get(0).getBadgeTemplate().getName()).isEqualTo("Spring Boot Bronze Badge");
        
        System.out.println("✅ Edge Case PASSED: Exactly 60% passes and awards Bronze badge");
    }
    
    /**
     * Test no templates configured: Should handle gracefully
     */
    @Test
    public void testNoTemplatesConfigured_HandlesGracefully() throws Exception {
        // Arrange - Use different exam ID with no templates
        ExamResultDTO examResult = new ExamResultDTO(TEST_LEARNER_ID, 999L, 85.0, 100.0);
        
        // Act
        mockMvc.perform(post("/api/certifications-badges/exam-integration/process-exam-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(examResult)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passed").value(true))
                .andExpect(jsonPath("$.certificationAwarded").value(false))
                .andExpect(jsonPath("$.badgeAwarded").value(false));
        
        System.out.println("✅ No Templates PASSED: Handles missing templates gracefully");
    }
}
