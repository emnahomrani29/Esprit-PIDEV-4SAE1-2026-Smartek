package com.smartek.examservice.service;

import com.smartek.examservice.client.CourseClient;
import com.smartek.examservice.client.CourseResponse;
import com.smartek.examservice.client.TrainingClient;
import com.smartek.examservice.client.TrainingResponse;
import com.smartek.examservice.dto.*;
import com.smartek.examservice.entity.*;
import com.smartek.examservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService {
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamResultRepository examResultRepository;
    private final ExamEnrollmentRepository examEnrollmentRepository;
    private final CourseClient courseClient;
    private final TrainingClient trainingClient;

    @Transactional
    @CacheEvict(value = {"exams", "examsByCourse"}, allEntries = true)
    public ExamResponse createExam(ExamRequest request) {
        // Calculer totalMarks automatiquement si des questions sont fournies
        Integer totalMarks = request.getTotalMarks();
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            totalMarks = request.getQuestions().stream()
                    .mapToInt(q -> q.getMarks() != null ? q.getMarks() : 0)
                    .sum();
        }
        
        Exam exam = new Exam();
        exam.setCourseId(request.getCourseId());
        exam.setTrainingId(request.getTrainingId());
        exam.setExamType(request.getExamType() != null ? request.getExamType() : "QUIZ");
        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setDuration(request.getDuration());
        exam.setPassingScore(request.getPassingScore());
        exam.setTotalMarks(totalMarks);
        exam.setStartDate(request.getStartDate());
        exam.setEndDate(request.getEndDate());
        exam.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        exam.setCreatedBy(request.getCreatedBy());
        
        Exam savedExam = examRepository.save(exam);
        
        // Créer les questions si fournies
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            for (QuestionRequest questionReq : request.getQuestions()) {
                Question question = new Question();
                question.setExam(savedExam);
                question.setQuestionText(questionReq.getQuestionText());
                question.setQuestionType(questionReq.getQuestionType());
                question.setMarks(questionReq.getMarks());
                question.setCorrectAnswer(questionReq.getCorrectAnswer());
                
                if (questionReq.getOptions() != null && !questionReq.getOptions().isEmpty()) {
                    List<QuestionOption> options = questionReq.getOptions().stream()
                            .map(optReq -> {
                                QuestionOption option = new QuestionOption();
                                option.setQuestion(question);
                                option.setOptionText(optReq.getOptionText());
                                option.setIsCorrect(optReq.getIsCorrect());
                                return option;
                            })
                            .collect(Collectors.toList());
                    question.setOptions(options);
                }
                
                questionRepository.save(question);
            }
        }
        
        return mapToResponse(savedExam);
    }

    @Cacheable(value = "exams", unless = "#result.isEmpty()")
    public List<ExamResponse> getAllExams() {
        return examRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public Page<ExamResponse> getAllExamsPaginated(Pageable pageable) {
        return examRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Cacheable(value = "examsByCourse", key = "#courseId")
    public List<ExamResponse> getExamsByCourse(Long courseId) {
        return examRepository.findByCourseId(courseId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "exam", key = "#id")
    @Transactional(readOnly = true)
    public ExamResponse getExamById(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + id));
        
        try {
            // Charger les questions et leurs options de manière explicite
            if (exam.getQuestions() != null) {
                exam.getQuestions().size(); // Force lazy loading
                exam.getQuestions().forEach(q -> {
                    if (q.getOptions() != null) {
                        q.getOptions().size(); // Force lazy loading of options
                    }
                });
            }
            return mapToResponseWithQuestions(exam);
        } catch (Exception e) {
            // Si erreur lors du mapping avec questions, essayer sans questions
            System.err.println("Error mapping exam with questions: " + e.getMessage());
            e.printStackTrace();
            return mapToResponse(exam);
        }
    }

    @Transactional
    @CachePut(value = "exam", key = "#id")
    @CacheEvict(value = {"exams", "examsByCourse"}, allEntries = true)
    public ExamResponse updateExam(Long id, ExamRequest request) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        
        // Mettre à jour les champs de base
        exam.setCourseId(request.getCourseId());
        exam.setTrainingId(request.getTrainingId());
        if (request.getExamType() != null) {
            exam.setExamType(request.getExamType());
        }
        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setDuration(request.getDuration());
        exam.setPassingScore(request.getPassingScore());
        exam.setStartDate(request.getStartDate());
        exam.setEndDate(request.getEndDate());
        if (request.getIsActive() != null) {
            exam.setIsActive(request.getIsActive());
        }
        
        // Calculer totalMarks automatiquement si des questions sont fournies
        Integer totalMarks = request.getTotalMarks();
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            totalMarks = request.getQuestions().stream()
                    .mapToInt(q -> q.getMarks() != null ? q.getMarks() : 0)
                    .sum();
            
            // Supprimer les anciennes questions en utilisant orphanRemoval
            if (exam.getQuestions() != null) {
                exam.getQuestions().clear();
            } else {
                exam.setQuestions(new ArrayList<>());
            }
            
            // Créer les nouvelles questions
            for (QuestionRequest questionReq : request.getQuestions()) {
                Question question = new Question();
                question.setExam(exam);
                question.setQuestionText(questionReq.getQuestionText());
                question.setQuestionType(questionReq.getQuestionType());
                question.setMarks(questionReq.getMarks());
                question.setCorrectAnswer(questionReq.getCorrectAnswer());
                
                if (questionReq.getOptions() != null && !questionReq.getOptions().isEmpty()) {
                    List<QuestionOption> options = questionReq.getOptions().stream()
                            .map(optReq -> {
                                QuestionOption option = new QuestionOption();
                                option.setQuestion(question);
                                option.setOptionText(optReq.getOptionText());
                                option.setIsCorrect(optReq.getIsCorrect());
                                return option;
                            })
                            .collect(Collectors.toList());
                    question.setOptions(options);
                }
                
                exam.getQuestions().add(question);
            }
        }
        
        exam.setTotalMarks(totalMarks);
        
        Exam updatedExam = examRepository.save(exam);
        return mapToResponse(updatedExam);
    }

    @Transactional
    @CacheEvict(value = {"exam", "exams", "examsByCourse"}, allEntries = true)
    public void deleteExam(Long id) {
        examRepository.deleteById(id);
    }
    
    @Transactional
    @CacheEvict(value = {"exam", "exams", "examsByCourse"}, allEntries = true)
    public void deleteExamsByTrainingId(Long trainingId) {
        // Supprimer d'abord les enrollments
        examEnrollmentRepository.deleteByTrainingId(trainingId);
        
        // Ensuite supprimer les examens
        List<Exam> exams = examRepository.findByTrainingId(trainingId);
        if (!exams.isEmpty()) {
            examRepository.deleteAll(exams);
        }
    }
    
    @Transactional
    @CacheEvict(value = {"exam", "exams", "examsByCourse"}, allEntries = true)
    public void deleteQuizzesByCourseId(Long courseId) {
        // Supprimer d'abord les enrollments
        examEnrollmentRepository.deleteByCourseId(courseId);
        
        // Ensuite supprimer les quiz
        List<Exam> quizzes = examRepository.findByCourseId(courseId);
        if (!quizzes.isEmpty()) {
            examRepository.deleteAll(quizzes);
        }
    }

    private ExamResponse mapToResponse(Exam exam) {
        ExamResponse response = new ExamResponse();
        response.setId(exam.getId());
        response.setCourseId(exam.getCourseId());
        response.setTrainingId(exam.getTrainingId());
        response.setExamType(exam.getExamType());
        response.setTitle(exam.getTitle());
        response.setDescription(exam.getDescription());
        response.setDuration(exam.getDuration());
        response.setPassingScore(exam.getPassingScore());
        response.setTotalMarks(exam.getTotalMarks());
        response.setStartDate(exam.getStartDate());
        response.setEndDate(exam.getEndDate());
        response.setIsActive(exam.getIsActive());
        response.setQuestionCount(exam.getQuestions() != null ? exam.getQuestions().size() : 0);
        response.setExerciseCount(exam.getExercises() != null ? exam.getExercises().size() : 0);
        response.setCreatedAt(exam.getCreatedAt());
        response.setUpdatedAt(exam.getUpdatedAt());
        return response;
    }
    
    private ExamResponse mapToResponseWithQuestions(Exam exam) {
        ExamResponse response = mapToResponse(exam);
        
        if (exam.getQuestions() != null && !exam.getQuestions().isEmpty()) {
            List<QuestionResponse> questions = exam.getQuestions().stream()
                    .map(this::mapQuestionToResponse)
                    .collect(Collectors.toList());
            response.setQuestions(questions);
        }
        
        return response;
    }
    
    private QuestionResponse mapQuestionToResponse(Question question) {
        QuestionResponse response = new QuestionResponse();
        response.setId(question.getId());
        response.setQuestionText(question.getQuestionText());
        response.setQuestionType(question.getQuestionType());
        response.setMarks(question.getMarks());
        response.setCorrectAnswer(question.getCorrectAnswer());
        
        if (question.getOptions() != null && !question.getOptions().isEmpty()) {
            List<OptionResponse> options = question.getOptions().stream()
                    .map(this::mapOptionToResponse)
                    .collect(Collectors.toList());
            response.setOptions(options);
        }
        
        return response;
    }
    
    private OptionResponse mapOptionToResponse(QuestionOption option) {
        OptionResponse response = new OptionResponse();
        response.setId(option.getId());
        response.setOptionText(option.getOptionText());
        response.setIsCorrect(option.getIsCorrect());
        return response;
    }

    public List<LearnerExamResponse> getLearnerExams(Long userId) {
        List<Exam> allExams = examRepository.findAll();
        List<LearnerExamResponse> learnerExams = new ArrayList<>();

        for (Exam exam : allExams) {
            try {
                // Récupérer les informations du cours
                CourseResponse course = courseClient.getCourse(exam.getCourseId());
                
                // Récupérer les informations de la formation
                TrainingResponse training = trainingClient.getTraining(course.getTrainingId());
                
                // Vérifier si le learner a complété tous les cours de la formation
                Boolean hasCompleted = trainingClient.hasCompletedAllCourses(userId, training.getId());
                
                // Récupérer les résultats de l'examen pour ce learner
                List<ExamResult> results = examResultRepository.findByExamIdAndUserId(exam.getId(), userId);
                
                LearnerExamResponse response = new LearnerExamResponse();
                response.setId(exam.getId());
                response.setCourseId(exam.getCourseId());
                response.setCourseName(course.getTitle());
                response.setTrainingId(training.getId());
                response.setTrainingName(training.getName());
                response.setExamType(exam.getExamType());
                response.setTitle(exam.getTitle());
                response.setDescription(exam.getDescription());
                response.setDuration(exam.getDuration());
                response.setPassingScore(exam.getPassingScore());
                response.setTotalMarks(exam.getTotalMarks());
                response.setStartDate(exam.getStartDate());
                response.setEndDate(exam.getEndDate());
                response.setIsActive(exam.getIsActive());
                response.setIsLocked(!hasCompleted); // Bloqué si pas complété
                response.setHasAttempted(!results.isEmpty());
                response.setAttemptsCount(results.size());
                
                // Calculer le meilleur score
                if (!results.isEmpty()) {
                    Integer bestScore = results.stream()
                            .map(ExamResult::getObtainedMarks)
                            .max(Integer::compareTo)
                            .orElse(0);
                    response.setBestScore(bestScore);
                } else {
                    response.setBestScore(null);
                }
                
                learnerExams.add(response);
            } catch (Exception e) {
                // Si erreur (cours ou formation non trouvé), on ignore cet examen
                System.err.println("Error processing exam " + exam.getId() + ": " + e.getMessage());
            }
        }

        return learnerExams;
    }
}
