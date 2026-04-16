package com.smartek.offersservice.mapper;

import com.smartek.offersservice.dto.ApplicationRequest;
import com.smartek.offersservice.dto.ApplicationResponse;
import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.entity.Offer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour ApplicationMapper.
 */
@DisplayName("ApplicationMapper — Tests unitaires")
class ApplicationMapperTest {

    private ApplicationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ApplicationMapper();
    }

    @Test
    @DisplayName("toEntity() crée une candidature PENDING avec score 0")
    void toEntity_shouldCreatePendingApplicationWithZeroScore() {
        ApplicationRequest request = ApplicationRequest.builder()
                .learnerId(10L)
                .learnerName("Alice Dupont")
                .learnerEmail("alice@test.com")
                .coverLetter("Ma lettre de motivation")
                .cvFileName("cv_alice.pdf")
                .build();

        Application entity = mapper.toEntity(request);

        assertThat(entity.getLearnerId()).isEqualTo(10L);
        assertThat(entity.getLearnerName()).isEqualTo("Alice Dupont");
        assertThat(entity.getLearnerEmail()).isEqualTo("alice@test.com");
        assertThat(entity.getCoverLetter()).isEqualTo("Ma lettre de motivation");
        assertThat(entity.getStatus()).isEqualTo("PENDING");
        assertThat(entity.getScore()).isEqualTo(0);
    }

    @Test
    @DisplayName("toResponse() inclut le titre et l'ID de l'offre si présente")
    void toResponse_shouldIncludeOfferInfo_whenOfferPresent() {
        Offer offer = new Offer();
        offer.setId(5L);
        offer.setTitle("Dev Java");

        Application application = new Application();
        application.setId(1L);
        application.setLearnerId(10L);
        application.setLearnerName("Bob Martin");
        application.setLearnerEmail("bob@test.com");
        application.setScore(80);
        application.setStatus("REVIEWED");
        application.setRecruiterNote("Bon profil");
        application.setAppliedAt(LocalDateTime.now());
        application.setOffer(offer);

        ApplicationResponse response = mapper.toResponse(application);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getOfferId()).isEqualTo(5L);
        assertThat(response.getOfferTitle()).isEqualTo("Dev Java");
        assertThat(response.getScore()).isEqualTo(80);
        assertThat(response.getStatus()).isEqualTo(Application.ApplicationStatus.REVIEWED);
        assertThat(response.getRecruiterNote()).isEqualTo("Bon profil");
    }

    @Test
    @DisplayName("toResponse() gère le cas où l'offre est null")
    void toResponse_shouldHandleNullOffer() {
        Application application = new Application();
        application.setId(1L);
        application.setLearnerId(10L);
        application.setLearnerName("Test");
        application.setLearnerEmail("test@test.com");
        application.setScore(0);
        application.setStatus("PENDING");

        ApplicationResponse response = mapper.toResponse(application);

        assertThat(response.getOfferId()).isNull();
        assertThat(response.getOfferTitle()).isNull();
    }
}
