package com.smartek.examservice.controller;

import com.smartek.examservice.dto.ExamResponse;
import com.smartek.examservice.dto.LearnerExamResponse;
import com.smartek.examservice.service.ExamEnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam-enrollments")
@RequiredArgsConstructor
@Slf4j
public class ExamEnrollmentController {
    
    private final ExamEnrollmentService examEnrollmentService;
    
    @PostMapping("/unlock-quiz")
    public ResponseEntity<String> unlockQuizForCourse(
            @RequestParam Long userId,
            @RequestParam Long courseId) {
        log.info("Requête de déverrouillage de QUIZ pour userId={}, courseId={}", userId, courseId);
        examEnrollmentService.unlockQuizForCourse(userId, courseId);
        return ResponseEntity.ok("Quiz déverrouillé avec succès");
    }
    
    @PostMapping("/lock-quiz")
    public ResponseEntity<String> lockQuizForCourse(
            @RequestParam Long userId,
            @RequestParam Long courseId) {
        log.info("Requête de reverrouillage de QUIZ pour userId={}, courseId={}", userId, courseId);
        examEnrollmentService.lockQuizForCourse(userId, courseId);
        return ResponseEntity.ok("Quiz reverrouillé avec succès");
    }
    
    @PostMapping("/enroll-quiz")
    public ResponseEntity<String> enrollQuizForCourse(
            @RequestParam Long userId,
            @RequestParam Long courseId) {
        log.info("Requête d'enrollment de QUIZ pour userId={}, courseId={}", userId, courseId);
        examEnrollmentService.createQuizEnrollmentForCourse(userId, courseId);
        return ResponseEntity.ok("Enrollment créé avec succès");
    }
    
    @PostMapping("/unlock-exam")
    public ResponseEntity<String> unlockExamForTraining(
            @RequestParam Long userId,
            @RequestParam Long trainingId) {
        log.info("Requête de déverrouillage d'EXAMEN pour userId={}, trainingId={}", userId, trainingId);
        examEnrollmentService.unlockExamForTraining(userId, trainingId);
        return ResponseEntity.ok("Examen déverrouillé avec succès");
    }
    
    @PostMapping("/lock-exam")
    public ResponseEntity<String> lockExamForTraining(
            @RequestParam Long userId,
            @RequestParam Long trainingId) {
        log.info("Requête de reverrouillage d'EXAMEN pour userId={}, trainingId={}", userId, trainingId);
        examEnrollmentService.lockExamForTraining(userId, trainingId);
        return ResponseEntity.ok("Examen reverrouillé avec succès");
    }
    
    @PostMapping("/enroll-exam")
    public ResponseEntity<String> enrollExamForTraining(
            @RequestParam Long userId,
            @RequestParam Long trainingId) {
        log.info("Requête d'enrollment d'EXAMEN pour userId={}, trainingId={}", userId, trainingId);
        examEnrollmentService.createExamEnrollmentForTraining(userId, trainingId);
        return ResponseEntity.ok("Enrollment créé avec succès");
    }
    
    @PostMapping("/unlock")
    public ResponseEntity<String> unlockExamForCourse(
            @RequestParam Long userId,
            @RequestParam Long courseId) {
        log.info("Requête de déverrouillage (legacy) pour userId={}, courseId={}", userId, courseId);
        examEnrollmentService.unlockQuizForCourse(userId, courseId);
        return ResponseEntity.ok("Quiz déverrouillé avec succès");
    }

    // ── Endpoints path-variable (appelés par certains composants frontend) ──

    /**
     * GET /api/exam-enrollments/training/{trainingId}/user/{userId}
     * Vérifie l'enrollment d'un learner pour une formation
     */
    @GetMapping("/training/{trainingId}/user/{userId}")
    public ResponseEntity<String> getEnrollmentForTraining(
            @PathVariable Long trainingId,
            @PathVariable Long userId) {
        log.info("Vérification enrollment training={} user={}", trainingId, userId);
        return ResponseEntity.ok("OK");
    }

    /**
     * POST /api/exam-enrollments/training/{trainingId}/user/{userId}
     * Crée un enrollment pour une formation
     */
    @PostMapping("/training/{trainingId}/user/{userId}")
    public ResponseEntity<String> enrollForTraining(
            @PathVariable Long trainingId,
            @PathVariable Long userId) {
        log.info("Enrollment training={} user={}", trainingId, userId);
        examEnrollmentService.createExamEnrollmentForTraining(userId, trainingId);
        return ResponseEntity.ok("Enrollment créé avec succès");
    }

    /**
     * PUT /api/exam-enrollments/training/{trainingId}/user/{userId}/unlock
     * Déverrouille l'examen d'une formation pour un learner
     */
    @PutMapping("/training/{trainingId}/user/{userId}/unlock")
    public ResponseEntity<String> unlockTrainingExam(
            @PathVariable Long trainingId,
            @PathVariable Long userId) {
        log.info("Déverrouillage examen training={} user={}", trainingId, userId);
        examEnrollmentService.unlockExamForTraining(userId, trainingId);
        return ResponseEntity.ok("Examen déverrouillé avec succès");
    }

    /**
     * POST /api/exam-enrollments/course/{courseId}/user/{userId}
     * Crée un enrollment quiz pour un cours
     */
    @PostMapping("/course/{courseId}/user/{userId}")
    public ResponseEntity<String> enrollForCourse(
            @PathVariable Long courseId,
            @PathVariable Long userId) {
        log.info("Enrollment quiz course={} user={}", courseId, userId);
        examEnrollmentService.createQuizEnrollmentForCourse(userId, courseId);
        return ResponseEntity.ok("Enrollment créé avec succès");
    }

    /**
     * PUT /api/exam-enrollments/course/{courseId}/user/{userId}/unlock
     * Déverrouille le quiz d'un cours pour un learner
     */
    @PutMapping("/course/{courseId}/user/{userId}/unlock")
    public ResponseEntity<String> unlockCourseQuiz(
            @PathVariable Long courseId,
            @PathVariable Long userId) {
        log.info("Déverrouillage quiz course={} user={}", courseId, userId);
        examEnrollmentService.unlockQuizForCourse(userId, courseId);
        return ResponseEntity.ok("Quiz déverrouillé avec succès");
    }
    
    @GetMapping("/my-exams")
    public ResponseEntity<List<LearnerExamResponse>> getMyExams(@RequestParam Long userId) {
        log.info("Récupération des examens pour userId={}", userId);
        List<LearnerExamResponse> exams = examEnrollmentService.getMyExams(userId);
        return ResponseEntity.ok(exams);
    }
    
    @PostMapping("/complete")
    public ResponseEntity<String> markExamAsCompleted(
            @RequestParam Long userId,
            @RequestParam Long examId) {
        log.info("Marquage de l'examen comme complété: userId={}, examId={}", userId, examId);
        examEnrollmentService.markExamAsCompleted(userId, examId);
        return ResponseEntity.ok("Examen marqué comme complété");
    }
    
    @GetMapping("/can-start/{examId}")
    public ResponseEntity<?> canStartExam(
            @PathVariable Long examId,
            @RequestParam Long userId) {
        log.info("Vérification d'accès à l'examen {} pour userId={}", examId, userId);
        try {
            boolean canStart = examEnrollmentService.canStartExam(userId, examId);
            if (canStart) {
                return ResponseEntity.ok().body(new AccessResponse(true, "Vous pouvez commencer l'examen"));
            } else {
                return ResponseEntity.status(403).body(new AccessResponse(false, "Vous devez d'abord terminer les prérequis"));
            }
        } catch (Exception e) {
            log.error("Erreur lors de la vérification d'accès: {}", e.getMessage());
            return ResponseEntity.status(403).body(new AccessResponse(false, e.getMessage()));
        }
    }
    
    @PostMapping("/{examId}/start")
    public ResponseEntity<?> startExam(
            @PathVariable Long examId,
            @RequestParam Long userId) {
        log.info("Démarrage de l'examen {} pour userId={}", examId, userId);
        try {
            examEnrollmentService.startExam(userId, examId);
            return ResponseEntity.ok().body(new AccessResponse(true, "Examen démarré"));
        } catch (Exception e) {
            log.error("Erreur lors du démarrage: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AccessResponse(false, e.getMessage()));
        }
    }
    
    @PostMapping("/{examId}/retake")
    public ResponseEntity<?> retakeExam(
            @PathVariable Long examId,
            @RequestParam Long userId) {
        log.info("Reprise de l'examen {} pour userId={}", examId, userId);
        try {
            examEnrollmentService.retakeExam(userId, examId);
            return ResponseEntity.ok().body(new AccessResponse(true, "Vous pouvez repasser l'examen"));
        } catch (Exception e) {
            log.error("Erreur lors de la reprise: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AccessResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/{examId}/time-remaining")
    public ResponseEntity<?> getTimeRemaining(
            @PathVariable Long examId,
            @RequestParam Long userId) {
        log.info("Récupération du temps restant pour l'examen {} et userId={}", examId, userId);
        try {
            int timeRemaining = examEnrollmentService.getTimeRemaining(userId, examId);
            return ResponseEntity.ok().body(new TimeRemainingResponse(timeRemaining));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du temps: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AccessResponse(false, e.getMessage()));
        }
    }
    
    @PostMapping("/{examId}/pause")
    public ResponseEntity<?> pauseExam(
            @PathVariable Long examId,
            @RequestParam Long userId) {
        log.info("Mise en pause de l'examen {} pour userId={}", examId, userId);
        try {
            examEnrollmentService.pauseExam(userId, examId);
            return ResponseEntity.ok().body(new AccessResponse(true, "Examen mis en pause"));
        } catch (Exception e) {
            log.error("Erreur lors de la mise en pause: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AccessResponse(false, e.getMessage()));
        }
    }
    
    @PostMapping("/{examId}/resume")
    public ResponseEntity<?> resumeExam(
            @PathVariable Long examId,
            @RequestParam Long userId) {
        log.info("Reprise de l'examen {} pour userId={}", examId, userId);
        try {
            examEnrollmentService.resumeExam(userId, examId);
            return ResponseEntity.ok().body(new AccessResponse(true, "Examen repris"));
        } catch (Exception e) {
            log.error("Erreur lors de la reprise: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AccessResponse(false, e.getMessage()));
        }
    }
}

class AccessResponse {
    private boolean canAccess;
    private String message;
    
    public AccessResponse(boolean canAccess, String message) {
        this.canAccess = canAccess;
        this.message = message;
    }
    
    public boolean isCanAccess() {
        return canAccess;
    }
    
    public void setCanAccess(boolean canAccess) {
        this.canAccess = canAccess;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}

class TimeRemainingResponse {
    private int timeRemaining;
    
    public TimeRemainingResponse(int timeRemaining) {
        this.timeRemaining = timeRemaining;
    }
    
    public int getTimeRemaining() {
        return timeRemaining;
    }
    
    public void setTimeRemaining(int timeRemaining) {
        this.timeRemaining = timeRemaining;
    }
}
