package com.smartek.examservice.service;

import com.smartek.examservice.dto.QuestionRequest;
import com.smartek.examservice.entity.Exam;
import com.smartek.examservice.entity.Question;
import com.smartek.examservice.repository.ExamRepository;
import com.smartek.examservice.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionService - Tests unitaires")
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private ExamRepository examRepository;

    @InjectMocks
    private QuestionService questionService;

    private Exam exam;
    private Question question;
    private QuestionRequest request;

    @BeforeEach
    void setUp() {
        exam = new Exam();
        exam.setId(1L);
        exam.setTitle("Quiz Spring Boot");
        exam.setExamType("QUIZ");
        exam.setDuration(60);
        exam.setPassingScore(70);
        exam.setTotalMarks(100);
        exam.setIsActive(true);
        exam.setQuestions(new ArrayList<>());

        question = new Question();
        question.setId(1L);
        question.setExam(exam);
        question.setQuestionText("Qu'est-ce que Spring Boot ?");
        question.setQuestionType("MULTIPLE_CHOICE");
        question.setMarks(10);
        question.setOptions(new ArrayList<>());

        request = new QuestionRequest();
        request.setExamId(1L);
        request.setQuestionText("Qu'est-ce que Spring Boot ?");
        request.setQuestionType("MULTIPLE_CHOICE");
        request.setMarks(10);
        request.setCorrectAnswer("A");
    }

    @Nested
    @DisplayName("createQuestion()")
    class CreateQuestion {

        @Test
        @DisplayName("Doit créer une question avec succès")
        void shouldCreateQuestion() {
            when(examRepository.findById(1L)).thenReturn(Optional.of(exam));
            when(questionRepository.save(any(Question.class))).thenReturn(question);

            Question result = questionService.createQuestion(request);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(questionRepository, times(1)).save(any(Question.class));
        }

        @Test
        @DisplayName("Doit lever une exception si l'examen n'existe pas")
        void shouldThrowWhenExamNotFound() {
            when(examRepository.findById(99L)).thenReturn(Optional.empty());
            request.setExamId(99L);

            assertThatThrownBy(() -> questionService.createQuestion(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Exam not found");
        }
    }

    @Nested
    @DisplayName("getQuestionsByExam()")
    class GetQuestionsByExam {

        @Test
        @DisplayName("Doit retourner les questions d'un examen")
        void shouldReturnQuestions() {
            when(questionRepository.findByExamId(1L)).thenReturn(List.of(question));

            List<Question> result = questionService.getQuestionsByExam(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getQuestionText()).isEqualTo("Qu'est-ce que Spring Boot ?");
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune question")
        void shouldReturnEmptyList() {
            when(questionRepository.findByExamId(99L)).thenReturn(List.of());

            List<Question> result = questionService.getQuestionsByExam(99L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateQuestion()")
    class UpdateQuestion {

        @Test
        @DisplayName("Doit mettre à jour une question existante")
        void shouldUpdateQuestion() {
            when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
            when(questionRepository.save(any(Question.class))).thenReturn(question);

            Question result = questionService.updateQuestion(1L, request);

            assertThat(result).isNotNull();
            verify(questionRepository, times(1)).save(any(Question.class));
        }

        @Test
        @DisplayName("Doit lever une exception si la question n'existe pas")
        void shouldThrowWhenNotFound() {
            when(questionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> questionService.updateQuestion(99L, request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Question not found");
        }
    }

    @Nested
    @DisplayName("deleteQuestion()")
    class DeleteQuestion {

        @Test
        @DisplayName("Doit supprimer une question")
        void shouldDeleteQuestion() {
            doNothing().when(questionRepository).deleteById(1L);

            questionService.deleteQuestion(1L);

            verify(questionRepository, times(1)).deleteById(1L);
        }
    }
}
