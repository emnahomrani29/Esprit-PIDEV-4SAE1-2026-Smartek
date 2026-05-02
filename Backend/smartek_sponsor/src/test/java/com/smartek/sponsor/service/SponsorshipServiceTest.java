package com.smartek.sponsor.service;

import com.smartek.sponsor.entity.*;
import com.smartek.sponsor.exception.*;
import com.smartek.sponsor.repository.ContractRepository;
import com.smartek.sponsor.repository.SponsorshipRepository;
import com.smartek.sponsor.service.impl.SponsorshipServiceImpl;
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
@DisplayName("SponsorshipService - Tests unitaires (logique métier riche)")
class SponsorshipServiceTest {

    @Mock private SponsorshipRepository sponsorshipRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private BudgetService budgetService;
    @Mock private EmailService emailService;

    @InjectMocks private SponsorshipServiceImpl sponsorshipService;

    private Contract activeContract;
    private Sponsorship sampleSponsorship;

    @BeforeEach
    void setUp() {
        activeContract = new Contract();
        activeContract.setId(1L);
        activeContract.setContractNumber("CTR-2026-001");
        activeContract.setStartDate(LocalDate.of(2026, 1, 1));
        activeContract.setEndDate(LocalDate.of(2026, 12, 31));
        activeContract.setAmount(10000.0);
        activeContract.setStatus(ContractStatus.ACTIVE);

        sampleSponsorship = new Sponsorship();
        sampleSponsorship.setId(1L);
        sampleSponsorship.setSponsorshipType(SponsorshipType.EVENT);
        sampleSponsorship.setAmountAllocated(600.0);
        sampleSponsorship.setStartDate(LocalDate.of(2026, 3, 1));
        sampleSponsorship.setEndDate(LocalDate.of(2026, 6, 30));
        sampleSponsorship.setVisibilityLevel(VisibilityLevel.LOGO);
        sampleSponsorship.setTargetType(TargetType.EVENT);
        sampleSponsorship.setTargetId(10L);
        sampleSponsorship.setStatus(SponsorshipStatus.PENDING);
        sampleSponsorship.setContract(activeContract);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createSponsorship - logique métier riche (6 validations)
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createSponsorship() - Validations métier")
    class CreateSponsorship {

        @Test
        @DisplayName("Doit créer un sponsoring avec succès quand toutes les validations passent")
        void shouldCreateSponsorshipSuccessfully() {
            when(contractRepository.findById(1L)).thenReturn(Optional.of(activeContract));
            when(budgetService.getAvailableBudget(1L)).thenReturn(5000.0);
            when(sponsorshipRepository.findOverlappingSponsorships(any(), any(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(sponsorshipRepository.save(any(Sponsorship.class))).thenReturn(sampleSponsorship);
            doNothing().when(emailService).notifyAdminNewSponsorship(any());

            Sponsorship result = sponsorshipService.createSponsorship(1L, sampleSponsorship);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(SponsorshipStatus.PENDING);
            verify(sponsorshipRepository).save(any(Sponsorship.class));
            verify(emailService).notifyAdminNewSponsorship(any());
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si le contrat n'existe pas")
        void shouldThrowWhenContractNotFound() {
            when(contractRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sponsorshipService.createSponsorship(99L, sampleSponsorship))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(sponsorshipRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit lever InvalidContractStateException si le contrat n'est pas ACTIVE")
        void shouldThrowWhenContractNotActive() {
            activeContract.setStatus(ContractStatus.EXPIRED);
            when(contractRepository.findById(1L)).thenReturn(Optional.of(activeContract));

            assertThatThrownBy(() -> sponsorshipService.createSponsorship(1L, sampleSponsorship))
                    .isInstanceOf(InvalidContractStateException.class)
                    .hasMessageContaining("not active");

            verify(sponsorshipRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit lever InsufficientBudgetException si le budget est insuffisant")
        void shouldThrowWhenBudgetInsufficient() {
            when(contractRepository.findById(1L)).thenReturn(Optional.of(activeContract));
            when(budgetService.getAvailableBudget(1L)).thenReturn(100.0); // 100€ < 600€ demandés

            assertThatThrownBy(() -> sponsorshipService.createSponsorship(1L, sampleSponsorship))
                    .isInstanceOf(InsufficientBudgetException.class)
                    .hasMessageContaining("Insufficient budget");

            verify(sponsorshipRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit lever BusinessException si le montant est inférieur au minimum du niveau de visibilité")
        void shouldThrowWhenAmountBelowVisibilityMinimum() {
            sampleSponsorship.setAmountAllocated(200.0); // < 500€ minimum pour LOGO
            when(contractRepository.findById(1L)).thenReturn(Optional.of(activeContract));
            when(budgetService.getAvailableBudget(1L)).thenReturn(5000.0);

            assertThatThrownBy(() -> sponsorshipService.createSponsorship(1L, sampleSponsorship))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("minimum");

            verify(sponsorshipRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit lever InvalidDateRangeException si les dates dépassent la période du contrat")
        void shouldThrowWhenDatesOutsideContractPeriod() {
            sampleSponsorship.setStartDate(LocalDate.of(2025, 1, 1)); // avant le contrat
            when(contractRepository.findById(1L)).thenReturn(Optional.of(activeContract));
            when(budgetService.getAvailableBudget(1L)).thenReturn(5000.0);

            assertThatThrownBy(() -> sponsorshipService.createSponsorship(1L, sampleSponsorship))
                    .isInstanceOf(InvalidDateRangeException.class)
                    .hasMessageContaining("within contract period");

            verify(sponsorshipRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit lever InvalidDateRangeException si startDate >= endDate")
        void shouldThrowWhenStartDateNotBeforeEndDate() {
            sampleSponsorship.setStartDate(LocalDate.of(2026, 6, 30));
            sampleSponsorship.setEndDate(LocalDate.of(2026, 3, 1)); // end < start
            when(contractRepository.findById(1L)).thenReturn(Optional.of(activeContract));
            when(budgetService.getAvailableBudget(1L)).thenReturn(5000.0);

            assertThatThrownBy(() -> sponsorshipService.createSponsorship(1L, sampleSponsorship))
                    .isInstanceOf(InvalidDateRangeException.class)
                    .hasMessageContaining("Start date must be before end date");

            verify(sponsorshipRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit lever SponsorshipOverlapException si un chevauchement existe")
        void shouldThrowWhenOverlappingSponsorship() {
            when(contractRepository.findById(1L)).thenReturn(Optional.of(activeContract));
            when(budgetService.getAvailableBudget(1L)).thenReturn(5000.0);
            when(sponsorshipRepository.findOverlappingSponsorships(any(), any(), any(), any()))
                    .thenReturn(List.of(sampleSponsorship)); // chevauchement détecté

            assertThatThrownBy(() -> sponsorshipService.createSponsorship(1L, sampleSponsorship))
                    .isInstanceOf(SponsorshipOverlapException.class)
                    .hasMessageContaining("already has a sponsorship");

            verify(sponsorshipRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit notifier l'admin après création réussie")
        void shouldNotifyAdminAfterSuccessfulCreation() {
            when(contractRepository.findById(1L)).thenReturn(Optional.of(activeContract));
            when(budgetService.getAvailableBudget(1L)).thenReturn(5000.0);
            when(sponsorshipRepository.findOverlappingSponsorships(any(), any(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(sponsorshipRepository.save(any(Sponsorship.class))).thenReturn(sampleSponsorship);
            doNothing().when(emailService).notifyAdminNewSponsorship(any());

            sponsorshipService.createSponsorship(1L, sampleSponsorship);

            verify(emailService, times(1)).notifyAdminNewSponsorship(sampleSponsorship);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getSponsorshipById
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getSponsorshipById()")
    class GetSponsorshipById {

        @Test
        @DisplayName("Doit retourner le sponsoring par ID")
        void shouldReturnSponsorshipById() {
            when(sponsorshipRepository.findById(1L)).thenReturn(Optional.of(sampleSponsorship));

            Sponsorship result = sponsorshipService.getSponsorshipById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si le sponsoring n'existe pas")
        void shouldThrowWhenNotFound() {
            when(sponsorshipRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sponsorshipService.getSponsorshipById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateSponsorship - contraintes de statut
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateSponsorship() - Contraintes de statut")
    class UpdateSponsorship {

        @Test
        @DisplayName("Doit mettre à jour un sponsoring PENDING")
        void shouldUpdatePendingSponsorship() {
            when(sponsorshipRepository.findById(1L)).thenReturn(Optional.of(sampleSponsorship));
            when(sponsorshipRepository.save(any(Sponsorship.class))).thenReturn(sampleSponsorship);

            Sponsorship result = sponsorshipService.updateSponsorship(1L, null, sampleSponsorship);

            assertThat(result).isNotNull();
            verify(sponsorshipRepository).save(any(Sponsorship.class));
        }

        @Test
        @DisplayName("Doit remettre à PENDING un sponsoring REJECTED lors de la mise à jour")
        void shouldResetToPendingWhenUpdatingRejected() {
            sampleSponsorship.setStatus(SponsorshipStatus.REJECTED);
            sampleSponsorship.setRejectionReason("Montant insuffisant");
            when(sponsorshipRepository.findById(1L)).thenReturn(Optional.of(sampleSponsorship));
            when(sponsorshipRepository.save(any(Sponsorship.class))).thenAnswer(inv -> inv.getArgument(0));

            Sponsorship result = sponsorshipService.updateSponsorship(1L, null, sampleSponsorship);

            assertThat(result.getStatus()).isEqualTo(SponsorshipStatus.PENDING);
            assertThat(result.getRejectionReason()).isNull();
        }

        @Test
        @DisplayName("Doit lever BusinessException si le sponsoring est APPROVED")
        void shouldThrowWhenUpdatingApprovedSponsorship() {
            sampleSponsorship.setStatus(SponsorshipStatus.APPROVED);
            when(sponsorshipRepository.findById(1L)).thenReturn(Optional.of(sampleSponsorship));

            assertThatThrownBy(() -> sponsorshipService.updateSponsorship(1L, null, sampleSponsorship))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot update sponsorship");

            verify(sponsorshipRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit lever BusinessException si le sponsoring est COMPLETED")
        void shouldThrowWhenUpdatingCompletedSponsorship() {
            sampleSponsorship.setStatus(SponsorshipStatus.COMPLETED);
            when(sponsorshipRepository.findById(1L)).thenReturn(Optional.of(sampleSponsorship));

            assertThatThrownBy(() -> sponsorshipService.updateSponsorship(1L, null, sampleSponsorship))
                    .isInstanceOf(BusinessException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteSponsorship - contraintes de statut
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deleteSponsorship() - Contraintes de statut")
    class DeleteSponsorship {

        @Test
        @DisplayName("Doit supprimer un sponsoring PENDING")
        void shouldDeletePendingSponsorship() {
            when(sponsorshipRepository.findById(1L)).thenReturn(Optional.of(sampleSponsorship));

            sponsorshipService.deleteSponsorship(1L);

            verify(sponsorshipRepository).delete(sampleSponsorship);
        }

        @Test
        @DisplayName("Doit supprimer un sponsoring REJECTED")
        void shouldDeleteRejectedSponsorship() {
            sampleSponsorship.setStatus(SponsorshipStatus.REJECTED);
            when(sponsorshipRepository.findById(1L)).thenReturn(Optional.of(sampleSponsorship));

            sponsorshipService.deleteSponsorship(1L);

            verify(sponsorshipRepository).delete(sampleSponsorship);
        }

        @Test
        @DisplayName("Doit lever BusinessException si le sponsoring est APPROVED")
        void shouldThrowWhenDeletingApprovedSponsorship() {
            sampleSponsorship.setStatus(SponsorshipStatus.APPROVED);
            when(sponsorshipRepository.findById(1L)).thenReturn(Optional.of(sampleSponsorship));

            assertThatThrownBy(() -> sponsorshipService.deleteSponsorship(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot delete sponsorship");

            verify(sponsorshipRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Doit lever BusinessException si le sponsoring est COMPLETED")
        void shouldThrowWhenDeletingCompletedSponsorship() {
            sampleSponsorship.setStatus(SponsorshipStatus.COMPLETED);
            when(sponsorshipRepository.findById(1L)).thenReturn(Optional.of(sampleSponsorship));

            assertThatThrownBy(() -> sponsorshipService.deleteSponsorship(1L))
                    .isInstanceOf(BusinessException.class);

            verify(sponsorshipRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si le sponsoring n'existe pas")
        void shouldThrowWhenNotFound() {
            when(sponsorshipRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sponsorshipService.deleteSponsorship(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
