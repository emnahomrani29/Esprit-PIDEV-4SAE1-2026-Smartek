package com.smartek.offersservice.integration;

import com.smartek.offersservice.entity.Offer;
import com.smartek.offersservice.entity.SavedOffer;
import com.smartek.offersservice.repository.ApplicationRepository;
import com.smartek.offersservice.repository.InterviewFeedbackRepository;
import com.smartek.offersservice.repository.InterviewRepository;
import com.smartek.offersservice.repository.OfferRepository;
import com.smartek.offersservice.repository.SavedOfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("SavedOffer — Tests d'intégration")
class SavedOfferIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private OfferRepository offerRepository;
    @Autowired private SavedOfferRepository savedOfferRepository;
    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private InterviewRepository interviewRepository;
    @Autowired private InterviewFeedbackRepository feedbackRepository;

    @BeforeEach
    void cleanDatabase() {
        feedbackRepository.deleteAll();
        interviewRepository.deleteAll();
        applicationRepository.deleteAll();
        savedOfferRepository.deleteAll();
        offerRepository.deleteAll();
    }

    private Offer saveOffer(String title) {
        return offerRepository.save(Offer.builder()
                .title(title).description("Description").companyName("TechCorp")
                .location("Paris").contractType("CDI").companyId(1L)
                .status("ACTIVE").viewCount(0L).positions(1).remote(false).build());
    }

    // ─── Sauvegarder une offre ────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/saved-offers/{offerId}/learner/{learnerId}")
    class SaveOfferTests {

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("200 OK si offre sauvegardée avec succès")
        void saveOffer_shouldReturn200_andPersist() throws Exception {
            Offer offer = saveOffer("Dev Java");

            mockMvc.perform(post("/api/saved-offers/" + offer.getId() + "/learner/10"))
                    .andExpect(status().isOk());

            assertThat(savedOfferRepository.existsByOfferIdAndLearnerId(offer.getId(), 10L)).isTrue();
        }

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("422 si offre déjà en favoris")
        void saveOffer_shouldReturn422_whenAlreadySaved() throws Exception {
            Offer offer = saveOffer("Dev Java");

            mockMvc.perform(post("/api/saved-offers/" + offer.getId() + "/learner/10"))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/saved-offers/" + offer.getId() + "/learner/10"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.message").value(containsString("favoris")));
        }

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("404 si offre introuvable")
        void saveOffer_shouldReturn404_whenOfferNotFound() throws Exception {
            mockMvc.perform(post("/api/saved-offers/99999/learner/10"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── Retirer une offre des favoris ────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/saved-offers/{offerId}/learner/{learnerId}")
    class UnsaveOfferTests {

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("204 No Content si offre retirée des favoris")
        void unsaveOffer_shouldReturn204_andDelete() throws Exception {
            Offer offer = saveOffer("Dev Java");
            SavedOffer saved = new SavedOffer();
            saved.setOfferId(offer.getId());
            saved.setLearnerId(10L);
            savedOfferRepository.save(saved);

            mockMvc.perform(delete("/api/saved-offers/" + offer.getId() + "/learner/10"))
                    .andExpect(status().isNoContent());

            assertThat(savedOfferRepository.existsByOfferIdAndLearnerId(offer.getId(), 10L)).isFalse();
        }

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("404 si favori introuvable")
        void unsaveOffer_shouldReturn404_whenNotSaved() throws Exception {
            Offer offer = saveOffer("Dev Java");

            mockMvc.perform(delete("/api/saved-offers/" + offer.getId() + "/learner/10"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── Lister les favoris ───────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/saved-offers/learner/{learnerId}")
    class GetSavedOffersTests {

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("200 avec liste des offres sauvegardées")
        void getSavedOffers_shouldReturn200_withList() throws Exception {
            Offer offer1 = saveOffer("Dev Java");
            Offer offer2 = saveOffer("Dev Python");

            SavedOffer s1 = new SavedOffer();
            s1.setOfferId(offer1.getId());
            s1.setLearnerId(10L);
            savedOfferRepository.save(s1);

            SavedOffer s2 = new SavedOffer();
            s2.setOfferId(offer2.getId());
            s2.setLearnerId(10L);
            savedOfferRepository.save(s2);

            mockMvc.perform(get("/api/saved-offers/learner/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("200 avec liste vide si aucun favori")
        void getSavedOffers_shouldReturn200_withEmptyList() throws Exception {
            mockMvc.perform(get("/api/saved-offers/learner/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ─── Vérifier si une offre est en favoris ─────────────────────────────────

    @Nested
    @DisplayName("GET /api/saved-offers/{offerId}/learner/{learnerId}/check")
    class IsSavedTests {

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("true si offre en favoris")
        void isSaved_shouldReturnTrue_whenSaved() throws Exception {
            Offer offer = saveOffer("Dev Java");
            SavedOffer saved = new SavedOffer();
            saved.setOfferId(offer.getId());
            saved.setLearnerId(10L);
            savedOfferRepository.save(saved);

            mockMvc.perform(get("/api/saved-offers/" + offer.getId() + "/learner/10/check"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));
        }

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("false si offre non en favoris")
        void isSaved_shouldReturnFalse_whenNotSaved() throws Exception {
            Offer offer = saveOffer("Dev Java");

            mockMvc.perform(get("/api/saved-offers/" + offer.getId() + "/learner/10/check"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }
    }
}
