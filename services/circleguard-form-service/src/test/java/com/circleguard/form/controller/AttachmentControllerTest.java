package com.circleguard.form.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.circleguard.form.service.StorageService storageService;

    @Test
    void shouldUploadFile() throws Exception {
        org.mockito.Mockito.when(storageService.store(org.mockito.ArgumentMatchers.any())).thenReturn("mock-filename.pdf");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test data".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/attachments").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").exists());
    }
}
