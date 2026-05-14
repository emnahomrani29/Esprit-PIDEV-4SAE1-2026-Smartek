package com.smartek.offersservice.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartek.offersservice.controller.OfferController;
import com.smartek.offersservice.dto.OfferRequest;
import com.smartek.offersservice.service.OfferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests du GlobalExceptionHandler via le OfferController — standalone MockMvc (sans contexte Spring).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler — Tests")
class GlobalExceptionHandlerTest {

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
                .build();
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
    @DisplayName("MethodArgumentNotValidException → 400 avec erreurs de validation")
    void validationException_shouldReturn400_withFieldErrors() throws Exception {
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
    @DisplayName("Validation multiple → 400 avec toutes les erreurs de champs")
    void multipleValidationErrors_shouldReturn400_withAllErrors() throws Exception {
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
