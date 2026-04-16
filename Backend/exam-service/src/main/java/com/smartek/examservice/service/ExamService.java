package com.smartek.examservice.service;

import com.smartek.examservice.client.CourseClient;
import com.smartek.examservice.client.CourseResponse;
import com.smartek.examservice.client.TrainingClient;
import com.smartek.examservice.client.TrainingResponse;
import com.smartek.examservice.dto.*;
import com.smartek.examservice.entity.*;
import com.smartek.examservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ExamService {
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamResultRepository examResultRepository;
    private final ExamEnrollmentRepository examEnrollmentRepository;
    private final CourseClient courseClient;
    private final TrainingClient trainingClient;
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
        // Use EntityGraph query to fetch questions and options in one JOIN — no lazy loading needed
        Exam exam = examRepository.findByIdWithQuestions(id)
                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + id));
        return mapToResponseWithQuestions(exam);
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
        log.info("Suppression de l'examen avec l'ID: {}", id);
        
        // Vérifier si l'examen existe
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen non trouvé avec l'ID: " + id));
        
        // Supprimer d'abord les résultats d'examen
        examResultRepository.deleteByExamId(id);
        log.info("Résultats d'examen supprimés pour l'examen {}", id);
        
        // Supprimer les enrollments
        examEnrollmentRepository.deleteByExamId(id);
        log.info("Enrollments supprimés pour l'examen {}", id);
        
        // Supprimer les questions associées
        questionRepository.deleteByExamId(id);
        log.info("Questions supprimées pour l'examen {}", id);
        
        // Enfin, supprimer l'examen
        examRepository.deleteById(id);
        log.info("Examen {} supprimé avec succès", id);
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
        // Fetch only active exams to reduce data volume
        List<Exam> allExams = examRepository.findByIsActive(true);
        if (allExams.isEmpty()) return Collections.emptyList();

        // Collect all unique courseIds and trainingIds in one pass — avoid N+1
        Set<Long> courseIds = allExams.stream()
                .filter(e -> e.getCourseId() != null)
                .map(Exam::getCourseId)
                .collect(Collectors.toSet());

        // Batch-fetch course info (one call per unique courseId)
        Map<Long, CourseResponse> courseMap = new HashMap<>();
        for (Long courseId : courseIds) {
            try {
                courseMap.put(courseId, courseClient.getCourse(courseId));
            } catch (Exception e) {
                log.warn("Could not fetch course {}: {}", courseId, e.getMessage());
            }
        }

        // Collect unique trainingIds from fetched courses
        Set<Long> trainingIds = courseMap.values().stream()
                .filter(c -> c.getTrainingId() != null)
                .map(CourseResponse::getTrainingId)
                .collect(Collectors.toSet());

        Map<Long, TrainingResponse> trainingMap = new HashMap<>();
        for (Long trainingId : trainingIds) {
            try {
                trainingMap.put(trainingId, trainingClient.getTraining(trainingId));
            } catch (Exception e) {
                log.warn("Could not fetch training {}: {}", trainingId, e.getMessage());
            }
        }

        // Fetch all exam results for this user in one query
        List<ExamResult> userResults = examResultRepository.findByUserId(userId);
        Map<Long, List<ExamResult>> resultsByExamId = userResults.stream()
                .collect(Collectors.groupingBy(r -> r.getExam().getId()));

        List<LearnerExamResponse> learnerExams = new ArrayList<>();
        for (Exam exam : allExams) {
            try {
                CourseResponse course = courseMap.get(exam.getCourseId());
                if (course == null) continue;

                TrainingResponse training = trainingMap.get(course.getTrainingId());
                if (training == null) continue;

                Boolean hasCompleted = trainingClient.hasCompletedAllCourses(userId, training.getId());
                List<ExamResult> results = resultsByExamId.getOrDefault(exam.getId(), Collections.emptyList());

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
                response.setIsLocked(!Boolean.TRUE.equals(hasCompleted));
                response.setHasAttempted(!results.isEmpty());
                response.setAttemptsCount(results.size());
                response.setBestScore(results.stream()
                        .map(ExamResult::getObtainedMarks)
                        .max(Integer::compareTo)
                        .orElse(null));

                learnerExams.add(response);
            } catch (Exception e) {
                log.warn("Error processing exam {}: {}", exam.getId(), e.getMessage());
            }
        }
        return learnerExams;
    }
}
