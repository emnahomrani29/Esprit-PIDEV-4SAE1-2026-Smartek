package com.smartek.sponsor.service;

import com.smartek.sponsor.entity.Contract;
import com.smartek.sponsor.entity.ContractStatus;
import com.smartek.sponsor.entity.ContractType;
import com.smartek.sponsor.entity.Sponsor;
import com.smartek.sponsor.entity.SponsorStatus;
import com.smartek.sponsor.exception.ResourceNotFoundException;
import com.smartek.sponsor.repository.ContractRepository;
import com.smartek.sponsor.repository.SponsorRepository;
import com.smartek.sponsor.service.impl.ContractServiceImpl;
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
@DisplayName("ContractService - Tests unitaires")
class ContractServiceTest {

    @Mock private ContractRepository contractRepository;
    @Mock private SponsorRepository sponsorRepository;

    @InjectMocks private ContractServiceImpl contractService;

    private Sponsor sampleSponsor;
    private Contract sampleContract;

    @BeforeEach
    void setUp() {
        sampleSponsor = new Sponsor();
        sampleSponsor.setId(1L);
        sampleSponsor.setName("TechCorp");
        sampleSponsor.setEmail("techcorp@example.com");
        sampleSponsor.setStatus(SponsorStatus.ACTIVE);

        sampleContract = new Contract();
        sampleContract.setId(1L);
        sampleContract.setContractNumber("CTR-2026-001");
        sampleContract.setStartDate(LocalDate.of(2026, 1, 1));
        sampleContract.setEndDate(LocalDate.of(2026, 12, 31));
        sampleContract.setAmount(10000.0);
        sampleContract.setCurrency("EUR");
        sampleContract.setStatus(ContractStatus.ACTIVE);
        sampleContract.setType(ContractType.GLOBAL);
        sampleContract.setSponsor(sampleSponsor);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createContract
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createContract()")
    class CreateContract {

        @Test
        @DisplayName("Doit créer un contrat avec succès")
        void shouldCreateContractSuccessfully() {
            when(sponsorRepository.findById(1L)).thenReturn(Optional.of(sampleSponsor));
            when(contractRepository.save(any(Contract.class))).thenReturn(sampleContract);

            Contract result = contractService.createContract(1L, sampleContract);

            assertThat(result).isNotNull();
            assertThat(result.getContractNumber()).isEqualTo("CTR-2026-001");
            assertThat(result.getSponsor()).isEqualTo(sampleSponsor);
            verify(contractRepository).save(any(Contract.class));
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si le sponsor n'existe pas")
        void shouldThrowWhenSponsorNotFound() {
            when(sponsorRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.createContract(99L, sampleContract))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(contractRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit forcer l'ID à null pour éviter les conflits")
        void shouldResetIdBeforeSave() {
            sampleContract.setId(999L); // ID existant
            when(sponsorRepository.findById(1L)).thenReturn(Optional.of(sampleSponsor));
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> {
                Contract c = inv.getArgument(0);
                assertThat(c.getId()).isNull(); // ID doit être null
                return sampleContract;
            });

            contractService.createContract(1L, sampleContract);

            verify(contractRepository).save(any(Contract.class));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllContracts
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAllContracts()")
    class GetAllContracts {

        @Test
        @DisplayName("Doit retourner tous les contrats")
        void shouldReturnAllContracts() {
            when(contractRepository.findAll()).thenReturn(List.of(sampleContract));

            List<Contract> result = contractService.getAllContracts();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getContractNumber()).isEqualTo("CTR-2026-001");
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun contrat")
        void shouldReturnEmptyListWhenNoContracts() {
            when(contractRepository.findAll()).thenReturn(Collections.emptyList());

            List<Contract> result = contractService.getAllContracts();

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getContractById
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getContractById()")
    class GetContractById {

        @Test
        @DisplayName("Doit retourner le contrat par ID")
        void shouldReturnContractById() {
            when(contractRepository.findById(1L)).thenReturn(Optional.of(sampleContract));

            Contract result = contractService.getContractById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getAmount()).isEqualTo(10000.0);
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si le contrat n'existe pas")
        void shouldThrowWhenContractNotFound() {
            when(contractRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.getContractById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateContract
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateContract()")
    class UpdateContract {

        @Test
        @DisplayName("Doit mettre à jour un contrat existant")
        void shouldUpdateContractSuccessfully() {
            Contract updated = new Contract();
            updated.setContractNumber("CTR-2026-002");
            updated.setStartDate(LocalDate.of(2026, 2, 1));
            updated.setEndDate(LocalDate.of(2026, 12, 31));
            updated.setAmount(15000.0);
            updated.setCurrency("EUR");
            updated.setStatus(ContractStatus.ACTIVE);
            updated.setType(ContractType.GLOBAL);

            when(contractRepository.findById(1L)).thenReturn(Optional.of(sampleContract));
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));

            Contract result = contractService.updateContract(1L, null, updated);

            assertThat(result.getContractNumber()).isEqualTo("CTR-2026-002");
            assertThat(result.getAmount()).isEqualTo(15000.0);
            verify(contractRepository).save(any(Contract.class));
        }

        @Test
        @DisplayName("Doit mettre à jour le sponsor si sponsorId fourni")
        void shouldUpdateSponsorWhenSponsorIdProvided() {
            Sponsor newSponsor = new Sponsor();
            newSponsor.setId(2L);
            newSponsor.setName("MediaGroup");

            when(contractRepository.findById(1L)).thenReturn(Optional.of(sampleContract));
            when(sponsorRepository.findById(2L)).thenReturn(Optional.of(newSponsor));
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));

            Contract result = contractService.updateContract(1L, 2L, sampleContract);

            assertThat(result.getSponsor().getName()).isEqualTo("MediaGroup");
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si le contrat n'existe pas")
        void shouldThrowWhenContractNotFound() {
            when(contractRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.updateContract(99L, null, sampleContract))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteContract
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deleteContract()")
    class DeleteContract {

        @Test
        @DisplayName("Doit supprimer un contrat existant")
        void shouldDeleteContractSuccessfully() {
            when(contractRepository.findById(1L)).thenReturn(Optional.of(sampleContract));

            contractService.deleteContract(1L);

            verify(contractRepository).delete(sampleContract);
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si le contrat n'existe pas")
        void shouldThrowWhenContractNotFound() {
            when(contractRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contractService.deleteContract(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(contractRepository, never()).delete(any());
        }
    }
}
