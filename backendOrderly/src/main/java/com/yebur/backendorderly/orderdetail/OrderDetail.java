package com.yebur.backendorderly.orderdetail;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.yebur.backendorderly.cashsessions.CashSession;
import com.yebur.backendorderly.order.Order;
import com.yebur.backendorderly.product.Product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="order_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class OrderDetail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
        name = "order_details_products",
        joinColumns = @JoinColumn(name = "id_product"),
        inverseJoinColumns = @JoinColumn(name = "id_order_detail")
    )
    private Product product;

    @ToString.Include
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_order", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cash_session")
    private CashSession cashSession;

    @Column
    @ToString.Include
    private String comment;

    @Column(nullable = false)
    @ToString.Include
    private int amount;

    @Column(nullable = false, precision = 10, scale = 2)
    @ToString.Include
    private BigDecimal unitPrice;
    
    @Column
    @ToString.Include
    private LocalDateTime createdAt;

    @Column
    @Enumerated(EnumType.STRING) 
    @ToString.Include
    private OrderDetailStatus status;

    @Column
    @ToString.Include
    private String paymentMethod;

    @Column
    @ToString.Include
    private String batchId;

    @PrePersist
    public void prePersist(){
        if(batchId == null){
            batchId = UUID.randomUUID().toString();
        }
    }
}
