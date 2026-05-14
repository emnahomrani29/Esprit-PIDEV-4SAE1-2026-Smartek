package com.smartek.sponsor.service;

import com.smartek.sponsor.dto.SponsorDashboardDTO;
import com.smartek.sponsor.entity.Contract;
import com.smartek.sponsor.entity.Sponsor;
import com.smartek.sponsor.entity.SponsorStatus;
import com.smartek.sponsor.entity.Sponsorship;
import com.smartek.sponsor.exception.ResourceNotFoundException;
import com.smartek.sponsor.repository.ContractRepository;
import com.smartek.sponsor.repository.SponsorRepository;
import com.smartek.sponsor.repository.SponsorshipRepository;
import com.smartek.sponsor.service.impl.SponsorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SponsorService - Tests unitaires")
class SponsorServiceTest {

    @Mock private SponsorRepository sponsorRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private SponsorshipRepository sponsorshipRepository;
    @Mock private RestTemplate restTemplate;

    @InjectMocks private SponsorServiceImpl sponsorService;

    private Sponsor sampleSponsor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sponsorService, "authServiceUrl", "http://localhost:8081");

        sampleSponsor = new Sponsor();
        sampleSponsor.setId(1L);
        sampleSponsor.setName("TechCorp");
        sampleSponsor.setEmail("techcorp@example.com");
        sampleSponsor.setPhone("0612345678");
        sampleSponsor.setCompanyName("TechCorp SA");
        sampleSponsor.setIndustry("Technology");
        sampleSponsor.setWebsite("https://techcorp.com");
        sampleSponsor.setStatus(SponsorStatus.ACTIVE);
        sampleSponsor.setPassword("password123");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createSponsor
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createSponsor")
    class CreateSponsor {

        @Test
        @DisplayName("Création réussie - enregistre dans auth-service et sauvegarde")
        void createSponsor_success() {
            when(restTemplate.postForEntity(anyString(), any(), eq(Object.class)))
                    .thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());
            when(sponsorRepository.save(any(Sponsor.class))).thenReturn(sampleSponsor);

            Sponsor result = sponsorService.createSponsor(sampleSponsor);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("TechCorp");
            verify(sponsorRepository).save(sampleSponsor);
        }

        @Test
        @DisplayName("Échec auth-service - lève RuntimeException")
        void createSponsor_authServiceFails_throwsException() {
            when(restTemplate.postForEntity(anyString(), any(), eq(Object.class)))
                    .thenThrow(new RuntimeException("Connection refused"));

            assertThatThrownBy(() -> sponsorService.createSponsor(sampleSponsor))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to create sponsor account");

            verify(sponsorRepository, never()).save(any());
        }

        @Test
        @DisplayName("Auth-service retourne erreur - lève RuntimeException")
        void createSponsor_authServiceError_throwsException() {
            when(restTemplate.postForEntity(anyString(), any(), eq(Object.class)))
                    .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());

            assertThatThrownBy(() -> sponsorService.createSponsor(sampleSponsor))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to create sponsor account");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllSponsors
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAllSponsors")
    class GetAllSponsors {

        @Test
        @DisplayName("Retourne la liste de tous les sponsors")
        void getAllSponsors_returnsList() {
            Sponsor sponsor2 = new Sponsor();
            sponsor2.setId(2L);
            sponsor2.setName("MediaGroup");
            when(sponsorRepository.findAll()).thenReturn(List.of(sampleSponsor, sponsor2));

            List<Sponsor> result = sponsorService.getAllSponsors();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Sponsor::getName)
                    .containsExactly("TechCorp", "MediaGroup");
        }

        @Test
        @DisplayName("Aucun sponsor - retourne liste vide")
        void getAllSponsors_empty_returnsEmptyList() {
            when(sponsorRepository.findAll()).thenReturn(List.of());

            List<Sponsor> result = sponsorService.getAllSponsors();

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getSponsorById
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getSponsorById")
    class GetSponsorById {

        @Test
        @DisplayName("Sponsor trouvé - retourne le sponsor")
        void getSponsorById_found_returnsSponsor() {
            when(sponsorRepository.findById(1L)).thenReturn(Optional.of(sampleSponsor));

            Sponsor result = sponsorService.getSponsorById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("TechCorp");
        }

        @Test
        @DisplayName("Sponsor non trouvé - lève ResourceNotFoundException")
        void getSponsorById_notFound_throwsException() {
            when(sponsorRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sponsorService.getSponsorById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getSponsorByEmail
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getSponsorByEmail")
    class GetSponsorByEmail {

        @Test
        @DisplayName("Email trouvé - retourne le sponsor")
        void getSponsorByEmail_found_returnsSponsor() {
            when(sponsorRepository.findByEmail("techcorp@example.com"))
                    .thenReturn(Optional.of(sampleSponsor));

            Sponsor result = sponsorService.getSponsorByEmail("techcorp@example.com");

            assertThat(result.getEmail()).isEqualTo("techcorp@example.com");
        }

        @Test
        @DisplayName("Email non trouvé - lève ResourceNotFoundException")
        void getSponsorByEmail_notFound_throwsException() {
            when(sponsorRepository.findByEmail("unknown@example.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sponsorService.getSponsorByEmail("unknown@example.com"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateSponsor
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateSponsor")
    class UpdateSponsor {

        @Test
        @DisplayName("Mise à jour réussie - retourne le sponsor modifié")
        void updateSponsor_success() {
            Sponsor updated = new Sponsor();
            updated.setName("TechCorp Updated");
            updated.setEmail("updated@example.com");
            updated.setPhone("0699999999");
            updated.setCompanyName("TechCorp Updated SA");
            updated.setIndustry("IT");
            updated.setWebsite("https://updated.com");
            updated.setStatus(SponsorStatus.ACTIVE);

            when(sponsorRepository.findById(1L)).thenReturn(Optional.of(sampleSponsor));
            when(sponsorRepository.save(any(Sponsor.class))).thenAnswer(inv -> inv.getArgument(0));

            Sponsor result = sponsorService.updateSponsor(1L, updated);

            assertThat(result.getName()).isEqualTo("TechCorp Updated");
            assertThat(result.getEmail()).isEqualTo("updated@example.com");
            verify(sponsorRepository).save(any(Sponsor.class));
        }

        @Test
        @DisplayName("Sponsor non trouvé - lève ResourceNotFoundException")
        void updateSponsor_notFound_throwsException() {
            when(sponsorRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sponsorService.updateSponsor(99L, sampleSponsor))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteSponsor
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deleteSponsor")
    class DeleteSponsor {

        @Test
        @DisplayName("Suppression réussie - appelle delete sur le repository")
        void deleteSponsor_success() {
            when(sponsorRepository.findById(1L)).thenReturn(Optional.of(sampleSponsor));

            sponsorService.deleteSponsor(1L);

            verify(sponsorRepository).delete(sampleSponsor);
        }

        @Test
        @DisplayName("Sponsor non trouvé - lève ResourceNotFoundException")
        void deleteSponsor_notFound_throwsException() {
            when(sponsorRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sponsorService.deleteSponsor(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(sponsorRepository, never()).delete(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getSponsorDashboard
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getSponsorDashboard")
    class GetSponsorDashboard {

        @Test
        @DisplayName("Dashboard calculé correctement avec contrats et sponsorships")
        void getSponsorDashboard_success() {
            Contract contract = new Contract();
            contract.setId(1L);
            contract.setAmount(10000.0);

            Sponsorship sponsorship = new Sponsorship();
            sponsorship.setAmountAllocated(3000.0);

            when(sponsorRepository.findById(1L)).thenReturn(Optional.of(sampleSponsor));
            when(contractRepository.findBySponsorId(1L)).thenReturn(List.of(contract));
            when(sponsorshipRepository.findByContractSponsorId(1L)).thenReturn(List.of(sponsorship));

            SponsorDashboardDTO dashboard = sponsorService.getSponsorDashboard(1L);

            assertThat(dashboard).isNotNull();
            assertThat(dashboard.getTotalContractAmount()).isEqualTo(10000.0);
            assertThat(dashboard.getTotalSpent()).isEqualTo(3000.0);
            assertThat(dashboard.getRemainingBalance()).isEqualTo(7000.0);
        }

        @Test
        @DisplayName("Dashboard sans contrats - montants à zéro")
        void getSponsorDashboard_noContracts_zeroAmounts() {
            when(sponsorRepository.findById(1L)).thenReturn(Optional.of(sampleSponsor));
            when(contractRepository.findBySponsorId(1L)).thenReturn(List.of());
            when(sponsorshipRepository.findByContractSponsorId(1L)).thenReturn(List.of());

            SponsorDashboardDTO dashboard = sponsorService.getSponsorDashboard(1L);

            assertThat(dashboard.getTotalContractAmount()).isEqualTo(0.0);
            assertThat(dashboard.getTotalSpent()).isEqualTo(0.0);
            assertThat(dashboard.getRemainingBalance()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Sponsor non trouvé - lève ResourceNotFoundException")
        void getSponsorDashboard_notFound_throwsException() {
            when(sponsorRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sponsorService.getSponsorDashboard(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
