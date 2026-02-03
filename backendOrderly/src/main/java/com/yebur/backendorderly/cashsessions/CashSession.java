package com.yebur.backendorderly.cashsessions;

import com.yebur.backendorderly.cashoperations.CashOperation;
import com.yebur.backendorderly.orderdetail.OrderDetail;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class CashSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ToString.Include
    private LocalDate businessDate;

    @ToString.Include
    private Integer shiftNo;

    @Column(nullable = false)
    @ToString.Include
    private LocalDateTime openedAt;

    @Column
    @ToString.Include
    private LocalDateTime closedAt;

    @Column(precision = 12, scale = 2)
    @ToString.Include
    private BigDecimal cashStart;

    @Column(precision = 12, scale = 2)
    @ToString.Include
    private BigDecimal cashEndExpected;

    @Column(precision = 12, scale = 2)
    @ToString.Include
    private BigDecimal cashEndActual;

    @Column(precision = 12, scale = 2)
    @ToString.Include
    private BigDecimal difference;

    @Column(precision = 12, scale = 2)
    @ToString.Include
    private BigDecimal totalSalesCash;

    @Column(precision = 12, scale = 2)
    @ToString.Include
    private BigDecimal totalSalesCard;

    @Column
    @Enumerated(EnumType.STRING)
    @ToString.Include
    private CashSessionStatus status;

    @OneToMany(mappedBy = "cashSession")
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CashOperation> operations = new ArrayList<>();

}
