package com.smartek.examservice.service;

import com.smartek.examservice.dto.TrainerExamAnalyticsResponse;
import com.smartek.examservice.entity.Exam;
import com.smartek.examservice.entity.ExamResult;
import com.smartek.examservice.repository.ExamRepository;
import com.smartek.examservice.repository.ExamResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;
    private final RestTemplate restTemplate;

    public List<TrainerExamAnalyticsResponse> getTrainerExamAnalytics(Long trainerId) {
        log.info("Getting exam analytics for trainer: {}", trainerId);
        
        // Récupérer tous les examens créés par ce trainer
        List<Exam> trainerExams = examRepository.findByCreatedBy(trainerId);
        log.info("Found {} exams for trainer {}", trainerExams.size(), trainerId);
        
        List<TrainerExamAnalyticsResponse> analytics = new ArrayList<>();
        
        for (Exam exam : trainerExams) {
            // Récupérer tous les résultats pour cet examen
            List<ExamResult> results = examResultRepository.findByExamId(exam.getId());
            log.info("Found {} results for exam {}", results.size(), exam.getId());
            
            for (ExamResult result : results) {
                // Récupérer les informations de l'étudiant depuis auth-service
                String learnerName = "Student " + result.getUserId();
                String learnerEmail = "";
                
                try {
                    String authServiceUrl = "http://auth-service/api/auth/user/" + result.getUserId();
                    Map<String, Object> userInfo = restTemplate.getForObject(authServiceUrl, Map.class);
                    if (userInfo != null) {
                        learnerName = userInfo.get("firstName") + " " + userInfo.getOrDefault("lastName", "");
                        learnerEmail = (String) userInfo.get("email");
                    }
                } catch (Exception e) {
                    log.warn("Could not fetch user info for userId: {}", result.getUserId());
                }
                
                TrainerExamAnalyticsResponse response = new TrainerExamAnalyticsResponse();
                response.setExamId(exam.getId());
                response.setExamTitle(exam.getTitle());
                response.setLearnerId(result.getUserId());
                response.setLearnerName(learnerName);
                response.setLearnerEmail(learnerEmail);
                response.setScore(result.getObtainedMarks());
                response.setMaxScore(result.getTotalMarks());
                response.setPercentage(result.getPercentage());
                response.setStatus(result.getPassed() ? "passed" : "failed");
                response.setCompletedAt(result.getSubmittedAt());
                
                analytics.add(response);
            }
        }
        
        log.info("Returning {} analytics records", analytics.size());
        return analytics;
    }
}
