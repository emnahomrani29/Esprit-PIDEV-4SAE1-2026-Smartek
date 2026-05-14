package com.smartek.courseservice.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Exceptions - course-service")
class ExceptionTest {

    @Nested
    @DisplayName("ResourceNotFoundException")
    class ResourceNotFoundExceptionTest {

        @Test
        @DisplayName("Constructeur avec message simple")
        void simpleMessage() {
            ResourceNotFoundException ex = new ResourceNotFoundException("Cours non trouvé");
            assertThat(ex.getMessage()).isEqualTo("Cours non trouvé");
        }

        @Test
        @DisplayName("Constructeur avec resourceName, fieldName, fieldValue")
        void formattedMessage() {
            ResourceNotFoundException ex = new ResourceNotFoundException("Cours", "id", 42L);
            assertThat(ex.getMessage()).contains("Cours").contains("id").contains("42");
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
            DuplicateResourceException ex = new DuplicateResourceException("Titre déjà utilisé");
            assertThat(ex.getMessage()).isEqualTo("Titre déjà utilisé");
        }

        @Test
        @DisplayName("Constructeur avec resourceName, fieldName, fieldValue")
        void formattedMessage() {
            DuplicateResourceException ex = new DuplicateResourceException("Cours", "titre", "Spring Boot");
            assertThat(ex.getMessage()).contains("Cours").contains("titre").contains("Spring Boot");
        }

        @Test
        @DisplayName("Est une RuntimeException")
        void isRuntimeException() {
            assertThat(new DuplicateResourceException("test"))
                    .isInstanceOf(RuntimeException.class);
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
        }

        @Test
        @DisplayName("Est une RuntimeException")
        void isRuntimeException() {
            assertThat(new BadRequestException("test"))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
