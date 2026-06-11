package com.circleguard.file.controller;

import com.circleguard.file.service.FileStorageService;
import com.circleguard.file.monitoring.BusinessMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileUploadController.class)
@Import(FileUploadControllerTest.TestConfig.class)
class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileStorageService storageService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public BusinessMetrics businessMetrics() {
            return new BusinessMetrics(new SimpleMeterRegistry());
        }
    }

    @Test
    void shouldUploadFileSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "certificate.pdf", "application/pdf", "mock content".getBytes());

        Mockito.when(storageService.saveFile(Mockito.any())).thenReturn("certificate.pdf");

        mockMvc.perform(multipart("/api/v1/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("certificate.pdf"));
    }
}
