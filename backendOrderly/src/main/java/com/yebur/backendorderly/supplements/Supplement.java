package com.yebur.backendorderly.supplements;

import com.yebur.backendorderly.category.Category;
import com.yebur.backendorderly.product.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="supplements")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Supplement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column
    private BigDecimal price;

    @ManyToMany
    @JoinTable(
            name = "categories_supplements",
            joinColumns = @JoinColumn(name = "supplement_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "products_supplements",
            joinColumns = @JoinColumn(name = "supplement_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products = new ArrayList<>();
}
