package com.yebur.backendorderly.appconfig;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigRequest {
    private String theme;
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private String smtpReceiverEmail;
    private Boolean smtpUseTls;
    private String printerName;
}
