package com.yebur.backendorderly.entities;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
    
    @ManyToMany
    @JoinTable(
        name = "orderdetails_products",
        joinColumns = @JoinColumn(name = "id_product"),
        inverseJoinColumns = @JoinColumn(name = "id_orderdetail")
    )
    private Product product;

    @ManyToOne
    @JoinColumn(name = "id_order", nullable = false)
    private List<Order> order;

    private String comment;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private double unitPrice;

}
