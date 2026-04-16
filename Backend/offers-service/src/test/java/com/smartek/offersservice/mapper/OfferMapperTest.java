package com.smartek.offersservice.mapper;

import com.smartek.offersservice.dto.OfferRequest;
import com.smartek.offersservice.dto.OfferResponse;
import com.smartek.offersservice.entity.Offer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour OfferMapper.
 * Vérifie la conversion correcte entre entités et DTOs.
 */
@DisplayName("OfferMapper — Tests unitaires")
class OfferMapperTest {

    private OfferMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OfferMapper();
    }

    @Test
    @DisplayName("toEntity() mappe tous les champs correctement")
    void toEntity_shouldMapAllFields() {
        OfferRequest request = OfferRequest.builder()
                .title("Dev Java")
                .description("Description complète")
                .companyName("TechCorp")
                .location("Paris")
                .contractType("CDI")
                .salary("45-55k")
                .salaryMin(45000)
                .salaryMax(55000)
                .domain("IT")
                .experienceLevel(Offer.ExperienceLevel.MID)
                .remote(true)
                .positions(3)
                .requiredSkills(Set.of("Java", "Spring"))
                .companyId(1L)
                .status(Offer.OfferStatus.ACTIVE)
                .build();

        Offer entity = mapper.toEntity(request);

        assertThat(entity.getTitle()).isEqualTo("Dev Java");
        assertThat(entity.getDescription()).isEqualTo("Description complète");
        assertThat(entity.getCompanyName()).isEqualTo("TechCorp");
        assertThat(entity.getLocation()).isEqualTo("Paris");
        assertThat(entity.getContractType()).isEqualTo("CDI");
        assertThat(entity.getSalaryMin()).isEqualTo(45000);
        assertThat(entity.getSalaryMax()).isEqualTo(55000);
        assertThat(entity.getDomain()).isEqualTo("IT");
        assertThat(entity.getExperienceLevel()).isEqualTo(Offer.ExperienceLevel.MID);
        assertThat(entity.getRemote()).isTrue();
        assertThat(entity.getPositions()).isEqualTo(3);
        assertThat(entity.getRequiredSkills()).containsExactlyInAnyOrder("Java", "Spring");
        assertThat(entity.getCompanyId()).isEqualTo(1L);
        assertThat(entity.getStatus()).isEqualTo(Offer.OfferStatus.ACTIVE);
    }

    @Test
    @DisplayName("toEntity() applique les valeurs par défaut si null")
    void toEntity_shouldApplyDefaults_whenNullFields() {
        OfferRequest request = OfferRequest.builder()
                .title("Dev Java")
                .description("Desc")
                .companyName("Corp")
                .location("Lyon")
                .contractType("CDD")
                .companyId(2L)
                .build(); // remote, positions, status, skills = null

        Offer entity = mapper.toEntity(request);

        assertThat(entity.getRemote()).isFalse();
        assertThat(entity.getPositions()).isEqualTo(1);
        assertThat(entity.getStatus()).isEqualTo(Offer.OfferStatus.ACTIVE);
        assertThat(entity.getRequiredSkills()).isEmpty();
    }

    @Test
    @DisplayName("toResponse() mappe tous les champs correctement")
    void toResponse_shouldMapAllFields() {
        Offer offer = Offer.builder()
                .id(1L)
                .title("Dev Java")
                .description("Desc")
                .companyName("TechCorp")
                .location("Paris")
                .contractType("CDI")
                .salary("50k")
                .salaryMin(50000)
                .salaryMax(60000)
                .domain("IT")
                .experienceLevel(Offer.ExperienceLevel.SENIOR)
                .remote(false)
                .positions(2)
                .requiredSkills(Set.of("Java"))
                .viewCount(42L)
                .companyId(1L)
                .status(Offer.OfferStatus.ACTIVE)
                .build();

        OfferResponse response = mapper.toResponse(offer);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Dev Java");
        assertThat(response.getViewCount()).isEqualTo(42L);
        assertThat(response.getStatus()).isEqualTo(Offer.OfferStatus.ACTIVE);
        assertThat(response.getExperienceLevel()).isEqualTo(Offer.ExperienceLevel.SENIOR);
        assertThat(response.getRequiredSkills()).contains("Java");
        assertThat(response.getOpen()).isTrue(); // ACTIVE sans expiresAt = ouvert
    }

    @Test
    @DisplayName("toResponse() indique open=false pour une offre CLOSED")
    void toResponse_shouldReturnOpenFalse_whenOfferClosed() {
        Offer offer = Offer.builder()
                .id(1L).title("Dev").description("Desc").companyName("Corp")
                .location("Paris").contractType("CDI").companyId(1L)
                .status(Offer.OfferStatus.CLOSED)
                .viewCount(0L).positions(1).remote(false)
                .build();

        OfferResponse response = mapper.toResponse(offer);

        assertThat(response.getOpen()).isFalse();
    }

    @Test
    @DisplayName("toResponse() indique open=false pour une offre expirée")
    void toResponse_shouldReturnOpenFalse_whenOfferExpired() {
        Offer offer = Offer.builder()
                .id(1L).title("Dev").description("Desc").companyName("Corp")
                .location("Paris").contractType("CDI").companyId(1L)
                .status(Offer.OfferStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().minusDays(1)) // expirée hier
                .viewCount(0L).positions(1).remote(false)
                .build();

        OfferResponse response = mapper.toResponse(offer);

        assertThat(response.getOpen()).isFalse();
    }

    @Test
    @DisplayName("updateEntityFromRequest() met à jour uniquement les champs non-null")
    void updateEntityFromRequest_shouldUpdateOnlyNonNullFields() {
        Offer offer = Offer.builder()
                .id(1L).title("Ancien titre").description("Ancienne desc")
                .companyName("Corp").location("Paris").contractType("CDI")
                .companyId(1L).remote(false).positions(1)
                .status(Offer.OfferStatus.ACTIVE).viewCount(10L)
                .build();

        OfferRequest update = OfferRequest.builder()
                .title("Nouveau titre")
                .description("Nouvelle desc")
                .companyName("Corp")
                .location("Lyon")
                .contractType("CDI")
                .companyId(1L)
                // remote, positions, status = null → ne doivent pas changer
                .build();

        mapper.updateEntityFromRequest(offer, update);

        assertThat(offer.getTitle()).isEqualTo("Nouveau titre");
        assertThat(offer.getLocation()).isEqualTo("Lyon");
        assertThat(offer.getRemote()).isFalse();   // inchangé
        assertThat(offer.getPositions()).isEqualTo(1); // inchangé
        assertThat(offer.getStatus()).isEqualTo(Offer.OfferStatus.ACTIVE); // inchangé
    }
}
