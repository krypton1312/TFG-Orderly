package com.yebur.backendorderly.orderdetail;

import java.time.LocalDateTime;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="order_details")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
        name = "order_details_products",
        joinColumns = @JoinColumn(name = "id_product"),
        inverseJoinColumns = @JoinColumn(name = "id_order_detail")
    )
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_order", nullable = false)
    private Order order;

    @Column
    private String comment;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private double unitPrice;
    
    @Column
    private LocalDateTime createdAt;

    @Column
    @Enumerated(EnumType.STRING) 
    private OrderDetailStatus status;

}
