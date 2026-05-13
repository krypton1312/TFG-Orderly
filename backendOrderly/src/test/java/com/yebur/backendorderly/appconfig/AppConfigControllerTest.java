package com.yebur.backendorderly.appconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AppConfigControllerTest {

    @Mock AppConfigService configService;
    @Mock AesEncryptionService encryption;
    @InjectMocks AppConfigController controller;

    MockMvc mockMvc;
    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getConfig_omitsSmtpPassword() throws Exception {
        AppConfig config = new AppConfig();
        config.setId(1L);
        config.setPcTheme("light");
        config.setMobileTheme("dark");
        config.setSmtpPassword("should_not_appear_in_response");
        when(configService.getOrCreateConfig(any())).thenReturn(config);

        String body = mockMvc.perform(get("/config"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain("smtpPassword");
        assertThat(body).doesNotContain("should_not_appear_in_response");
        assertThat(body).contains("\"pcTheme\"");
        assertThat(body).contains("\"mobileTheme\"");
    }

    @Test
    void putConfig_nullSmtpPassword_delegatesToService() throws Exception {
        AppConfig existing = new AppConfig();
        existing.setId(1L);
        existing.setSmtpPassword("original_encrypted");
        existing.setPcTheme("dark");
        existing.setMobileTheme("light");
        when(configService.updateConfig(any(), any())).thenReturn(existing);

        ConfigRequest req = new ConfigRequest();
        req.setPcTheme("dark");
        req.setMobileTheme("light");

        mockMvc.perform(put("/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void postTestSmtp_returnsSuccessOrFailureShape() throws Exception {
        AppConfig config = new AppConfig();
        config.setId(1L);
        when(configService.getOrCreateConfig(any())).thenReturn(config);

        String body = mockMvc.perform(post("/config/test-smtp"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).contains("\"success\"");
        assertThat(body).contains("false");
        assertThat(body).contains("\"error\"");
    }
}
