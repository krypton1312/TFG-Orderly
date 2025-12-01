package com.yebur.backendorderly.supplements;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplementResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private List<CategoryResponseSummary> categories;
    private List<ProductResponseSummary> products;

}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CategoryResponseSummary{
    private Long id;
    private String name;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ProductResponseSummary{
    private Long id;
    private String name;
}
