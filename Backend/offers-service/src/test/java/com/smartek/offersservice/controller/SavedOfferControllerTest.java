package com.smartek.offersservice.controller;

import com.smartek.offersservice.config.TestSecurityConfig;
import com.smartek.offersservice.dto.OfferResponse;
import com.smartek.offersservice.exception.BusinessException;
import com.smartek.offersservice.exception.GlobalExceptionHandler;
import com.smartek.offersservice.exception.ResourceNotFoundException;
import com.smartek.offersservice.service.SavedOfferService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SavedOfferController.class)
@Import({GlobalExceptionHandler.class, TestSecurityConfig.class})
@DisplayName("SavedOfferController — Tests WebMvc")
class SavedOfferControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private SavedOfferService savedOfferService;

    private OfferResponse buildOfferResponse() {
        return OfferResponse.builder()
                .id(1L).title("Dev Java").companyName("TechCorp")
                .location("Paris").contractType("CDI").companyId(1L)
                .status("ACTIVE").viewCount(0L).positions(1).remote(false).open(true)
                .build();
    }

    // ─── POST /api/saved-offers/{offerId}/learner/{learnerId} ─────────────────

    @Nested
    @DisplayName("POST /api/saved-offers/{offerId}/learner/{learnerId}")
    class SaveOfferTests {

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("200 OK si offre sauvegardée avec succès")
        void saveOffer_shouldReturn200() throws Exception {
            doNothing().when(savedOfferService).saveOffer(1L, 2L);

            mockMvc.perform(post("/api/saved-offers/1/learner/2"))
                    .andExpect(status().isOk());

            verify(savedOfferService).saveOffer(1L, 2L);
        }

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("422 si offre déjà en favoris")
        void saveOffer_alreadySaved_shouldReturn422() throws Exception {
            doThrow(new BusinessException("Cette offre est déjà dans vos favoris"))
                    .when(savedOfferService).saveOffer(1L, 2L);

            mockMvc.perform(post("/api/saved-offers/1/learner/2"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("favoris")));
        }

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("404 si offre introuvable")
        void saveOffer_offerNotFound_shouldReturn404() throws Exception {
            doThrow(new ResourceNotFoundException("Offre non trouvée avec l'id: 99"))
                    .when(savedOfferService).saveOffer(99L, 2L);

            mockMvc.perform(post("/api/saved-offers/99/learner/2"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── DELETE /api/saved-offers/{offerId}/learner/{learnerId} ──────────────

    @Nested
    @DisplayName("DELETE /api/saved-offers/{offerId}/learner/{learnerId}")
    class UnsaveOfferTests {

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("204 No Content si offre retirée des favoris")
        void unsaveOffer_shouldReturn204() throws Exception {
            doNothing().when(savedOfferService).unsaveOffer(1L, 2L);

            mockMvc.perform(delete("/api/saved-offers/1/learner/2"))
                    .andExpect(status().isNoContent());

            verify(savedOfferService).unsaveOffer(1L, 2L);
        }

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("404 si favori introuvable")
        void unsaveOffer_notFound_shouldReturn404() throws Exception {
            doThrow(new ResourceNotFoundException("Favori non trouvé"))
                    .when(savedOfferService).unsaveOffer(1L, 99L);

            mockMvc.perform(delete("/api/saved-offers/1/learner/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── GET /api/saved-offers/learner/{learnerId} ────────────────────────────

    @Nested
    @DisplayName("GET /api/saved-offers/learner/{learnerId}")
    class GetSavedOffersTests {

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("200 avec liste des offres sauvegardées")
        void getSavedOffers_shouldReturn200_withList() throws Exception {
            when(savedOfferService.getSavedOffersByLearner(2L))
                    .thenReturn(List.of(buildOfferResponse()));

            mockMvc.perform(get("/api/saved-offers/learner/2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].title").value("Dev Java"))
                    .andExpect(jsonPath("$[0].status").value("ACTIVE"));
        }

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("200 avec liste vide si aucun favori")
        void getSavedOffers_shouldReturn200_withEmptyList() throws Exception {
            when(savedOfferService.getSavedOffersByLearner(99L)).thenReturn(List.of());

            mockMvc.perform(get("/api/saved-offers/learner/99"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ─── GET /api/saved-offers/{offerId}/learner/{learnerId}/check ───────────

    @Nested
    @DisplayName("GET /api/saved-offers/{offerId}/learner/{learnerId}/check")
    class IsSavedTests {

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("200 true si offre en favoris")
        void isSaved_true_shouldReturn200() throws Exception {
            when(savedOfferService.isSaved(1L, 2L)).thenReturn(true);

            mockMvc.perform(get("/api/saved-offers/1/learner/2/check"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));
        }

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("200 false si offre non en favoris")
        void isSaved_false_shouldReturn200() throws Exception {
            when(savedOfferService.isSaved(1L, 99L)).thenReturn(false);

            mockMvc.perform(get("/api/saved-offers/1/learner/99/check"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }
    }
}
