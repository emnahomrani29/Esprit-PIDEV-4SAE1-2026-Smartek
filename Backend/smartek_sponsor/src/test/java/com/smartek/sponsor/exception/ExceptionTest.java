package com.smartek.sponsor.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Exceptions - smartek_sponsor")
class ExceptionTest {

    @Nested
    @DisplayName("ResourceNotFoundException")
    class ResourceNotFoundExceptionTest {

        @Test
        @DisplayName("Message formaté avec resourceName/fieldName/fieldValue")
        void formattedMessage() {
            ResourceNotFoundException ex = new ResourceNotFoundException("Sponsor", "id", 99L);
            assertThat(ex.getMessage()).contains("Sponsor").contains("id").contains("99");
        }

        @Test
        @DisplayName("Est une RuntimeException")
        void isRuntimeException() {
            assertThat(new ResourceNotFoundException("Sponsor", "id", 1L))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("InsufficientBudgetException")
    class InsufficientBudgetExceptionTest {

        @Test
        @DisplayName("Constructeur avec message")
        void withMessage() {
            InsufficientBudgetException ex = new InsufficientBudgetException(
                    "Insufficient budget. Available: 100.00€, Requested: 600.00€");
            assertThat(ex.getMessage()).contains("Insufficient budget");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Constructeur avec message et cause")
        void withMessageAndCause() {
            Throwable cause = new RuntimeException("cause");
            InsufficientBudgetException ex = new InsufficientBudgetException("Budget error", cause);
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("InvalidContractStateException")
    class InvalidContractStateExceptionTest {

        @Test
        @DisplayName("Constructeur avec message")
        void withMessage() {
            InvalidContractStateException ex = new InvalidContractStateException(
                    "Contract is not active. Current status: EXPIRED");
            assertThat(ex.getMessage()).contains("not active");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("SponsorshipOverlapException")
    class SponsorshipOverlapExceptionTest {

        @Test
        @DisplayName("Constructeur avec message")
        void withMessage() {
            SponsorshipOverlapException ex = new SponsorshipOverlapException(
                    "Target EVENT #10 already has a sponsorship during this period");
            assertThat(ex.getMessage()).contains("already has a sponsorship");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("BusinessException")
    class BusinessExceptionTest {

        @Test
        @DisplayName("Constructeur avec message")
        void withMessage() {
            BusinessException ex = new BusinessException("Cannot delete sponsorship with status: APPROVED");
            assertThat(ex.getMessage()).contains("Cannot delete");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Constructeur avec message et cause")
        void withMessageAndCause() {
            Throwable cause = new RuntimeException("root cause");
            BusinessException ex = new BusinessException("Business error", cause);
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("InvalidDateRangeException")
    class InvalidDateRangeExceptionTest {

        @Test
        @DisplayName("Constructeur avec message")
        void withMessage() {
            InvalidDateRangeException ex = new InvalidDateRangeException("Start date must be before end date");
            assertThat(ex.getMessage()).isEqualTo("Start date must be before end date");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }
}
