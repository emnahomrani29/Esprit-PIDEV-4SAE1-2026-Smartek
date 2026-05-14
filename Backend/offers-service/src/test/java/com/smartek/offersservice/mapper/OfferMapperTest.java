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
                .experienceLevel("MID")
                .remote(true)
                .positions(3)
                .requiredSkills(Set.of("Java", "Spring"))
                .companyId(1L)
                .status("ACTIVE")
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
        assertThat(entity.getExperienceLevel()).isEqualTo("MID");
        assertThat(entity.getRemote()).isTrue();
        assertThat(entity.getPositions()).isEqualTo(3);
        assertThat(entity.getRequiredSkills()).containsExactlyInAnyOrder("Java", "Spring");
        assertThat(entity.getCompanyId()).isEqualTo(1L);
        assertThat(entity.getStatus()).isEqualTo("ACTIVE");
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
                .build();

        Offer entity = mapper.toEntity(request);

        assertThat(entity.getRemote()).isFalse();
        assertThat(entity.getPositions()).isEqualTo(1);
        assertThat(entity.getStatus()).isEqualTo("ACTIVE");
        assertThat(entity.getRequiredSkills()).isEmpty();
    }

    @Test
    @DisplayName("toResponse() mappe tous les champs correctement")
    void toResponse_shouldMapAllFields() {
        Offer offer = new Offer();
        offer.setId(1L);
        offer.setTitle("Dev Java");
        offer.setDescription("Desc");
        offer.setCompanyName("TechCorp");
        offer.setLocation("Paris");
        offer.setContractType("CDI");
        offer.setSalary("50k");
        offer.setSalaryMin(50000);
        offer.setSalaryMax(60000);
        offer.setDomain("IT");
        offer.setExperienceLevel("SENIOR");
        offer.setRemote(false);
        offer.setPositions(2);
        offer.setRequiredSkills(Set.of("Java"));
        offer.setViewCount(42L);
        offer.setCompanyId(1L);
        offer.setStatus("ACTIVE");

        OfferResponse response = mapper.toResponse(offer);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Dev Java");
        assertThat(response.getViewCount()).isEqualTo(42L);
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        assertThat(response.getExperienceLevel()).isEqualTo("SENIOR");
        assertThat(response.getRequiredSkills()).contains("Java");
        assertThat(response.getOpen()).isTrue(); // ACTIVE sans expiresAt = ouvert
    }

    @Test
    @DisplayName("toResponse() indique open=false pour une offre CLOSED")
    void toResponse_shouldReturnOpenFalse_whenOfferClosed() {
        Offer offer = new Offer();
        offer.setId(1L);
        offer.setTitle("Dev");
        offer.setStatus("CLOSED");
        offer.setViewCount(0L);
        offer.setPositions(1);
        offer.setRemote(false);
        offer.setCompanyId(1L);

        OfferResponse response = mapper.toResponse(offer);

        assertThat(response.getOpen()).isFalse();
    }

    @Test
    @DisplayName("toResponse() indique open=false pour une offre expirée")
    void toResponse_shouldReturnOpenFalse_whenOfferExpired() {
        Offer offer = new Offer();
        offer.setId(1L);
        offer.setTitle("Dev");
        offer.setStatus("ACTIVE");
        offer.setExpiresAt(LocalDateTime.now().minusDays(1));
        offer.setViewCount(0L);
        offer.setPositions(1);
        offer.setRemote(false);
        offer.setCompanyId(1L);

        OfferResponse response = mapper.toResponse(offer);

        assertThat(response.getOpen()).isFalse();
    }

    @Test
    @DisplayName("updateEntityFromRequest() met à jour uniquement les champs non-null")
    void updateEntityFromRequest_shouldUpdateOnlyNonNullFields() {
        Offer offer = new Offer();
        offer.setId(1L);
        offer.setTitle("Ancien titre");
        offer.setDescription("Ancienne desc");
        offer.setCompanyName("Corp");
        offer.setLocation("Paris");
        offer.setContractType("CDI");
        offer.setCompanyId(1L);
        offer.setRemote(false);
        offer.setPositions(1);
        offer.setStatus("ACTIVE");
        offer.setViewCount(10L);

        OfferRequest update = OfferRequest.builder()
                .title("Nouveau titre")
                .description("Nouvelle desc")
                .companyName("Corp")
                .location("Lyon")
                .contractType("CDI")
                .companyId(1L)
                .build();

        mapper.updateEntityFromRequest(offer, update);

        assertThat(offer.getTitle()).isEqualTo("Nouveau titre");
        assertThat(offer.getLocation()).isEqualTo("Lyon");
        assertThat(offer.getRemote()).isFalse();
        assertThat(offer.getPositions()).isEqualTo(1);
        assertThat(offer.getStatus()).isEqualTo("ACTIVE");
    }
}
