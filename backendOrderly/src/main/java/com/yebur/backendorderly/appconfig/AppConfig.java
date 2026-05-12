package com.yebur.backendorderly.appconfig;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppConfig {

    @Id
    private Long id = 1L;

    private String theme = "light";
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private String smtpReceiverEmail;
    private Boolean smtpUseTls = false;
    private String printerName;
}
