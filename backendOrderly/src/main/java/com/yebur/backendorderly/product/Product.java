package com.yebur.backendorderly.product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.yebur.backendorderly.category.Category;
import com.yebur.backendorderly.orderdetail.OrderDetail;

import com.yebur.backendorderly.supplements.Supplement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "products")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;
    
    @Column(nullable = false)
    @ToString.Include
    private String name;
    
    @Column(nullable = false, precision = 10, scale = 2)
    @ToString.Include
    private BigDecimal price;

    @Column
    @ToString.Include
    private int stock;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_category", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @Column
    @Enumerated(EnumType.STRING)
    @ToString.Include
    private ProductDestination destination;

    @ManyToMany(mappedBy = "products")
    private List<Supplement> supplements = new ArrayList<>();
}
