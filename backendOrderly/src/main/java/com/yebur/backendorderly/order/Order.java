package com.yebur.backendorderly.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.yebur.backendorderly.client.Client;
import com.yebur.backendorderly.employee.Employee;
import com.yebur.backendorderly.orderdetail.OrderDetail;
import com.yebur.backendorderly.resttable.RestTable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;
    
    @Column(nullable = false)
    @ToString.Include
    private LocalDateTime datetime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @ToString.Include
    private OrderStatus state;

    @Column
    @ToString.Include
    private String paymentMethod;

    @Column(nullable = false, precision = 10, scale = 2)
    @ToString.Include
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employee", nullable = true)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_restable")
    private RestTable restTable;

    @OneToMany(
            mappedBy = "order",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = false
    )
    private List<OrderDetail> orderDetails;

}
