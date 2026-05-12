package com.yebur.backendorderly.appconfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppConfigServiceTest {

    @Mock private AppConfigRepository repository;
    @Mock private AesEncryptionService encryption;
    @InjectMocks private AppConfigService service;

    @Test
    void getOrCreateConfig_returnsDefaultsWhenEmpty() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        AppConfig defaultConfig = new AppConfig();
        defaultConfig.setId(1L);
        defaultConfig.setTheme("light");
        when(repository.saveAndFlush(any())).thenReturn(defaultConfig);

        AppConfig result = service.getOrCreateConfig();

        assertThat(result.getTheme()).isEqualTo("light");
        assertThat(result.getId()).isEqualTo(1L);
        verify(repository).saveAndFlush(any());
    }

    @Test
    void updateConfig_setsAllFields() {
        AppConfig existing = new AppConfig();
        existing.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(encryption.encrypt("new_pass")).thenReturn("encrypted_new_pass");

        ConfigRequest req = new ConfigRequest();
        req.setTheme("dark");
        req.setSmtpHost("smtp.example.com");
        req.setSmtpPort(587);
        req.setSmtpPassword("new_pass");
        req.setSmtpUseTls(true);

        AppConfig result = service.updateConfig(req);

        assertThat(result.getTheme()).isEqualTo("dark");
        assertThat(result.getSmtpHost()).isEqualTo("smtp.example.com");
        assertThat(result.getSmtpPassword()).isEqualTo("encrypted_new_pass");
    }

    @Test
    void updateConfig_nullPassword_keepsPreviousEncryptedPassword() {
        AppConfig existing = new AppConfig();
        existing.setId(1L);
        existing.setSmtpPassword("existing_encrypted_value");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ConfigRequest req = new ConfigRequest();
        req.setTheme("light");
        req.setSmtpPassword(null);

        AppConfig result = service.updateConfig(req);

        assertThat(result.getSmtpPassword()).isEqualTo("existing_encrypted_value");
        verify(encryption, never()).encrypt(any());
    }
}
