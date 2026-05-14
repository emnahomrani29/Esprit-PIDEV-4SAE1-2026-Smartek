package com.smartek.sponsor.service;

import com.smartek.sponsor.dto.BudgetSummaryDTO;
import com.smartek.sponsor.entity.Contract;
import com.smartek.sponsor.entity.ContractStatus;
import com.smartek.sponsor.entity.Sponsorship;
import com.smartek.sponsor.entity.SponsorshipStatus;
import com.smartek.sponsor.exception.ResourceNotFoundException;
import com.smartek.sponsor.repository.ContractRepository;
import com.smartek.sponsor.repository.SponsorshipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BudgetService - Tests unitaires (calculs financiers)")
class BudgetServiceTest {

    @Mock private ContractRepository contractRepository;
    @Mock private SponsorshipRepository sponsorshipRepository;

    @InjectMocks private BudgetService budgetService;

    private Contract sampleContract;
    private Sponsorship approvedSponsorship;
    private Sponsorship pendingSponsorship;

    @BeforeEach
    void setUp() {
        sampleContract = new Contract();
        sampleContract.setId(1L);
        sampleContract.setContractNumber("CTR-2026-001");
        sampleContract.setAmount(10000.0);
        sampleContract.setStatus(ContractStatus.ACTIVE);
        sampleContract.setStartDate(LocalDate.of(2026, 1, 1));
        sampleContract.setEndDate(LocalDate.of(2026, 12, 31));

        approvedSponsorship = new Sponsorship();
        approvedSponsorship.setId(1L);
        approvedSponsorship.setAmountAllocated(3000.0);
        approvedSponsorship.setStatus(SponsorshipStatus.APPROVED);

        pendingSponsorship = new Sponsorship();
        pendingSponsorship.setId(2L);
        pendingSponsorship.setAmountAllocated(1500.0);
        pendingSponsorship.setStatus(SponsorshipStatus.PENDING);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAvailableBudget
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAvailableBudget() - Calcul du budget disponible")
    class GetAvailableBudget {

        @Test
        @DisplayName("Doit calculer correctement : total - dépensé - réservé")
        void shouldCalculateAvailableBudgetCorrectly() {
            when(contractRepository.findById(1L)).thenReturn(Optional.of(sampleContract));
            when(sponsorshipRepository.findByContractIdAndStatusIn(eq(1L), anyList()))
                    .thenReturn(List.of(approvedSponsorship)); // 3000€ dépensés
            when(sponsorshipRepository.findByContractIdAndStatus(1L, SponsorshipStatus.PENDING))
                    .thenReturn(List.of(pendingSponsorship)); // 1500€ réservés

            Double available = budgetService.getAvailableBudget(1L);

            // 10000 - 3000 - 1500 = 5500
            assertThat(available).isEqualTo(5500.0);
        }

        @Test
        @DisplayName("Doit retourner le budget total si aucun sponsoring")
        void shouldReturnTotalBudgetWhenNoSponsorships() {
            when(contractRepository.findById(1L)).thenReturn(Optional.of(sampleContract));
            when(sponsorshipRepository.findByContractIdAndStatusIn(eq(1L), anyList()))
                    .thenReturn(Collections.emptyList());
            when(sponsorshipRepository.findByContractIdAndStatus(1L, SponsorshipStatus.PENDING))
                    .thenReturn(Collections.emptyList());

            Double available = budgetService.getAvailableBudget(1L);

            assertThat(available).isEqualTo(10000.0);
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si le contrat n'existe pas")
        void shouldThrowWhenContractNotFound() {
            when(contractRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> budgetService.getAvailableBudget(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // calculateSpent
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("calculateSpent() - Calcul des dépenses")
    class CalculateSpent {

        @Test
        @DisplayName("Doit sommer les montants APPROVED et COMPLETED")
        void shouldSumApprovedAndCompletedAmounts() {
            Sponsorship completed = new Sponsorship();
            completed.setAmountAllocated(2000.0);
            completed.setStatus(SponsorshipStatus.COMPLETED);

            when(sponsorshipRepository.findByContractIdAndStatusIn(eq(1L), anyList()))
                    .thenReturn(List.of(approvedSponsorship, completed));

            Double spent = budgetService.calculateSpent(1L);

            assertThat(spent).isEqualTo(5000.0); // 3000 + 2000
        }

        @Test
        @DisplayName("Doit retourner 0 si aucun sponsoring approuvé ou complété")
        void shouldReturnZeroWhenNoApprovedOrCompleted() {
            when(sponsorshipRepository.findByContractIdAndStatusIn(eq(1L), anyList()))
                    .thenReturn(Collections.emptyList());

            Double spent = budgetService.calculateSpent(1L);

            assertThat(spent).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Doit ignorer les montants null dans le calcul")
        void shouldIgnoreNullAmounts() {
            Sponsorship nullAmount = new Sponsorship();
            nullAmount.setAmountAllocated(null);
            nullAmount.setStatus(SponsorshipStatus.APPROVED);

            when(sponsorshipRepository.findByContractIdAndStatusIn(eq(1L), anyList()))
                    .thenReturn(List.of(nullAmount));

            Double spent = budgetService.calculateSpent(1L);

            assertThat(spent).isEqualTo(0.0);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // calculateReserved
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("calculateReserved() - Calcul des réservations")
    class CalculateReserved {

        @Test
        @DisplayName("Doit sommer les montants PENDING")
        void shouldSumPendingAmounts() {
            Sponsorship pending2 = new Sponsorship();
            pending2.setAmountAllocated(500.0);
            pending2.setStatus(SponsorshipStatus.PENDING);

            when(sponsorshipRepository.findByContractIdAndStatus(1L, SponsorshipStatus.PENDING))
                    .thenReturn(List.of(pendingSponsorship, pending2));

            Double reserved = budgetService.calculateReserved(1L);

            assertThat(reserved).isEqualTo(2000.0); // 1500 + 500
        }

        @Test
        @DisplayName("Doit retourner 0 si aucun sponsoring en attente")
        void shouldReturnZeroWhenNoPendingSponsorships() {
            when(sponsorshipRepository.findByContractIdAndStatus(1L, SponsorshipStatus.PENDING))
                    .thenReturn(Collections.emptyList());

            Double reserved = budgetService.calculateReserved(1L);

            assertThat(reserved).isEqualTo(0.0);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getBudgetSummary - calcul complet
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getBudgetSummary() - Résumé complet")
    class GetBudgetSummary {

        @Test
        @DisplayName("Doit retourner un résumé complet avec tous les champs calculés")
        void shouldReturnCompleteBudgetSummary() {
            when(contractRepository.findById(1L)).thenReturn(Optional.of(sampleContract));
            when(sponsorshipRepository.findByContractIdAndStatusIn(eq(1L), anyList()))
                    .thenReturn(List.of(approvedSponsorship)); // 3000€
            when(sponsorshipRepository.findByContractIdAndStatus(1L, SponsorshipStatus.PENDING))
                    .thenReturn(List.of(pendingSponsorship)); // 1500€

            BudgetSummaryDTO summary = budgetService.getBudgetSummary(1L);

            assertThat(summary.getTotalBudget()).isEqualTo(10000.0);
            assertThat(summary.getSpent()).isEqualTo(3000.0);
            assertThat(summary.getReserved()).isEqualTo(1500.0);
            assertThat(summary.getAvailable()).isEqualTo(5500.0);
            assertThat(summary.getUsagePercentage()).isEqualTo(45.0); // (3000+1500)/10000*100
            assertThat(summary.getWarningLevel()).isEqualTo("NORMAL");
        }

        @Test
        @DisplayName("Doit retourner WARNING si l'utilisation est entre 80% et 90%")
        void shouldReturnWarningLevelWhenUsageAbove80() {
            sampleContract.setAmount(1000.0);
            Sponsorship bigSponsorship = new Sponsorship();
            bigSponsorship.setAmountAllocated(850.0);
            bigSponsorship.setStatus(SponsorshipStatus.APPROVED);

            when(contractRepository.findById(1L)).thenReturn(Optional.of(sampleContract));
            when(sponsorshipRepository.findByContractIdAndStatusIn(eq(1L), anyList()))
                    .thenReturn(List.of(bigSponsorship)); // 85%
            when(sponsorshipRepository.findByContractIdAndStatus(1L, SponsorshipStatus.PENDING))
                    .thenReturn(Collections.emptyList());

            BudgetSummaryDTO summary = budgetService.getBudgetSummary(1L);

            assertThat(summary.getWarningLevel()).isEqualTo("WARNING");
        }

        @Test
        @DisplayName("Doit retourner CRITICAL si l'utilisation dépasse 90%")
        void shouldReturnCriticalLevelWhenUsageAbove90() {
            sampleContract.setAmount(1000.0);
            Sponsorship bigSponsorship = new Sponsorship();
            bigSponsorship.setAmountAllocated(950.0);
            bigSponsorship.setStatus(SponsorshipStatus.APPROVED);

            when(contractRepository.findById(1L)).thenReturn(Optional.of(sampleContract));
            when(sponsorshipRepository.findByContractIdAndStatusIn(eq(1L), anyList()))
                    .thenReturn(List.of(bigSponsorship)); // 95%
            when(sponsorshipRepository.findByContractIdAndStatus(1L, SponsorshipStatus.PENDING))
                    .thenReturn(Collections.emptyList());

            BudgetSummaryDTO summary = budgetService.getBudgetSummary(1L);

            assertThat(summary.getWarningLevel()).isEqualTo("CRITICAL");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getSponsorBudgetSummary
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getSponsorBudgetSummary() - Agrégation multi-contrats")
    class GetSponsorBudgetSummary {

        @Test
        @DisplayName("Doit retourner un résumé vide si le sponsor n'a aucun contrat")
        void shouldReturnEmptySummaryWhenNoContracts() {
            when(contractRepository.findBySponsorId(1L)).thenReturn(Collections.emptyList());

            BudgetSummaryDTO summary = budgetService.getSponsorBudgetSummary(1L);

            assertThat(summary.getTotalBudget()).isEqualTo(0.0);
            assertThat(summary.getSpent()).isEqualTo(0.0);
            assertThat(summary.getAvailable()).isEqualTo(0.0);
            assertThat(summary.getWarningLevel()).isEqualTo("NORMAL");
        }

        @Test
        @DisplayName("Doit agréger les budgets de plusieurs contrats")
        void shouldAggregateBudgetsFromMultipleContracts() {
            Contract contract2 = new Contract();
            contract2.setId(2L);
            contract2.setAmount(5000.0);
            contract2.setStatus(ContractStatus.ACTIVE);

            when(contractRepository.findBySponsorId(1L)).thenReturn(List.of(sampleContract, contract2));
            // Contrat 1 : 3000€ dépensés, 0 réservés
            when(sponsorshipRepository.findByContractIdAndStatusIn(eq(1L), anyList()))
                    .thenReturn(List.of(approvedSponsorship));
            when(sponsorshipRepository.findByContractIdAndStatus(1L, SponsorshipStatus.PENDING))
                    .thenReturn(Collections.emptyList());
            // Contrat 2 : 0€ dépensés, 0 réservés
            when(sponsorshipRepository.findByContractIdAndStatusIn(eq(2L), anyList()))
                    .thenReturn(Collections.emptyList());
            when(sponsorshipRepository.findByContractIdAndStatus(2L, SponsorshipStatus.PENDING))
                    .thenReturn(Collections.emptyList());

            BudgetSummaryDTO summary = budgetService.getSponsorBudgetSummary(1L);

            assertThat(summary.getTotalBudget()).isEqualTo(15000.0); // 10000 + 5000
            assertThat(summary.getSpent()).isEqualTo(3000.0);
            assertThat(summary.getAvailable()).isEqualTo(12000.0);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // isBudgetThresholdReached
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("isBudgetThresholdReached() - Seuil d'alerte")
    class IsBudgetThresholdReached {

        @Test
        @DisplayName("Doit retourner true si l'utilisation dépasse le seuil")
        void shouldReturnTrueWhenThresholdExceeded() {
            sampleContract.setAmount(1000.0);
            Sponsorship bigSponsorship = new Sponsorship();
            bigSponsorship.setAmountAllocated(800.0);
            bigSponsorship.setStatus(SponsorshipStatus.APPROVED);

            when(contractRepository.findById(1L)).thenReturn(Optional.of(sampleContract));
            when(sponsorshipRepository.findByContractIdAndStatusIn(eq(1L), anyList()))
                    .thenReturn(List.of(bigSponsorship)); // 80%
            when(sponsorshipRepository.findByContractIdAndStatus(1L, SponsorshipStatus.PENDING))
                    .thenReturn(Collections.emptyList());

            boolean result = budgetService.isBudgetThresholdReached(1L, 75);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Doit retourner false si l'utilisation est sous le seuil")
        void shouldReturnFalseWhenBelowThreshold() {
            when(contractRepository.findById(1L)).thenReturn(Optional.of(sampleContract));
            when(sponsorshipRepository.findByContractIdAndStatusIn(eq(1L), anyList()))
                    .thenReturn(Collections.emptyList()); // 0%
            when(sponsorshipRepository.findByContractIdAndStatus(1L, SponsorshipStatus.PENDING))
                    .thenReturn(Collections.emptyList());

            boolean result = budgetService.isBudgetThresholdReached(1L, 80);

            assertThat(result).isFalse();
        }
    }
}
