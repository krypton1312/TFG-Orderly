package com.yebur.backendorderly.product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.yebur.backendorderly.category.Category;
import com.yebur.backendorderly.orderdetail.OrderDetail;

import com.yebur.backendorderly.supplements.Supplement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column
    private int stock;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_category", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @Column
    @Enumerated(EnumType.STRING)
    private ProductDestination destination;

    @ManyToMany(mappedBy = "products")
    private List<Supplement> supplements = new ArrayList<>();
}
