package com.smartek.sponsor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SmartekSponsorApplicationTests {

    @Test
    void contextLoads() {
        // Test that Spring context loads successfully with H2 database
    }

    @Test
    void applicationStarts() {
        // Test that application can start
        assert true;
    }
}
