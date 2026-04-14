package com.smartek.examservice.service;

import com.smartek.examservice.client.CourseClient;
import com.smartek.examservice.client.TrainingClient;
import com.smartek.examservice.dto.ExamResponse;
import com.smartek.examservice.dto.LearnerExamResponse;
import com.smartek.examservice.entity.Exam;
import com.smartek.examservice.entity.ExamEnrollment;
import com.smartek.examservice.entity.ExamResult;
import com.smartek.examservice.repository.ExamEnrollmentRepository;
import com.smartek.examservice.repository.ExamRepository;
import com.smartek.examservice.repository.ExamResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamEnrollmentService {
    
    private final ExamEnrollmentRepository examEnrollmentRepository;
    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;
    private final CourseClient courseClient;
    private final TrainingClient trainingClient;
    
    @Transactional
    public void unlockQuizForCourse(Long userId, Long courseId) {
        log.info("Déverrouillage du QUIZ pour le cours {} et l'utilisateur {}", courseId, userId);
        
        // Trouver le quiz (QUIZ) associé au cours
        Exam quiz = examRepository.findFirstByCourseIdAndExamType(courseId, "QUIZ")
                .orElseThrow(() -> new RuntimeException("Aucun quiz trouvé pour ce cours"));
        
        // Vérifier si l'enrollment existe déjà
        ExamEnrollment enrollment = examEnrollmentRepository
                .findByUserIdAndExamId(userId, quiz.getId())
                .orElseGet(() -> {
                    // Créer un nouvel enrollment
                    return ExamEnrollment.builder()
                            .userId(userId)
                            .exam(quiz)
                            .courseId(courseId)
                            .trainingId(null)
                            .isUnlocked(false)
                            .isCompleted(false)
                            .build();
                });
        
        // Déverrouiller le quiz
        if (!enrollment.getIsUnlocked()) {
            enrollment.setIsUnlocked(true);
            enrollment.setUnlockedAt(LocalDateTime.now());
            examEnrollmentRepository.save(enrollment);
            log.info("Quiz {} déverrouillé pour l'utilisateur {}", quiz.getId(), userId);
        }
    }
    
    @Transactional
    public void lockQuizForCourse(Long userId, Long courseId) {
        log.info("Reverrouillage du QUIZ pour le cours {} et l'utilisateur {}", courseId, userId);
        
        // Trouver le quiz (QUIZ) associé au cours
        examRepository.findFirstByCourseIdAndExamType(courseId, "QUIZ")
                .ifPresent(quiz -> {
                    // Trouver l'enrollment
                    examEnrollmentRepository.findByUserIdAndExamId(userId, quiz.getId())
                            .ifPresent(enrollment -> {
                                // Reverrouiller le quiz
                                enrollment.setIsUnlocked(false);
                                enrollment.setUnlockedAt(null);
                                examEnrollmentRepository.save(enrollment);
                                log.info("Quiz {} reverrouillé pour l'utilisateur {}", quiz.getId(), userId);
                            });
                });
    }
    
    @Transactional
    public void createQuizEnrollmentForCourse(Long userId, Long courseId) {
        log.info("Création de l'enrollment pour le quiz du cours {} et l'utilisateur {}", courseId, userId);
        
        // Trouver le quiz (QUIZ) associé au cours
        examRepository.findFirstByCourseIdAndExamType(courseId, "QUIZ")
                .ifPresent(quiz -> {
                    // Vérifier si l'enrollment existe déjà
                    if (!examEnrollmentRepository.existsByUserIdAndExamId(userId, quiz.getId())) {
                        // Créer un nouvel enrollment verrouillé
                        ExamEnrollment enrollment = ExamEnrollment.builder()
                                .userId(userId)
                                .exam(quiz)
                                .courseId(courseId)
                                .trainingId(null)
                                .isUnlocked(false)
                                .isCompleted(false)
                                .build();
                        examEnrollmentRepository.save(enrollment);
                        log.info("Enrollment créé pour le quiz {} et l'utilisateur {}", quiz.getId(), userId);
                    }
                });
    }
    
    @Transactional
    public void unlockExamForTraining(Long userId, Long trainingId) {
        log.info("Déverrouillage de l'EXAMEN pour la formation {} et l'utilisateur {}", trainingId, userId);
        
        // Trouver l'examen (EXAM) associé à la formation
        Exam exam = examRepository.findFirstByTrainingIdAndExamType(trainingId, "EXAM")
                .orElseThrow(() -> new RuntimeException("Aucun examen trouvé pour cette formation"));
        
        // Vérifier si l'enrollment existe déjà
        ExamEnrollment enrollment = examEnrollmentRepository
                .findByUserIdAndExamId(userId, exam.getId())
                .orElseGet(() -> {
                    // Créer un nouvel enrollment
                    return ExamEnrollment.builder()
                            .userId(userId)
                            .exam(exam)
                            .courseId(null)
                            .trainingId(trainingId)
                            .isUnlocked(false)
                            .isCompleted(false)
                            .build();
                });
        
        // Déverrouiller l'examen
        if (!enrollment.getIsUnlocked()) {
            enrollment.setIsUnlocked(true);
            enrollment.setUnlockedAt(LocalDateTime.now());
            examEnrollmentRepository.save(enrollment);
            log.info("Examen {} déverrouillé pour l'utilisateur {}", exam.getId(), userId);
        }
    }
    
    @Transactional
    public void lockExamForTraining(Long userId, Long trainingId) {
        log.info("Reverrouillage de l'EXAMEN pour la formation {} et l'utilisateur {}", trainingId, userId);
        
        // Trouver l'examen (EXAM) associé à la formation
        examRepository.findFirstByTrainingIdAndExamType(trainingId, "EXAM")
                .ifPresent(exam -> {
                    // Trouver l'enrollment
                    examEnrollmentRepository.findByUserIdAndExamId(userId, exam.getId())
                            .ifPresent(enrollment -> {
                                // Reverrouiller l'examen
                                enrollment.setIsUnlocked(false);
                                enrollment.setUnlockedAt(null);
                                examEnrollmentRepository.save(enrollment);
                                log.info("Examen {} reverrouillé pour l'utilisateur {}", exam.getId(), userId);
                            });
                });
    }
    
    @Transactional
    public void createExamEnrollmentForTraining(Long userId, Long trainingId) {
        log.info("Création de l'enrollment pour l'examen de la formation {} et l'utilisateur {}", trainingId, userId);
        
        // Trouver l'examen (EXAM) associé à la formation
        examRepository.findFirstByTrainingIdAndExamType(trainingId, "EXAM")
                .ifPresent(exam -> {
                    // Vérifier si l'enrollment existe déjà
                    if (!examEnrollmentRepository.existsByUserIdAndExamId(userId, exam.getId())) {
                        // Créer un nouvel enrollment verrouillé
                        ExamEnrollment enrollment = ExamEnrollment.builder()
                                .userId(userId)
                                .exam(exam)
                                .courseId(null)
                                .trainingId(trainingId)
                                .isUnlocked(false)
                                .isCompleted(false)
                                .build();
                        examEnrollmentRepository.save(enrollment);
                        log.info("Enrollment créé pour l'examen {} et l'utilisateur {}", exam.getId(), userId);
                    }
                });
    }
    
    @Transactional
    public List<LearnerExamResponse> getMyExams(Long userId) {
        log.info("Récupération dynamique des examens pour l'utilisateur {}", userId);
        
        // Récupérer tous les enrollments existants
        List<ExamEnrollment> existingEnrollments = examEnrollmentRepository.findByUserId(userId);
        
        // Récupérer tous les examens actifs
        List<Exam> allExams = examRepository.findByIsActive(true);
        
        // Pour chaque examen, créer un enrollment s'il n'existe pas
        for (Exam exam : allExams) {
            boolean enrollmentExists = existingEnrollments.stream()
                    .anyMatch(e -> e.getExam().getId().equals(exam.getId()));
            
            if (!enrollmentExists) {
                // Créer automatiquement l'enrollment verrouillé
                ExamEnrollment newEnrollment = ExamEnrollment.builder()
                        .userId(userId)
                        .exam(exam)
                        .courseId(exam.getCourseId())
                        .trainingId(exam.getTrainingId())
                        .isUnlocked(false)
                        .isCompleted(false)
                        .build();
                
                ExamEnrollment saved = examEnrollmentRepository.save(newEnrollment);
                existingEnrollments.add(saved);
                log.info("Enrollment automatique créé pour l'examen {} (type: {}) pour l'utilisateur {}", 
                        exam.getId(), exam.getExamType(), userId);
            }
        }
        
        // Retourner tous les enrollments avec leur statut vérifié dynamiquement
        return existingEnrollments.stream()
                .map(enrollment -> mapToLearnerResponse(enrollment, userId))
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void markExamAsCompleted(Long userId, Long examId) {
        log.info("Marquage de l'examen {} comme complété pour l'utilisateur {}", examId, userId);
        
        ExamEnrollment enrollment = examEnrollmentRepository
                .findByUserIdAndExamId(userId, examId)
                .orElseThrow(() -> new RuntimeException("Enrollment non trouvé"));
        
        enrollment.setIsCompleted(true);
        enrollment.setCompletedAt(LocalDateTime.now());
        examEnrollmentRepository.save(enrollment);
    }
    
    public boolean canStartExam(Long userId, Long examId) {
        log.info("Vérification si l'utilisateur {} peut commencer l'examen {}", userId, examId);
        
        // Récupérer l'examen
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Examen non trouvé"));
        
        // Vérifier selon le type d'examen
        try {
            if ("QUIZ".equals(exam.getExamType()) && exam.getCourseId() != null) {
                // Pour un QUIZ, vérifier si le cours est terminé
                boolean courseCompleted = courseClient.isCourseCompleted(exam.getCourseId(), userId);
                log.info("Quiz {} - Cours {} terminé: {}", examId, exam.getCourseId(), courseCompleted);
                
                if (!courseCompleted) {
                    throw new RuntimeException("Vous devez d'abord terminer le cours avant de passer le quiz");
                }
                return true;
                
            } else if ("EXAM".equals(exam.getExamType()) && exam.getTrainingId() != null) {
                // Pour un EXAM, vérifier si la formation est terminée (100%)
                boolean trainingCompleted = trainingClient.hasCompletedAllCourses(userId, exam.getTrainingId());
                log.info("Exam {} - Formation {} terminée: {}", examId, exam.getTrainingId(), trainingCompleted);
                
                if (!trainingCompleted) {
                    throw new RuntimeException("Vous devez d'abord terminer tous les cours de la formation avant de passer l'examen final");
                }
                return true;
                
            } else {
                log.warn("Examen {} n'a ni courseId ni trainingId valide", examId);
                return true; // Permettre l'accès par défaut si pas de prérequis
            }
        } catch (RuntimeException e) {
            // Relancer les exceptions métier
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la vérification des prérequis pour l'examen {}: {}", examId, e.getMessage());
            throw new RuntimeException("Erreur lors de la vérification des prérequis: " + e.getMessage());
        }
    }
    
    private ExamResponse mapToResponse(Exam exam, ExamEnrollment enrollment) {
        ExamResponse response = new ExamResponse();
        response.setId(exam.getId());
        response.setCourseId(exam.getCourseId());
        response.setExamType(exam.getExamType());
        response.setTitle(exam.getTitle());
        response.setDescription(exam.getDescription());
        response.setDuration(exam.getDuration());
        response.setPassingScore(exam.getPassingScore());
        response.setTotalMarks(exam.getTotalMarks());
        response.setStartDate(exam.getStartDate());
        response.setEndDate(exam.getEndDate());
        response.setIsActive(exam.getIsActive());
        response.setCreatedAt(exam.getCreatedAt());
        response.setUpdatedAt(exam.getUpdatedAt());
        
        // Ajouter les informations d'enrollment
        if (enrollment != null) {
            response.setIsUnlocked(enrollment.getIsUnlocked());
            response.setIsCompleted(enrollment.getIsCompleted());
        }
        
        return response;
    }
    
    private ExamResponse mapToResponseWithDynamicCheck(ExamEnrollment enrollment, Long userId) {
        Exam exam = enrollment.getExam();
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
        response.setCreatedAt(exam.getCreatedAt());
        response.setUpdatedAt(exam.getUpdatedAt());
        response.setIsCompleted(enrollment.getIsCompleted());
        
        // Vérifier dynamiquement si l'examen doit être déverrouillé
        boolean shouldBeUnlocked = false;
        
        try {
            if ("QUIZ".equals(exam.getExamType()) && exam.getCourseId() != null) {
                // Pour un QUIZ, vérifier si le cours est terminé
                log.info("Vérification du QUIZ {} pour le cours {} et userId {}", exam.getId(), exam.getCourseId(), userId);
                shouldBeUnlocked = courseClient.isCourseCompleted(exam.getCourseId(), userId);
                log.info("Quiz {} - Cours {} terminé: {}", exam.getId(), exam.getCourseId(), shouldBeUnlocked);
            } else if ("EXAM".equals(exam.getExamType()) && exam.getTrainingId() != null) {
                // Pour un EXAM, vérifier si la formation est terminée (100%)
                log.info("Vérification de l'EXAM {} pour la formation {} et userId {}", exam.getId(), exam.getTrainingId(), userId);
                shouldBeUnlocked = trainingClient.hasCompletedAllCourses(userId, exam.getTrainingId());
                log.info("Exam {} - Formation {} terminée: {}", exam.getId(), exam.getTrainingId(), shouldBeUnlocked);
            } else {
                log.warn("Examen {} n'a ni courseId ni trainingId valide. Type: {}, CourseId: {}, TrainingId: {}", 
                        exam.getId(), exam.getExamType(), exam.getCourseId(), exam.getTrainingId());
            }
        } catch (Exception e) {
            log.error("Erreur lors de la vérification du statut de complétion pour l'examen {}: {}", 
                    exam.getId(), e.getMessage(), e);
            // En cas d'erreur, utiliser le statut stocké
            shouldBeUnlocked = enrollment.getIsUnlocked();
        }
        
        // Mettre à jour le statut dans la base de données si nécessaire
        if (shouldBeUnlocked != enrollment.getIsUnlocked()) {
            enrollment.setIsUnlocked(shouldBeUnlocked);
            if (shouldBeUnlocked && enrollment.getUnlockedAt() == null) {
                enrollment.setUnlockedAt(LocalDateTime.now());
            } else if (!shouldBeUnlocked) {
                enrollment.setUnlockedAt(null);
            }
            examEnrollmentRepository.save(enrollment);
            log.info("Statut de déverrouillage mis à jour pour l'examen {} (userId: {}): {} -> {}", 
                    exam.getId(), userId, enrollment.getIsUnlocked(), shouldBeUnlocked);
        } else {
            log.debug("Statut de déverrouillage inchangé pour l'examen {} (userId: {}): {}", 
                    exam.getId(), userId, shouldBeUnlocked);
        }
        
        response.setIsUnlocked(shouldBeUnlocked);
        
        return response;
    }
    
    private LearnerExamResponse mapToLearnerResponse(ExamEnrollment enrollment, Long userId) {
        Exam exam = enrollment.getExam();
        LearnerExamResponse response = new LearnerExamResponse();
        
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
        
        // Récupérer le nom du cours ou de la formation
        try {
            if ("QUIZ".equals(exam.getExamType()) && exam.getCourseId() != null) {
                String courseName = courseClient.getCourse(exam.getCourseId()).getTitle();
                response.setCourseName(courseName);
            } else if ("EXAM".equals(exam.getExamType()) && exam.getTrainingId() != null) {
                String trainingName = trainingClient.getTraining(exam.getTrainingId()).getName();
                response.setTrainingName(trainingName);
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du nom du cours/formation: {}", e.getMessage());
        }
        
        // Vérifier dynamiquement si l'examen doit être déverrouillé
        boolean shouldBeUnlocked = false;
        
        try {
            if ("QUIZ".equals(exam.getExamType()) && exam.getCourseId() != null) {
                shouldBeUnlocked = courseClient.isCourseCompleted(exam.getCourseId(), userId);
            } else if ("EXAM".equals(exam.getExamType()) && exam.getTrainingId() != null) {
                shouldBeUnlocked = trainingClient.hasCompletedAllCourses(userId, exam.getTrainingId());
            }
        } catch (Exception e) {
            log.error("Erreur lors de la vérification du statut: {}", e.getMessage());
            shouldBeUnlocked = enrollment.getIsUnlocked();
        }
        
        // Mettre à jour le statut dans la base de données si nécessaire
        if (shouldBeUnlocked != enrollment.getIsUnlocked()) {
            enrollment.setIsUnlocked(shouldBeUnlocked);
            if (shouldBeUnlocked && enrollment.getUnlockedAt() == null) {
                enrollment.setUnlockedAt(LocalDateTime.now());
            } else if (!shouldBeUnlocked) {
                enrollment.setUnlockedAt(null);
            }
            examEnrollmentRepository.save(enrollment);
        }
        
        response.setIsLocked(!shouldBeUnlocked);
        
        // Récupérer les résultats de l'examen pour ce learner
        List<ExamResult> results = examResultRepository.findByExamIdAndUserId(exam.getId(), userId);
        
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
        
        // Calculer le temps restant avant la fin de l'examen
        if (exam.getEndDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(exam.getEndDate())) {
                // Calculer la différence en minutes
                long minutesRemaining = java.time.Duration.between(now, exam.getEndDate()).toMinutes();
                response.setRemainingTimeMinutes(minutesRemaining);
            } else {
                // L'examen est terminé
                response.setRemainingTimeMinutes(0L);
            }
        } else {
            // Pas de date de fin définie
            response.setRemainingTimeMinutes(null);
        }
        
        return response;
    }
    
    @Transactional
    public void startExam(Long userId, Long examId) {
        log.info("Démarrage de l'examen {} pour l'utilisateur {}", examId, userId);
        
        ExamEnrollment enrollment = examEnrollmentRepository
                .findByUserIdAndExamId(userId, examId)
                .orElseThrow(() -> new RuntimeException("Enrollment non trouvé"));
        
        // Vérifier si l'examen est déverrouillé
        if (!enrollment.getIsUnlocked()) {
            throw new RuntimeException("Cet examen est verrouillé");
        }
        
        // Marquer le début de l'examen
        if (enrollment.getStartedAt() == null) {
            enrollment.setStartedAt(LocalDateTime.now());
            enrollment.setPausedAt(null);
            enrollment.setTimeSpentSeconds(0);
            examEnrollmentRepository.save(enrollment);
            log.info("Examen {} démarré à {} pour l'utilisateur {}", examId, enrollment.getStartedAt(), userId);
        } else if (enrollment.getPausedAt() != null) {
            // Si l'examen était en pause, le reprendre
            resumeExam(userId, examId);
        }
    }
    
    @Transactional
    public void pauseExam(Long userId, Long examId) {
        log.info("Mise en pause de l'examen {} pour l'utilisateur {}", examId, userId);
        
        ExamEnrollment enrollment = examEnrollmentRepository
                .findByUserIdAndExamId(userId, examId)
                .orElseThrow(() -> new RuntimeException("Enrollment non trouvé"));
        
        // Si l'examen est en cours (pas en pause)
        if (enrollment.getStartedAt() != null && enrollment.getPausedAt() == null) {
            LocalDateTime now = LocalDateTime.now();
            
            // Calculer le temps écoulé depuis le dernier démarrage/reprise
            long secondsElapsed = java.time.Duration.between(enrollment.getStartedAt(), now).getSeconds();
            
            // Ajouter au temps total passé
            int currentTimeSpent = enrollment.getTimeSpentSeconds() != null ? enrollment.getTimeSpentSeconds() : 0;
            enrollment.setTimeSpentSeconds((int) (currentTimeSpent + secondsElapsed));
            
            // Marquer comme en pause
            enrollment.setPausedAt(now);
            
            examEnrollmentRepository.save(enrollment);
            log.info("Examen {} mis en pause. Temps total passé: {} secondes", examId, enrollment.getTimeSpentSeconds());
        }
    }
    
    @Transactional
    public void resumeExam(Long userId, Long examId) {
        log.info("Reprise de l'examen {} pour l'utilisateur {}", examId, userId);
        
        ExamEnrollment enrollment = examEnrollmentRepository
                .findByUserIdAndExamId(userId, examId)
                .orElseThrow(() -> new RuntimeException("Enrollment non trouvé"));
        
        // Si l'examen était en pause
        if (enrollment.getPausedAt() != null) {
            // Redémarrer le compteur
            enrollment.setStartedAt(LocalDateTime.now());
            enrollment.setPausedAt(null);
            
            examEnrollmentRepository.save(enrollment);
            log.info("Examen {} repris pour l'utilisateur {}", examId, userId);
        }
    }
    
    @Transactional
    public void retakeExam(Long userId, Long examId) {
        log.info("Reprise de l'examen {} pour l'utilisateur {}", examId, userId);
        
        ExamEnrollment enrollment = examEnrollmentRepository
                .findByUserIdAndExamId(userId, examId)
                .orElseThrow(() -> new RuntimeException("Enrollment non trouvé"));
        
        // Vérifier si l'examen est déverrouillé
        if (!enrollment.getIsUnlocked()) {
            throw new RuntimeException("Cet examen est verrouillé");
        }
        
        // Compter TOUS les résultats existants
        List<ExamResult> allResults = examResultRepository.findByExamIdAndUserId(examId, userId);
        int totalAttempts = allResults.size();
        
        log.info("Nombre total de tentatives existantes: {}", totalAttempts);
        
        // Limiter à 2 tentatives maximum
        // Si l'utilisateur a déjà 2 résultats ou plus, bloquer
        if (totalAttempts >= 2) {
            throw new RuntimeException("Vous avez déjà utilisé votre tentative de reprise. Nombre maximum de tentatives atteint (2).");
        }
        
        // Si on arrive ici, c'est qu'il a 0 ou 1 tentative
        // On réinitialise juste l'enrollment pour permettre une nouvelle tentative
        // SANS supprimer les anciens résultats (pour garder l'historique)
        
        enrollment.setIsCompleted(false);
        enrollment.setCompletedAt(null);
        enrollment.setStartedAt(null);
        enrollment.setPausedAt(null);
        enrollment.setTimeSpentSeconds(0);
        examEnrollmentRepository.save(enrollment);
        
        log.info("Examen {} réinitialisé pour l'utilisateur {} (ce sera la tentative {} sur 2)", 
                examId, userId, totalAttempts + 1);
    }
    
    public int getTimeRemaining(Long userId, Long examId) {
        log.info("Calcul du temps restant pour l'examen {} et l'utilisateur {}", examId, userId);
        
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Examen non trouvé"));
        
        ExamEnrollment enrollment = examEnrollmentRepository
                .findByUserIdAndExamId(userId, examId)
                .orElseThrow(() -> new RuntimeException("Enrollment non trouvé"));
        
        int totalSeconds = exam.getDuration() * 60;
        
        // Si l'examen n'a pas encore été démarré, retourner la durée totale
        if (enrollment.getStartedAt() == null) {
            return totalSeconds;
        }
        
        int timeSpent = enrollment.getTimeSpentSeconds() != null ? enrollment.getTimeSpentSeconds() : 0;
        
        // Si l'examen est en pause, utiliser le temps déjà passé
        if (enrollment.getPausedAt() != null) {
            int timeRemaining = totalSeconds - timeSpent;
            log.info("Examen en pause. Temps passé: {} secondes, Temps restant: {} secondes", timeSpent, timeRemaining);
            return Math.max(0, timeRemaining);
        }
        
        // Si l'examen est en cours, calculer le temps écoulé depuis le dernier démarrage/reprise
        LocalDateTime now = LocalDateTime.now();
        long secondsSinceStart = java.time.Duration.between(enrollment.getStartedAt(), now).getSeconds();
        
        int totalTimeSpent = (int) (timeSpent + secondsSinceStart);
        int timeRemaining = totalSeconds - totalTimeSpent;
        
        log.info("Temps total passé: {} secondes, Temps restant: {} secondes", totalTimeSpent, timeRemaining);
        return Math.max(0, timeRemaining);
    }
}
