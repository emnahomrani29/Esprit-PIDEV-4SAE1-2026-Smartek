package com.smartek.examservice.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Exceptions - exam-service")
class ExceptionTest {

    @Nested
    @DisplayName("ResourceNotFoundException")
    class ResourceNotFoundExceptionTest {

        @Test
        @DisplayName("Constructeur avec message simple")
        void simpleMessage() {
            ResourceNotFoundException ex = new ResourceNotFoundException("Exam not found with id: 1");
            assertThat(ex.getMessage()).isEqualTo("Exam not found with id: 1");
        }

        @Test
        @DisplayName("Constructeur formaté resourceName/fieldName/fieldValue")
        void formattedMessage() {
            ResourceNotFoundException ex = new ResourceNotFoundException("Exam", "id", 42L);
            assertThat(ex.getMessage()).contains("Exam").contains("id").contains("42");
        }

        @Test
        @DisplayName("Est une RuntimeException")
        void isRuntimeException() {
            assertThat(new ResourceNotFoundException("test"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("DuplicateResourceException")
    class DuplicateResourceExceptionTest {

        @Test
        @DisplayName("Constructeur avec message simple")
        void simpleMessage() {
            DuplicateResourceException ex = new DuplicateResourceException("Exam déjà existant");
            assertThat(ex.getMessage()).isEqualTo("Exam déjà existant");
        }

        @Test
        @DisplayName("Constructeur formaté resourceName/fieldName/fieldValue")
        void formattedMessage() {
            DuplicateResourceException ex = new DuplicateResourceException("Exam", "titre", "Quiz Spring");
            assertThat(ex.getMessage()).contains("Exam").contains("titre").contains("Quiz Spring");
        }
    }

    @Nested
    @DisplayName("BadRequestException")
    class BadRequestExceptionTest {

        @Test
        @DisplayName("Constructeur avec message")
        void withMessage() {
            BadRequestException ex = new BadRequestException("Requête invalide");
            assertThat(ex.getMessage()).isEqualTo("Requête invalide");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }
}
