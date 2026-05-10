package com.yebur.backendorderly.analytics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "JWT_SECRET=test-secret-key-for-testing-only-at-least-32-chars-long",
        "DB_PASSWORD=testpassword"
})
public class AnalyticsControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetMonthlySummaryRequiresAuth() throws Exception {
        mockMvc.perform(get("/analytics/monthly-summary")
                        .param("year", "2026")
                        .param("month", "5"))
                .andExpect(status().isUnauthorized());
    }
}
