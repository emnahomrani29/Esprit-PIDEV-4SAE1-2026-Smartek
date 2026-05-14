package com.smartek.examservice.service;

import com.smartek.examservice.dto.ExerciseRequest;
import com.smartek.examservice.dto.ExerciseResponse;
import com.smartek.examservice.entity.Exam;
import com.smartek.examservice.entity.Exercise;
import com.smartek.examservice.repository.ExamRepository;
import com.smartek.examservice.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final ExamRepository examRepository;

    @Transactional
    public ExerciseResponse createExercise(ExerciseRequest request) {
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        Exercise exercise = Exercise.builder()
                .exam(exam)
                .exerciseNumber(request.getExerciseNumber())
                .content(request.getContent())
                .marks(request.getMarks())
                .instructions(request.getInstructions())
                .build();

        Exercise savedExercise = exerciseRepository.save(exercise);
        return mapToResponse(savedExercise);
    }

    public List<ExerciseResponse> getExercisesByExam(Long examId) {
        return exerciseRepository.findByExamIdOrderByExerciseNumberAsc(examId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ExerciseResponse getExerciseById(Long id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercise not found"));
        return mapToResponse(exercise);
    }

    @Transactional
    public ExerciseResponse updateExercise(Long id, ExerciseRequest request) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercise not found"));

        exercise.setExerciseNumber(request.getExerciseNumber());
        exercise.setContent(request.getContent());
        exercise.setMarks(request.getMarks());
        exercise.setInstructions(request.getInstructions());

        Exercise updatedExercise = exerciseRepository.save(exercise);
        return mapToResponse(updatedExercise);
    }

    @Transactional
    public void deleteExercise(Long id) {
        exerciseRepository.deleteById(id);
    }

    private ExerciseResponse mapToResponse(Exercise exercise) {
        ExerciseResponse response = new ExerciseResponse();
        response.setId(exercise.getId());
        response.setExamId(exercise.getExam().getId());
        response.setExerciseNumber(exercise.getExerciseNumber());
        response.setContent(exercise.getContent());
        response.setMarks(exercise.getMarks());
        response.setInstructions(exercise.getInstructions());
        return response;
    }
}
