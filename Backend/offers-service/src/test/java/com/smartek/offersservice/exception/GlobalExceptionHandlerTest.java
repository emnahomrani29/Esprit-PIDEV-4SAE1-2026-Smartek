package com.smartek.offersservice.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartek.offersservice.controller.OfferController;
import com.smartek.offersservice.dto.OfferRequest;
import com.smartek.offersservice.security.JwtAuthFilter;
import com.smartek.offersservice.security.JwtService;
import com.smartek.offersservice.security.SecurityConfig;
import com.smartek.offersservice.service.OfferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests du GlobalExceptionHandler via le OfferController.
 * Vérifie que chaque type d'exception produit le bon code HTTP et la bonne structure JSON.
 */
@WebMvcTest(OfferController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
@DisplayName("GlobalExceptionHandler — Tests")
class GlobalExceptionHandlerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private OfferService offerService;
    @MockBean private JwtAuthFilter jwtAuthFilter;
    @MockBean private JwtService jwtService;

    @BeforeEach
    void configureMockFilter() throws Exception {
        doAnswer(inv -> {
            jakarta.servlet.FilterChain chain = inv.getArgument(2);
            chain.doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("ResourceNotFoundException → 404 avec message")
    void resourceNotFound_shouldReturn404() throws Exception {
        when(offerService.getOfferById(99L))
                .thenThrow(new ResourceNotFoundException("Offre non trouvée avec l'id: 99"));

        mockMvc.perform(get("/api/offers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("99")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    @DisplayName("BusinessException → 422 avec message")
    void businessException_shouldReturn422() throws Exception {
        when(offerService.createOffer(any()))
                .thenThrow(new BusinessException("La date d'expiration ne peut pas être dans le passé"));

        OfferRequest request = OfferRequest.builder()
                .title("Dev Java").description("Desc").companyName("Corp")
                .location("Paris").contractType("CDI").companyId(1L).build();

        mockMvc.perform(post("/api/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    @DisplayName("MethodArgumentNotValidException → 400 avec erreurs de validation")
    void validationException_shouldReturn400_withFieldErrors() throws Exception {
        // Requête sans titre (champ obligatoire)
        String body = """
            {
                "description": "Desc",
                "companyName": "Corp",
                "location": "Paris",
                "contractType": "CDI",
                "companyId": 1
            }
            """;

        mockMvc.perform(post("/api/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    @DisplayName("Exception générique → 500 avec message générique")
    void genericException_shouldReturn500() throws Exception {
        when(offerService.createOffer(any()))
                .thenThrow(new RuntimeException("Erreur inattendue"));

        OfferRequest request = OfferRequest.builder()
                .title("Dev Java").description("Desc").companyName("Corp")
                .location("Paris").contractType("CDI").companyId(1L).build();

        mockMvc.perform(post("/api/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Une erreur interne est survenue"));
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    @DisplayName("Validation multiple → 400 avec toutes les erreurs de champs")
    void multipleValidationErrors_shouldReturn400_withAllErrors() throws Exception {
        // Requête sans titre, description, companyName, location, contractType, companyId
        String body = "{}";

        mockMvc.perform(post("/api/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isMap())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.description").exists())
                .andExpect(jsonPath("$.errors.companyId").exists());
    }
}
