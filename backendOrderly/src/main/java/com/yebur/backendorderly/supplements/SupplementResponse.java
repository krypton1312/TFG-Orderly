package com.yebur.backendorderly.supplements;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class SupplementResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private List<Long> categoriesId;
    private List<Long> productId;

    public SupplementResponse(Long id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

}
