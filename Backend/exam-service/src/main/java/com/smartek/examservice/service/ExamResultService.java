package com.smartek.examservice.service;

import com.smartek.examservice.client.UserClient;
import com.smartek.examservice.client.UserResponse;
import com.smartek.examservice.dto.*;
import com.smartek.examservice.entity.*;
import com.smartek.examservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamResultService {
    private final ExamResultRepository examResultRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamEnrollmentRepository examEnrollmentRepository;
    private final ExamDraftService examDraftService;
    private final UserClient userClient;

    @Transactional
    public ExamResultResponse submitExam(ExamSubmissionDTO request) {
        System.out.println("=== SUBMIT EXAM DEBUG ===");
        System.out.println("Exam ID: " + request.getExamId());
        System.out.println("User ID: " + request.getUserId());
        System.out.println("Answers count: " + (request.getAnswers() != null ? request.getAnswers().size() : 0));
        
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        System.out.println("Exam found: " + exam.getTitle());
        System.out.println("Total marks: " + exam.getTotalMarks());

        // Calculer le temps pris (durée de l'examen - temps restant)
        Integer timeTaken = request.getTimeTaken() != null ? request.getTimeTaken() : exam.getDuration();

        ExamResult result = new ExamResult();
        result.setExam(exam);
        result.setUserId(request.getUserId());
        result.setTotalMarks(exam.getTotalMarks());
        result.setTimeTaken(timeTaken);
        result.setSubmittedAt(LocalDateTime.now());
        result.setIsCorrected(true); // Auto-corrigé pour les QCM

        int obtainedMarks = 0;
        List<UserAnswer> userAnswers = request.getAnswers().stream()
                .map(ansReq -> {
                    Question question = questionRepository.findById(ansReq.getQuestionId())
                            .orElseThrow(() -> new RuntimeException("Question not found"));

                    System.out.println("Processing question: " + question.getId() + " - " + question.getQuestionText());

                    UserAnswer userAnswer = new UserAnswer();
                    userAnswer.setExamResult(result);
                    userAnswer.setQuestionId(ansReq.getQuestionId());
                    userAnswer.setAnswer(ansReq.getSelectedAnswer());

                    // Vérifier la réponse
                    boolean isCorrect = checkAnswer(question, ansReq);
                    userAnswer.setIsCorrect(isCorrect);
                    userAnswer.setMarksObtained(isCorrect ? question.getMarks() : 0);

                    System.out.println("Answer correct: " + isCorrect + ", Marks: " + userAnswer.getMarksObtained());

                    return userAnswer;
                })
                .collect(Collectors.toList());

        obtainedMarks = userAnswers.stream()
                .mapToInt(UserAnswer::getMarksObtained)
                .sum();

        System.out.println("Total obtained marks: " + obtainedMarks);

        result.setObtainedMarks(obtainedMarks);
        result.setPercentage((double) obtainedMarks / exam.getTotalMarks() * 100);
        result.setPassed(result.getPercentage() >= exam.getPassingScore());
        result.setUserAnswers(userAnswers);

        ExamResult savedResult = examResultRepository.save(result);
        
        System.out.println("Result saved with ID: " + savedResult.getId());
        
        // Supprimer le brouillon après soumission
        examDraftService.deleteDraft(exam.getId(), request.getUserId());
        
        // Marquer l'enrollment comme complété
        examEnrollmentRepository.findByUserIdAndExamId(request.getUserId(), exam.getId())
                .ifPresent(enrollment -> {
                    System.out.println("Marking enrollment as completed");
                    enrollment.setIsCompleted(true);
                    enrollment.setCompletedAt(LocalDateTime.now());
                    examEnrollmentRepository.save(enrollment);
                });

        System.out.println("=== END SUBMIT EXAM DEBUG ===");
        
        return mapToResultResponse(savedResult);
    }

    @Transactional
    public ExamResultResponse submitExamOld(ExamSubmissionRequest request) {
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        ExamResult result = new ExamResult();
        result.setExam(exam);
        result.setUserId(request.getUserId());
        result.setTotalMarks(exam.getTotalMarks());
        result.setTimeTaken(request.getTimeTaken());

        int obtainedMarks = 0;
        List<UserAnswer> userAnswers = request.getAnswers().stream()
                .map(ansReq -> {
                    Question question = questionRepository.findById(ansReq.getQuestionId())
                            .orElseThrow(() -> new RuntimeException("Question not found"));

                    UserAnswer userAnswer = new UserAnswer();
                    userAnswer.setExamResult(result);
                    userAnswer.setQuestionId(ansReq.getQuestionId());
                    userAnswer.setAnswer(ansReq.getAnswer());

                    boolean isCorrect = checkAnswerOld(question, ansReq.getAnswer());
                    userAnswer.setIsCorrect(isCorrect);
                    userAnswer.setMarksObtained(isCorrect ? question.getMarks() : 0);

                    return userAnswer;
                })
                .collect(Collectors.toList());

        obtainedMarks = userAnswers.stream()
                .mapToInt(UserAnswer::getMarksObtained)
                .sum();

        result.setObtainedMarks(obtainedMarks);
        result.setPercentage((double) obtainedMarks / exam.getTotalMarks() * 100);
        result.setPassed(obtainedMarks >= exam.getPassingScore());
        result.setUserAnswers(userAnswers);

        ExamResult savedResult = examResultRepository.save(result);
        return mapToResultResponse(savedResult);
    }

    public List<ExamResultResponse> getResultsByUser(Long userId) {
        return examResultRepository.findByUserId(userId).stream()
                .map(this::mapToResultResponse)
                .collect(Collectors.toList());
    }

    public List<ExamResultResponse> getResultsByExam(Long examId) {
        return examResultRepository.findByExamId(examId).stream()
                .map(this::mapToResultResponse)
                .collect(Collectors.toList());
    }

    public ExamResultResponse getResultById(Long resultId) {
        ExamResult result = examResultRepository.findById(resultId)
                .orElseThrow(() -> new RuntimeException("Result not found"));
        return mapToResultResponse(result);
    }

    public List<UserAnswerResponse> getUserAnswers(Long resultId) {
        ExamResult result = examResultRepository.findById(resultId)
                .orElseThrow(() -> new RuntimeException("Result not found"));
        
        return result.getUserAnswers().stream()
                .map(this::mapToUserAnswerResponse)
                .collect(Collectors.toList());
    }

    private UserAnswerResponse mapToUserAnswerResponse(UserAnswer userAnswer) {
        UserAnswerResponse response = new UserAnswerResponse();
        response.setId(userAnswer.getId());
        response.setQuestionId(userAnswer.getQuestionId());
        response.setSelectedAnswer(userAnswer.getAnswer());
        response.setIsCorrect(userAnswer.getIsCorrect());
        response.setMarksObtained(userAnswer.getMarksObtained());
        return response;
    }

    private boolean checkAnswer(Question question, ExamSubmissionDTO.AnswerDTO answerDTO) {
        System.out.println("=== CHECK ANSWER DEBUG ===");
        System.out.println("Question ID: " + question.getId());
        System.out.println("Question Type: " + question.getQuestionType());
        System.out.println("Selected Answer: " + answerDTO.getSelectedAnswer());
        System.out.println("Selected Options: " + answerDTO.getSelectedOptions());
        System.out.println("Correct Answer: " + question.getCorrectAnswer());
        
        if ("MULTIPLE_CHOICE".equals(question.getQuestionType()) || "TRUE_FALSE".equals(question.getQuestionType())) {
            List<QuestionOption> options = question.getOptions();
            if (options == null || options.isEmpty()) {
                System.out.println("No options found for question");
                return false;
            }
            
            // Si selectedOptions est fourni (liste d'indices)
            if (answerDTO.getSelectedOptions() != null && !answerDTO.getSelectedOptions().isEmpty()) {
                System.out.println("Using selectedOptions (indices)");
                
                // Compter les bonnes réponses attendues
                long expectedCorrectCount = options.stream().filter(QuestionOption::getIsCorrect).count();
                
                // Vérifier que le nombre de réponses sélectionnées correspond
                if (answerDTO.getSelectedOptions().size() != expectedCorrectCount) {
                    System.out.println("Wrong number of selections: " + answerDTO.getSelectedOptions().size() + " vs " + expectedCorrectCount);
                    return false;
                }
                
                // Vérifier que toutes les options sélectionnées sont correctes
                for (Integer optionIndex : answerDTO.getSelectedOptions()) {
                    if (optionIndex < 0 || optionIndex >= options.size()) {
                        System.out.println("Invalid option index: " + optionIndex);
                        return false;
                    }
                    if (!options.get(optionIndex).getIsCorrect()) {
                        System.out.println("Option " + optionIndex + " is not correct");
                        return false;
                    }
                }
                
                System.out.println("Answer is CORRECT (using indices)");
                return true;
            }
            // Sinon, utiliser selectedAnswer (texte de la réponse)
            else if (answerDTO.getSelectedAnswer() != null && !answerDTO.getSelectedAnswer().trim().isEmpty()) {
                System.out.println("Using selectedAnswer (text)");
                
                String selectedAnswer = answerDTO.getSelectedAnswer().trim();
                
                // Pour TRUE_FALSE, normaliser les valeurs true/false en Vrai/Faux
                if ("TRUE_FALSE".equals(question.getQuestionType())) {
                    if ("true".equalsIgnoreCase(selectedAnswer)) {
                        selectedAnswer = "Vrai";
                    } else if ("false".equalsIgnoreCase(selectedAnswer)) {
                        selectedAnswer = "Faux";
                    }
                }
                
                // Chercher l'option qui correspond au texte sélectionné
                for (int i = 0; i < options.size(); i++) {
                    QuestionOption option = options.get(i);
                    System.out.println("Option " + i + ": " + option.getOptionText() + " (isCorrect: " + option.getIsCorrect() + ")");
                    
                    if (option.getOptionText().trim().equalsIgnoreCase(selectedAnswer)) {
                        System.out.println("Found matching option at index " + i);
                        boolean isCorrect = option.getIsCorrect();
                        System.out.println("Answer is " + (isCorrect ? "CORRECT" : "INCORRECT"));
                        return isCorrect;
                    }
                }
                
                System.out.println("No matching option found for: " + answerDTO.getSelectedAnswer());
                return false;
            }
            
            System.out.println("No answer provided");
            return false;
            
        } else if ("SHORT_ANSWER".equals(question.getQuestionType())) {
            // Pour les réponses courtes, comparer avec la réponse correcte
            boolean isCorrect = question.getCorrectAnswer() != null && 
                   question.getCorrectAnswer().trim().equalsIgnoreCase(answerDTO.getSelectedAnswer().trim());
            System.out.println("Short answer is " + (isCorrect ? "CORRECT" : "INCORRECT"));
            return isCorrect;
        }
        
        System.out.println("Unknown question type");
        return false;
    }

    private boolean checkAnswerOld(Question question, String userAnswer) {
        if ("MULTIPLE_CHOICE".equals(question.getQuestionType())) {
            return question.getOptions().stream()
                    .anyMatch(opt -> opt.getIsCorrect() && opt.getOptionText().equals(userAnswer));
        }
        return question.getCorrectAnswer() != null && 
               question.getCorrectAnswer().equalsIgnoreCase(userAnswer);
    }

    private ExamResultResponse mapToResultResponse(ExamResult result) {
        ExamResultResponse response = new ExamResultResponse();
        response.setId(result.getId());
        response.setExamId(result.getExam().getId());
        response.setExamTitle(result.getExam().getTitle());
        response.setUserId(result.getUserId());
        response.setObtainedMarks(result.getObtainedMarks());
        response.setTotalMarks(result.getTotalMarks());
        response.setPercentage(result.getPercentage());
        response.setPassed(result.getPassed());
        response.setSubmittedAt(result.getSubmittedAt());
        response.setTimeTaken(result.getTimeTaken());
        response.setIsCorrected(result.getIsCorrected());
        return response;
    }
    
    public ExamStatsResponse getExamStatsByUserId(Long userId) {
        List<ExamResult> results = examResultRepository.findByUserId(userId);
        List<Exam> allExams = examRepository.findAll();
        
        int totalAvailable = allExams.size();
        long attempted = examResultRepository.countDistinctExamIdByUserId(userId);
        long passed = results.stream().filter(ExamResult::getPassed).count();
        long failed = results.stream().filter(r -> !r.getPassed()).count();
        
        double averageScore = results.stream()
                .mapToDouble(ExamResult::getPercentage)
                .average()
                .orElse(0.0);
        
        double successRate = attempted > 0 ? (passed * 100.0 / attempted) : 0.0;
        
        return ExamStatsResponse.builder()
                .userId(userId)
                .totalAvailable(totalAvailable)
                .attempted((int) attempted)
                .passed((int) passed)
                .failed((int) failed)
                .averageScore(Math.round(averageScore * 100.0) / 100.0)
                .successRate(Math.round(successRate * 100.0) / 100.0)
                .totalAttempts(results.size())
                .build();
    }
    
    public List<TrainerExamAnalyticsResponse> getTrainerExamAnalytics(Long trainerId) {
        List<ExamResult> results = examResultRepository.findAllByTrainerId(trainerId);
        List<TrainerExamAnalyticsResponse> analytics = new ArrayList<>();
        
        for (ExamResult result : results) {
            try {
                UserResponse user = userClient.getUserById(result.getUserId());
                
                TrainerExamAnalyticsResponse response = new TrainerExamAnalyticsResponse();
                response.setExamId(result.getExam().getId());
                response.setExamTitle(result.getExam().getTitle());
                response.setLearnerId(user.getId());
                response.setLearnerName(user.getFirstName() + " " + user.getLastName());
                response.setLearnerEmail(user.getEmail());
                response.setScore(result.getObtainedMarks());
                response.setMaxScore(result.getTotalMarks());
                response.setPercentage(result.getPercentage());
                response.setStatus(result.getPassed() ? "passed" : "failed");
                response.setCompletedAt(result.getSubmittedAt());
                
                analytics.add(response);
            } catch (Exception e) {
                // Si on ne peut pas récupérer l'utilisateur, on continue
                System.err.println("Error fetching user " + result.getUserId() + ": " + e.getMessage());
            }
        }
        
        return analytics;
    }
}
