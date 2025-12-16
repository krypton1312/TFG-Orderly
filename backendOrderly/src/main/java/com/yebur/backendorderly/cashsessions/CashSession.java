package com.yebur.backendorderly.cashsessions;

import com.yebur.backendorderly.cashoperations.CashOperations;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "cash_sessions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"business_date","shift_no"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate businessDate;

    private Integer shiftNo;

    @Column(nullable = false)
    private LocalDateTime openedAt;

    @Column
    private LocalDateTime closedAt;

    @Column(precision = 12, scale = 2)
    private BigDecimal cashStart;

    @Column(precision = 12, scale = 2)
    private BigDecimal cashEndExpected;

    @Column(precision = 12, scale = 2)
    private BigDecimal cashEndActual;

    @Column(precision = 12, scale = 2)
    private BigDecimal difference;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalSalesCash;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalSalesCard;

    @Column
    @Enumerated(EnumType.STRING)
    private CashSessionStatus status;


    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CashOperations> operations = new ArrayList<>();

}
