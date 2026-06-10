package com.circleguard.promotion.e2e;

import com.circleguard.promotion.security.SecurityConfig;
import com.circleguard.promotion.service.HealthStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class HealthStatusE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthStatusService statusService;

    @Test
    @WithMockUser(authorities = "HEALTH_CENTER")
    void reportShouldUpdateStatusWithOverride() throws Exception {
        mockMvc.perform(post("/api/v1/health/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"anonymousId\":\"user-100\",\"status\":\"POTENTIAL\",\"adminOverride\":true}"))
                .andExpect(status().isOk());

        verify(statusService).updateStatus("user-100", "POTENTIAL", true);
    }

    @Test
    @WithMockUser(authorities = "STUDENT")
    void resolveShouldBeForbiddenForStudent() throws Exception {
        mockMvc.perform(post("/api/v1/health/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"anonymousId\":\"user-200\"}"))
                .andExpect(status().isForbidden());
    }
}
