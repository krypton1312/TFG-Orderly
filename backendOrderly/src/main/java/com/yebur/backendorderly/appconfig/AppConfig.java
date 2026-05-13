package com.yebur.backendorderly.appconfig;

import com.yebur.backendorderly.employee.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Per-employee configuration row.
 * Legacy singleton rows can remain without employee assigned and are used as a template.
 * Password is stored AES-256-GCM encrypted (see AesEncryptionService).
 */
@Entity
@Table(name = "app_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class AppConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", unique = true)
    private Employee employee;

    @Deprecated
    @Column(name = "theme", length = 10)
    @Size(max = 10)
    private String theme = "light";

    @Column(name = "pc_theme", nullable = false, length = 10,
        columnDefinition = "varchar(10) not null default 'light'")
    @Size(max = 10)
    @ToString.Include
    private String pcTheme = "light";

    @Column(name = "mobile_theme", nullable = false, length = 10,
        columnDefinition = "varchar(10) not null default 'light'")
    @Size(max = 10)
    @ToString.Include
    private String mobileTheme = "light";

    @Column(length = 255)
    @Size(max = 255)
    private String smtpHost;

    @Column
    @Min(1)
    @Max(65535)
    private Integer smtpPort;

    @Column(length = 255)
    @Size(max = 255)
    private String smtpUsername;

    /** AES-256-GCM ciphertext — longer than raw input, stored as TEXT. */
    @Column(columnDefinition = "TEXT")
    private String smtpPassword;

    @Column(length = 255)
    @Email
    @Size(max = 255)
    private String smtpReceiverEmail;

    @Column(nullable = false, columnDefinition = "boolean not null default false")
    private Boolean smtpUseTls = false;

    @Column(length = 255)
    @Size(max = 255)
    private String printerName;
}
