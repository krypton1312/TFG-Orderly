package com.yebur.backendorderly.appconfig;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConfigResponse {
    private String theme;
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    // smtpPassword intentionally ABSENT — never returned to client (security: D-05)
    private String smtpReceiverEmail;
    private Boolean smtpUseTls;
    private String printerName;

    public static ConfigResponse fromEntity(AppConfig config) {
        ConfigResponse r = new ConfigResponse();
        r.setTheme(config.getTheme());
        r.setSmtpHost(config.getSmtpHost());
        r.setSmtpPort(config.getSmtpPort());
        r.setSmtpUsername(config.getSmtpUsername());
        // DO NOT set smtpPassword
        r.setSmtpReceiverEmail(config.getSmtpReceiverEmail());
        r.setSmtpUseTls(config.getSmtpUseTls());
        r.setPrinterName(config.getPrinterName());
        return r;
    }
}
