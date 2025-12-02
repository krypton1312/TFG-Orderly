package com.yebur.backendorderly.category;

import java.util.ArrayList;
import java.util.List;

import com.yebur.backendorderly.product.Product;

import com.yebur.backendorderly.supplements.Supplement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String color;

    @Column
    private Integer index;

    @OneToMany(mappedBy = "category")
    private List<Product> products;

    @ManyToMany(mappedBy = "categories")
    private List<Supplement> supplements = new ArrayList<>();

}
