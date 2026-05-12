package com.yebur.backendorderly.appconfig;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppConfigService {

    private final AppConfigRepository repository;
    private final AesEncryptionService encryption;

    @Transactional(readOnly = true)
    public AppConfig getOrCreateConfig() {
        return repository.findById(1L).orElseGet(this::createDefaults);
    }

    @Transactional
    public AppConfig updateConfig(ConfigRequest request) {
        AppConfig config = getOrCreateConfig();
        if (request.getTheme() != null) {
            config.setTheme(request.getTheme());
        }
        config.setSmtpHost(request.getSmtpHost());
        config.setSmtpPort(request.getSmtpPort());
        config.setSmtpUsername(request.getSmtpUsername());
        config.setSmtpReceiverEmail(request.getSmtpReceiverEmail());
        if (request.getSmtpUseTls() != null) {
            config.setSmtpUseTls(request.getSmtpUseTls());
        }
        config.setPrinterName(request.getPrinterName());
        if (request.getSmtpPassword() != null && !request.getSmtpPassword().isBlank()) {
            config.setSmtpPassword(encryption.encrypt(request.getSmtpPassword()));
        }
        return repository.save(config);
    }

    @Transactional
    AppConfig createDefaults() {
        AppConfig config = new AppConfig();
        config.setId(1L);
        config.setTheme("light");
        config.setSmtpUseTls(false);
        return repository.saveAndFlush(config);
    }
}
