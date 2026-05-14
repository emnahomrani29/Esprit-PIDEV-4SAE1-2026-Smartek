package com.smartek.offersservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartek.offersservice.dto.OfferResponse;
import com.smartek.offersservice.exception.BusinessException;
import com.smartek.offersservice.exception.GlobalExceptionHandler;
import com.smartek.offersservice.exception.ResourceNotFoundException;
import com.smartek.offersservice.service.SavedOfferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests du controller SavedOfferController — standalone MockMvc (sans contexte Spring).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SavedOfferController — Tests")
class SavedOfferControllerTest {

    @Mock
    private SavedOfferService savedOfferService;

    @InjectMocks
    private SavedOfferController savedOfferController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(savedOfferController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private OfferResponse buildOfferResponse() {
        return OfferResponse.builder()
                .id(1L).title("Dev Java").companyName("TechCorp")
                .location("Paris").contractType("CDI").companyId(1L)
                .status("ACTIVE").viewCount(0L).positions(1).remote(false).open(true)
                .build();
    }

    @Nested
    @DisplayName("POST /api/saved-offers/{offerId}/learner/{learnerId}")
    class SaveOfferTests {

        @Test
        @DisplayName("200 OK si offre sauvegardée avec succès")
        void saveOffer_shouldReturn200() throws Exception {
            doNothing().when(savedOfferService).saveOffer(1L, 2L);

            mockMvc.perform(post("/api/saved-offers/1/learner/2"))
                    .andExpect(status().isOk());

            verify(savedOfferService).saveOffer(1L, 2L);
        }

        @Test
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
        @DisplayName("404 si offre introuvable")
        void saveOffer_offerNotFound_shouldReturn404() throws Exception {
            doThrow(new ResourceNotFoundException("Offre non trouvée avec l'id: 99"))
                    .when(savedOfferService).saveOffer(99L, 2L);

            mockMvc.perform(post("/api/saved-offers/99/learner/2"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/saved-offers/{offerId}/learner/{learnerId}")
    class UnsaveOfferTests {

        @Test
        @DisplayName("204 No Content si offre retirée des favoris")
        void unsaveOffer_shouldReturn204() throws Exception {
            doNothing().when(savedOfferService).unsaveOffer(1L, 2L);

            mockMvc.perform(delete("/api/saved-offers/1/learner/2"))
                    .andExpect(status().isNoContent());

            verify(savedOfferService).unsaveOffer(1L, 2L);
        }

        @Test
        @DisplayName("404 si favori introuvable")
        void unsaveOffer_notFound_shouldReturn404() throws Exception {
            doThrow(new ResourceNotFoundException("Favori non trouvé"))
                    .when(savedOfferService).unsaveOffer(1L, 99L);

            mockMvc.perform(delete("/api/saved-offers/1/learner/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/saved-offers/learner/{learnerId}")
    class GetSavedOffersTests {

        @Test
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
        @DisplayName("200 avec liste vide si aucun favori")
        void getSavedOffers_shouldReturn200_withEmptyList() throws Exception {
            when(savedOfferService.getSavedOffersByLearner(99L)).thenReturn(List.of());

            mockMvc.perform(get("/api/saved-offers/learner/99"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/saved-offers/{offerId}/learner/{learnerId}/check")
    class IsSavedTests {

        @Test
        @DisplayName("200 true si offre en favoris")
        void isSaved_true_shouldReturn200() throws Exception {
            when(savedOfferService.isSaved(1L, 2L)).thenReturn(true);

            mockMvc.perform(get("/api/saved-offers/1/learner/2/check"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));
        }

        @Test
        @DisplayName("200 false si offre non en favoris")
        void isSaved_false_shouldReturn200() throws Exception {
            when(savedOfferService.isSaved(1L, 99L)).thenReturn(false);

            mockMvc.perform(get("/api/saved-offers/1/learner/99/check"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }
    }
}
