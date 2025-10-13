package com.yebur.backendorderly.product;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank
    private String name;

    @Positive
    private Double price;

    @Min(0)
    private Integer stock;
    
    @NotNull
    private Long categoryId;
}
