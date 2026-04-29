package com.smartek.sponsor.controller;

import com.smartek.sponsor.service.SponsorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SponsorController.class)
@ActiveProfiles("test")
class SponsorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SponsorService sponsorService;

    @Test
    void testGetAllSponsors() throws Exception {
        mockMvc.perform(get("/api/sponsors"))
                .andExpect(status().isOk());
    }
}
