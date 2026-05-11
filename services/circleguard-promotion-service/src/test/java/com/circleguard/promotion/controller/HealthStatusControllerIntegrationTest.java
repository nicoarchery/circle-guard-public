package com.circleguard.promotion.controller;

import com.circleguard.promotion.security.JwtAuthenticationFilter;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class HealthStatusControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthStatusService statusService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser(roles = "HEALTH_CENTER")
    void confirmPositiveShouldCallServiceAndReturnOk() throws Exception {
        mockMvc.perform(post("/api/v1/health/confirmed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"anonymousId\":\"user-1\"}"))
                .andExpect(status().isOk());

        verify(statusService).updateStatus("user-1", "CONFIRMED");
    }

    @Test
    @WithMockUser(roles = "HEALTH_CENTER")
    void reportStatusShouldCallOverrideAwareUpdate() throws Exception {
        mockMvc.perform(post("/api/v1/health/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"anonymousId\":\"user-2\",\"status\":\"POTENTIAL\",\"adminOverride\":true}"))
                .andExpect(status().isOk());

        verify(statusService).updateStatus("user-2", "POTENTIAL", true);
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void resolveShouldBeForbiddenForNonHealthCenterUsers() throws Exception {
        mockMvc.perform(post("/api/v1/health/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"anonymousId\":\"user-3\"}"))
                .andExpect(status().isForbidden());
    }
}
