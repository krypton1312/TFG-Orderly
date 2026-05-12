package com.yebur.backendorderly.appconfig;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class AppConfigController {

    private final AppConfigService configService;
    private final AesEncryptionService encryption;

    @GetMapping
    public ResponseEntity<ConfigResponse> getConfig() {
        AppConfig config = configService.getOrCreateConfig();
        return ResponseEntity.ok(ConfigResponse.fromEntity(config));
    }

    @PutMapping
    public ResponseEntity<ConfigResponse> updateConfig(@RequestBody ConfigRequest request) {
        AppConfig updated = configService.updateConfig(request);
        return ResponseEntity.ok(ConfigResponse.fromEntity(updated));
    }

    @PostMapping("/test-smtp")
    public ResponseEntity<Map<String, Object>> testSmtp() {
        AppConfig config = configService.getOrCreateConfig();

        if (config.getSmtpHost() == null || config.getSmtpHost().isBlank()) {
            return ResponseEntity.ok(Map.of("success", false, "error", "SMTP no configurado"));
        }
        if (config.getSmtpReceiverEmail() == null || config.getSmtpReceiverEmail().isBlank()) {
            return ResponseEntity.ok(Map.of("success", false, "error", "Correo receptor no configurado"));
        }

        try {
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setHost(config.getSmtpHost());
            sender.setPort(config.getSmtpPort() != null ? config.getSmtpPort() : 587);
            sender.setUsername(config.getSmtpUsername());

            String decryptedPass = encryption.decrypt(config.getSmtpPassword());
            if (decryptedPass != null) {
                sender.setPassword(decryptedPass);
            }

            Properties props = sender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            if (Boolean.TRUE.equals(config.getSmtpUseTls())) {
                props.put("mail.smtp.starttls.enable", "true");
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(config.getSmtpReceiverEmail());
            message.setSubject("Orderly — Prueba de conexión SMTP");
            message.setText("Conexión SMTP configurada correctamente.");
            sender.send(message);

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Error desconocido";
            return ResponseEntity.ok(Map.of("success", false, "error", errorMsg));
        }
    }
}
