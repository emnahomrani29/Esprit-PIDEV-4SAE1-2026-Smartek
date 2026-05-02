package com.smartek.trainingservice.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Exceptions - training-service")
class ExceptionTest {

    @Nested
    @DisplayName("ResourceNotFoundException")
    class ResourceNotFoundExceptionTest {

        @Test
        @DisplayName("Constructeur avec message simple")
        void simpleMessage() {
            ResourceNotFoundException ex = new ResourceNotFoundException("Formation non trouvée avec l'ID: 99");
            assertThat(ex.getMessage()).contains("99");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Constructeur formaté resourceName/fieldName/fieldValue")
        void formattedMessage() {
            ResourceNotFoundException ex = new ResourceNotFoundException("Formation", "id", 42L);
            assertThat(ex.getMessage()).contains("Formation").contains("id").contains("42");
        }
    }

    @Nested
    @DisplayName("DuplicateResourceException")
    class DuplicateResourceExceptionTest {

        @Test
        @DisplayName("Constructeur avec message simple")
        void simpleMessage() {
            DuplicateResourceException ex = new DuplicateResourceException("Formation déjà existante");
            assertThat(ex.getMessage()).isEqualTo("Formation déjà existante");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Constructeur formaté")
        void formattedMessage() {
            DuplicateResourceException ex = new DuplicateResourceException("Formation", "titre", "Spring Boot");
            assertThat(ex.getMessage()).contains("Formation").contains("titre").contains("Spring Boot");
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
