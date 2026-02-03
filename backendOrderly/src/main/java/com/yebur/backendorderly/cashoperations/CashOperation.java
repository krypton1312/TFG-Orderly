package com.yebur.backendorderly.cashoperations;

import com.yebur.backendorderly.cashsessions.CashSession;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_operations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class CashOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private CashSession session;

    @Column
    @Enumerated(EnumType.STRING)
    @ToString.Include
    private CashOperationType type;

    @Column
    @ToString.Include
    private String paymentMethod;

    @Column(nullable = false)
    @ToString.Include
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    @ToString.Include
    private BigDecimal amount;

    @ToString.Include
    private LocalDateTime createdAt;
}
