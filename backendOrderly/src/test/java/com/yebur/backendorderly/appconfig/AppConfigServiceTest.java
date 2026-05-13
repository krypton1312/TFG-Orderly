package com.yebur.backendorderly.appconfig;

import com.yebur.backendorderly.employee.Employee;
import com.yebur.backendorderly.employee.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppConfigServiceTest {

    @Mock private AppConfigRepository repository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private AesEncryptionService encryption;
    @Mock private Authentication authentication;
    @InjectMocks private AppConfigService service;

    @Test
    void getOrCreateConfig_returnsDefaultsWhenEmpty() {
        Employee employee = authenticatedEmployee(7L, "test@example.com");
        when(repository.findByEmployeeId(7L)).thenReturn(Optional.empty());
        when(repository.findFirstByEmployeeIsNullOrderByIdAsc()).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        AppConfig result = service.getOrCreateConfig(authentication);

        assertThat(result.getEmployee()).isEqualTo(employee);
        assertThat(result.getPcTheme()).isEqualTo("light");
        assertThat(result.getMobileTheme()).isEqualTo("light");
        verify(repository).saveAndFlush(any());
    }

    @Test
    void updateConfig_setsAllFields() {
        authenticatedEmployee(7L, "test@example.com");
        AppConfig existing = new AppConfig();
        existing.setId(1L);
        when(repository.findByEmployeeId(7L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(encryption.encrypt("new_pass")).thenReturn("encrypted_new_pass");

        ConfigRequest req = new ConfigRequest();
        req.setPcTheme("dark");
        req.setMobileTheme("light");
        req.setSmtpHost("smtp.example.com");
        req.setSmtpPort(587);
        req.setSmtpPassword("new_pass");
        req.setSmtpUseTls(true);

        AppConfig result = service.updateConfig(authentication, req);

        assertThat(result.getPcTheme()).isEqualTo("dark");
        assertThat(result.getMobileTheme()).isEqualTo("light");
        assertThat(result.getTheme()).isEqualTo("dark");
        assertThat(result.getSmtpHost()).isEqualTo("smtp.example.com");
        assertThat(result.getSmtpPassword()).isEqualTo("encrypted_new_pass");
    }

    @Test
    void updateConfig_nullPassword_keepsPreviousEncryptedPassword() {
        authenticatedEmployee(7L, "test@example.com");
        AppConfig existing = new AppConfig();
        existing.setId(1L);
        existing.setSmtpPassword("existing_encrypted_value");
        existing.setPcTheme("light");
        existing.setMobileTheme("dark");
        when(repository.findByEmployeeId(7L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ConfigRequest req = new ConfigRequest();
        req.setPcTheme("light");
        req.setSmtpPassword(null);

        AppConfig result = service.updateConfig(authentication, req);

        assertThat(result.getSmtpPassword()).isEqualTo("existing_encrypted_value");
        verify(encryption, never()).encrypt(any());
    }

    @Test
    void getOrCreateConfig_inheritsLegacySingletonValues() {
        authenticatedEmployee(9L, "legacy@example.com");
        when(repository.findByEmployeeId(9L)).thenReturn(Optional.empty());

        AppConfig legacy = new AppConfig();
        legacy.setTheme("dark");
        legacy.setSmtpHost("smtp.legacy.local");
        legacy.setSmtpUseTls(true);
        when(repository.findFirstByEmployeeIsNullOrderByIdAsc()).thenReturn(Optional.of(legacy));
        when(repository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        AppConfig result = service.getOrCreateConfig(authentication);

        assertThat(result.getPcTheme()).isEqualTo("dark");
        assertThat(result.getMobileTheme()).isEqualTo("dark");
        assertThat(result.getSmtpHost()).isEqualTo("smtp.legacy.local");
        assertThat(result.getSmtpUseTls()).isTrue();
    }

    private Employee authenticatedEmployee(Long id, String email) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setEmail(email);
        when(authentication.getName()).thenReturn(email);
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        return employee;
    }
}
