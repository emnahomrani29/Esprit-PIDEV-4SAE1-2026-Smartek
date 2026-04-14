package com.smartek.examservice.service;

import com.smartek.examservice.dto.QuestionRequest;
import com.smartek.examservice.entity.*;
import com.smartek.examservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;

    @Transactional
    public Question createQuestion(QuestionRequest request) {
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        Question question = new Question();
        question.setExam(exam);
        question.setQuestionText(request.getQuestionText());
        question.setQuestionType(request.getQuestionType());
        question.setMarks(request.getMarks());
        question.setCorrectAnswer(request.getCorrectAnswer());

        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            List<QuestionOption> options = request.getOptions().stream()
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

        return questionRepository.save(question);
    }

    public List<Question> getQuestionsByExam(Long examId) {
        return questionRepository.findByExamId(examId);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }
}
