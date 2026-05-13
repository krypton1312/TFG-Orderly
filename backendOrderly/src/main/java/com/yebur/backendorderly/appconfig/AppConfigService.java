package com.yebur.backendorderly.appconfig;

import com.yebur.backendorderly.employee.Employee;
import com.yebur.backendorderly.employee.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AppConfigService {

    private final AppConfigRepository repository;
    private final EmployeeRepository employeeRepository;
    private final AesEncryptionService encryption;

    @Transactional(readOnly = true)
    public AppConfig getOrCreateConfig(Authentication auth) {
        Employee employee = requireEmployee(auth);
        return repository.findByEmployeeId(employee.getId())
                .orElseGet(() -> createDefaults(employee));
    }

    @Transactional
    public AppConfig updateConfig(Authentication auth, ConfigRequest request) {
        AppConfig config = getOrCreateConfig(auth);

        String pcTheme = request.getPcTheme() != null ? request.getPcTheme() : request.getTheme();
        if (pcTheme != null) {
            String normalizedPcTheme = normalizeTheme(pcTheme);
            config.setPcTheme(normalizedPcTheme);
            config.setTheme(normalizedPcTheme);
        }
        if (request.getMobileTheme() != null) {
            config.setMobileTheme(normalizeTheme(request.getMobileTheme()));
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
    AppConfig createDefaults(Employee employee) {
        AppConfig config = new AppConfig();
        config.setEmployee(employee);

        repository.findFirstByEmployeeIsNullOrderByIdAsc()
                .ifPresent(template -> applyTemplate(config, template));

        config.setPcTheme(resolveTheme(config.getPcTheme(), config.getTheme()));
        config.setMobileTheme(resolveTheme(config.getMobileTheme(), config.getTheme()));
        config.setTheme(config.getPcTheme());
        if (config.getSmtpUseTls() == null) {
            config.setSmtpUseTls(false);
        }
        return repository.saveAndFlush(config);
    }

    private Employee requireEmployee(Authentication auth) {
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new IllegalArgumentException("Authenticated employee is required");
        }
        return employeeRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + auth.getName()));
    }

    private void applyTemplate(AppConfig target, AppConfig template) {
        target.setPcTheme(resolveTemplateTheme(template, false));
        target.setMobileTheme(resolveTemplateTheme(template, true));
        target.setTheme(target.getPcTheme());
        target.setSmtpHost(template.getSmtpHost());
        target.setSmtpPort(template.getSmtpPort());
        target.setSmtpUsername(template.getSmtpUsername());
        target.setSmtpPassword(template.getSmtpPassword());
        target.setSmtpReceiverEmail(template.getSmtpReceiverEmail());
        target.setSmtpUseTls(template.getSmtpUseTls());
        target.setPrinterName(template.getPrinterName());
    }

    private String resolveTemplateTheme(AppConfig template, boolean mobile) {
        if (template.getEmployee() == null && template.getTheme() != null && !template.getTheme().isBlank()) {
            return normalizeTheme(template.getTheme());
        }
        return resolveTheme(mobile ? template.getMobileTheme() : template.getPcTheme(), template.getTheme());
    }

    private String resolveTheme(String primaryTheme, String fallbackTheme) {
        if (primaryTheme != null && !primaryTheme.isBlank()) {
            return normalizeTheme(primaryTheme);
        }
        if (fallbackTheme != null && !fallbackTheme.isBlank()) {
            return normalizeTheme(fallbackTheme);
        }
        return "light";
    }

    private String normalizeTheme(String theme) {
        return theme == null || theme.isBlank()
                ? "light"
                : theme.trim().toLowerCase(Locale.ROOT);
    }
}
