package com.smartek.examservice.mapper;

import com.smartek.examservice.dto.*;
import com.smartek.examservice.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExamMapper {

    public Exam toEntity(ExamRequest request) {
        if (request == null) {
            return null;
        }
        
        Exam exam = new Exam();
        exam.setCourseId(request.getCourseId());
        exam.setTrainingId(request.getTrainingId());
        exam.setExamType(request.getExamType() != null ? request.getExamType() : "QUIZ");
        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setDuration(request.getDuration());
        exam.setPassingScore(request.getPassingScore());
        exam.setTotalMarks(request.getTotalMarks());
        exam.setStartDate(request.getStartDate());
        exam.setEndDate(request.getEndDate());
        exam.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        return exam;
    }

    public ExamResponse toResponse(Exam exam) {
        if (exam == null) {
            return null;
        }
        
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

    public ExamResponse toResponseWithQuestions(Exam exam) {
        ExamResponse response = toResponse(exam);
        
        if (exam != null && exam.getQuestions() != null && !exam.getQuestions().isEmpty()) {
            List<QuestionResponse> questions = exam.getQuestions().stream()
                    .map(this::toQuestionResponse)
                    .collect(Collectors.toList());
            response.setQuestions(questions);
        }
        
        return response;
    }

    public QuestionResponse toQuestionResponse(Question question) {
        if (question == null) {
            return null;
        }
        
        QuestionResponse response = new QuestionResponse();
        response.setId(question.getId());
        response.setQuestionText(question.getQuestionText());
        response.setQuestionType(question.getQuestionType());
        response.setMarks(question.getMarks());
        response.setCorrectAnswer(question.getCorrectAnswer());
        
        if (question.getOptions() != null && !question.getOptions().isEmpty()) {
            List<OptionResponse> options = question.getOptions().stream()
                    .map(this::toOptionResponse)
                    .collect(Collectors.toList());
            response.setOptions(options);
        }
        
        return response;
    }

    public OptionResponse toOptionResponse(QuestionOption option) {
        if (option == null) {
            return null;
        }
        
        OptionResponse response = new OptionResponse();
        response.setId(option.getId());
        response.setOptionText(option.getOptionText());
        response.setIsCorrect(option.getIsCorrect());
        
        return response;
    }

    public Question toQuestionEntity(QuestionRequest request, Exam exam) {
        if (request == null) {
            return null;
        }
        
        Question question = new Question();
        question.setExam(exam);
        question.setQuestionText(request.getQuestionText());
        question.setQuestionType(request.getQuestionType());
        question.setMarks(request.getMarks());
        question.setCorrectAnswer(request.getCorrectAnswer());
        
        return question;
    }

    public QuestionOption toOptionEntity(OptionRequest request, Question question) {
        if (request == null) {
            return null;
        }
        
        QuestionOption option = new QuestionOption();
        option.setQuestion(question);
        option.setOptionText(request.getOptionText());
        option.setIsCorrect(request.getIsCorrect());
        
        return option;
    }

    public void updateEntityFromRequest(Exam exam, ExamRequest request) {
        if (exam == null || request == null) {
            return;
        }
        
        exam.setCourseId(request.getCourseId());
        exam.setTrainingId(request.getTrainingId());
        if (request.getExamType() != null) {
            exam.setExamType(request.getExamType());
        }
        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setDuration(request.getDuration());
        exam.setPassingScore(request.getPassingScore());
        exam.setTotalMarks(request.getTotalMarks());
        exam.setStartDate(request.getStartDate());
        exam.setEndDate(request.getEndDate());
        if (request.getIsActive() != null) {
            exam.setIsActive(request.getIsActive());
        }
    }
}
