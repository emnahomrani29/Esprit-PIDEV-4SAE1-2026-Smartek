package com.smartek.offersservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartek.offersservice.dto.OfferRequest;
import com.smartek.offersservice.dto.OfferResponse;
import com.smartek.offersservice.dto.OfferStatsResponse;
import com.smartek.offersservice.exception.BusinessException;
import com.smartek.offersservice.exception.GlobalExceptionHandler;
import com.smartek.offersservice.exception.ResourceNotFoundException;
import com.smartek.offersservice.service.OfferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests du controller OfferController — standalone MockMvc (sans contexte Spring).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OfferController — Tests")
class OfferControllerTest {

    @Mock
    private OfferService offerService;

    @InjectMocks
    private OfferController offerController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(offerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private OfferResponse buildResponse(Long id, String title) {
        return OfferResponse.builder()
                .id(id).title(title)
                .companyName("TechCorp").location("Paris")
                .contractType("CDI").companyId(1L)
                .status("ACTIVE")
                .viewCount(0L).positions(1).remote(false)
                .open(true)
                .build();
    }

    @Test
    @DisplayName("GET /health → 200 OK")
    void health_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/offers/health"))
                .andExpect(status().isOk());
    }

    @Nested
    @DisplayName("GET /api/offers/{id}")
    class GetOfferByIdTests {

        @Test
        @DisplayName("200 avec l'offre si trouvée")
        void shouldReturn200_whenOfferFound() throws Exception {
            when(offerService.getOfferById(1L)).thenReturn(buildResponse(1L, "Dev Java"));

            mockMvc.perform(get("/api/offers/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Dev Java"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("404 si l'offre n'existe pas")
        void shouldReturn404_whenOfferNotFound() throws Exception {
            when(offerService.getOfferById(99L))
                    .thenThrow(new ResourceNotFoundException("Offre non trouvée avec l'id: 99"));

            mockMvc.perform(get("/api/offers/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("99")));
        }
    }

    @Nested
    @DisplayName("POST /api/offers")
    class CreateOfferTests {

        @Test
        @DisplayName("201 Created avec l'offre créée")
        void shouldReturn201_whenValid() throws Exception {
            OfferRequest request = OfferRequest.builder()
                    .title("Dev Java").description("Desc")
                    .companyName("Corp").location("Paris")
                    .contractType("CDI").companyId(1L)
                    .build();
            when(offerService.createOffer(any())).thenReturn(buildResponse(1L, "Dev Java"));

            mockMvc.perform(post("/api/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Dev Java"));
        }

        @Test
        @DisplayName("422 si règle métier violée")
        void shouldReturn422_whenBusinessRuleViolated() throws Exception {
            OfferRequest request = OfferRequest.builder()
                    .title("Dev Java").description("Desc")
                    .companyName("Corp").location("Paris")
                    .contractType("CDI").companyId(1L)
                    .build();
            when(offerService.createOffer(any()))
                    .thenThrow(new BusinessException("La date d'expiration doit être dans le futur"));

            mockMvc.perform(post("/api/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("expiration")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/offers/{id}")
    class DeleteOfferTests {

        @Test
        @DisplayName("204 No Content si suppression réussie")
        void shouldReturn204_whenDeleted() throws Exception {
            doNothing().when(offerService).deleteOffer(1L);

            mockMvc.perform(delete("/api/offers/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("422 si candidatures acceptées existent")
        void shouldReturn422_whenAcceptedApplicationsExist() throws Exception {
            doThrow(new BusinessException("Impossible de supprimer une offre avec des candidatures acceptées"))
                    .when(offerService).deleteOffer(1L);

            mockMvc.perform(delete("/api/offers/1"))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    @Nested
    @DisplayName("GET /api/offers/stats/company/{id}")
    class StatsTests {

        @Test
        @DisplayName("200 avec toutes les métriques")
        void shouldReturn200_withAllMetrics() throws Exception {
            OfferStatsResponse stats = OfferStatsResponse.builder()
                    .companyId(1L).totalOffers(5L).activeOffers(3L)
                    .totalApplications(20L).acceptedApplications(5L)
                    .acceptanceRate(25.0).averageApplicationScore(72.5)
                    .build();
            when(offerService.getStatsByCompany(1L)).thenReturn(stats);

            mockMvc.perform(get("/api/offers/stats/company/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.companyId").value(1))
                    .andExpect(jsonPath("$.totalOffers").value(5))
                    .andExpect(jsonPath("$.acceptanceRate").value(25.0))
                    .andExpect(jsonPath("$.averageApplicationScore").value(72.5));
        }
    }

    @Test
    @DisplayName("POST /api/offers/search → 200 avec résultats paginés")
    void searchOffers_shouldReturn200() throws Exception {
        var page = new PageImpl<>(
                List.of(buildResponse(1L, "Dev Java")),
                PageRequest.of(0, 10), 1);
        when(offerService.searchOffers(any())).thenReturn(page);

        mockMvc.perform(post("/api/offers/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"keyword\":\"Java\",\"page\":0,\"size\":10,\"sortBy\":\"createdAt\",\"sortDir\":\"desc\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Dev Java"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/offers → 200 avec pagination")
    void getAllOffers_shouldReturn200_withPagination() throws Exception {
        var page = new PageImpl<>(List.of(buildResponse(1L, "Dev Java")), PageRequest.of(0, 10), 1);
        when(offerService.getAllOffers(0, 10, "createdAt", "desc")).thenReturn(page);

        mockMvc.perform(get("/api/offers?page=0&size=10&sortBy=createdAt&sortDir=desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/offers/company/{id} → 200")
    void getOffersByCompany_shouldReturn200() throws Exception {
        when(offerService.getOffersByCompanyId(5L)).thenReturn(List.of(buildResponse(1L, "Dev Java")));

        mockMvc.perform(get("/api/offers/company/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Dev Java"));
    }

    @Test
    @DisplayName("GET /api/offers/top-viewed → 200")
    void getTopViewedOffers_shouldReturn200() throws Exception {
        when(offerService.getTopViewedOffers(5)).thenReturn(List.of(buildResponse(1L, "Dev Java")));

        mockMvc.perform(get("/api/offers/top-viewed?limit=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Nested
    @DisplayName("PUT /api/offers/{id}")
    class UpdateOfferTests {

        @Test
        @DisplayName("200 avec l'offre mise à jour")
        void shouldReturn200_whenUpdated() throws Exception {
            OfferRequest request = OfferRequest.builder()
                    .title("Dev Java Senior").description("Desc")
                    .companyName("Corp").location("Paris")
                    .contractType("CDI").companyId(1L).build();
            when(offerService.updateOffer(eq(1L), any())).thenReturn(buildResponse(1L, "Dev Java Senior"));

            mockMvc.perform(put("/api/offers/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Dev Java Senior"));
        }

        @Test
        @DisplayName("404 si offre non trouvée")
        void shouldReturn404_whenNotFound() throws Exception {
            OfferRequest request = OfferRequest.builder()
                    .title("Dev").description("Desc").companyName("Corp")
                    .location("Paris").contractType("CDI").companyId(1L).build();
            when(offerService.updateOffer(eq(99L), any()))
                    .thenThrow(new ResourceNotFoundException("Offer not found with id: 99"));

            mockMvc.perform(put("/api/offers/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @DisplayName("GET /api/offers/status/ACTIVE → 200")
    void getOffersByStatus_shouldReturn200() throws Exception {
        var page = new PageImpl<>(List.of(buildResponse(1L, "Dev Java")), PageRequest.of(0, 10), 1);
        when(offerService.getOffersByStatus("ACTIVE", 0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/offers/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
