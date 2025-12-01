package com.yebur.backendorderly.supplements;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class SupplementRequest {
    private String name;
    private BigDecimal price;
    private List<Long> categories_ids;
    private List<Long> products_ids;

    public SupplementRequest(String name, BigDecimal price, List<Long> products_ids, List<Long> categories_ids) {
        this.name = name;
        this.price = price;
        this.products_ids = products_ids;
        this.categories_ids = categories_ids;
    }

}
