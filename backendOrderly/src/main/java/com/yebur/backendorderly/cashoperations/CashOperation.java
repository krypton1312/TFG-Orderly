package com.yebur.backendorderly.cashoperations;

import com.yebur.backendorderly.cashsessions.CashSession;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_operations")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CashOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private CashSession session;

    @Column
    @Enumerated(EnumType.STRING)
    private CashOperationType type;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    private LocalDateTime createdAt;
}
