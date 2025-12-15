package com.yebur.backendorderly.cashsessions;

import com.yebur.backendorderly.cashoperations.CashOperations;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cash_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime openedAt;

    @Column
    private LocalDateTime closedAt;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cashStart;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cashEndExpected;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cashEndActual;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal difference;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSalesCash;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSalesCard;

    @Column
    @Enumerated(EnumType.STRING)
    private CashSessionStatus status;


    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CashOperations> operations = new ArrayList<>();

}
