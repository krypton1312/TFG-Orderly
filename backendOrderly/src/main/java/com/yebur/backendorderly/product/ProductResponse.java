package com.yebur.backendorderly.product;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductResponse {

    private Long id;

    private String name;

    private BigDecimal price;

    private Integer stock;

    private Long categoryId;

    private String destination;

    public ProductResponse(Long id, String name, BigDecimal price, Integer stock, Long categoryId, ProductDestination destination) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
        this.destination = destinationMapped(destination);
    }

    private String destinationMapped(ProductDestination destination) {
        if (destination == null) {
            return null;
        }
        return switch (destination) {
            case BAR -> "Barra";
            case DRINKS -> "Bebidas";
            case KITCHEN -> "Cocina";
        };
    }

}
