package com.smartek.certificationbadgeservice.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Exceptions - certification-badge-service")
class ExceptionTest {

    @Nested
    @DisplayName("DuplicateAwardException")
    class DuplicateAwardExceptionTest {

        @Test
        @DisplayName("Constructeur avec message")
        void withMessage() {
            DuplicateAwardException ex = new DuplicateAwardException("Badge already awarded to this learner");
            assertThat(ex.getMessage()).isEqualTo("Badge already awarded to this learner");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Constructeur avec message et cause")
        void withMessageAndCause() {
            Throwable cause = new RuntimeException("cause");
            DuplicateAwardException ex = new DuplicateAwardException("Duplicate", cause);
            assertThat(ex.getMessage()).isEqualTo("Duplicate");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("ResourceNotFoundException")
    class ResourceNotFoundExceptionTest {

        @Test
        @DisplayName("Constructeur avec message")
        void withMessage() {
            ResourceNotFoundException ex = new ResourceNotFoundException("Badge template not found with id: 99");
            assertThat(ex.getMessage()).contains("99");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Constructeur avec message et cause")
        void withMessageAndCause() {
            Throwable cause = new RuntimeException("cause");
            ResourceNotFoundException ex = new ResourceNotFoundException("Not found", cause);
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("ValidationException")
    class ValidationExceptionTest {

        @Test
        @DisplayName("Constructeur avec message simple")
        void withMessage() {
            ValidationException ex = new ValidationException("Expiry date cannot be before issue date");
            assertThat(ex.getMessage()).isEqualTo("Expiry date cannot be before issue date");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("getErrors() retourne la liste des erreurs")
        void getErrors_returnsList() {
            ValidationException ex = new ValidationException("Validation failed");
            assertThat(ex.getErrors()).isNotNull();
        }
    }

    @Nested
    @DisplayName("ServiceException")
    class ServiceExceptionTest {

        @Test
        @DisplayName("Constructeur avec message")
        void withMessage() {
            ServiceException ex = new ServiceException("Service error");
            assertThat(ex.getMessage()).isEqualTo("Service error");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }
}
