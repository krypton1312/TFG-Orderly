package com.yebur.backendorderly.supplements;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplementRequest {
    private String name;
    private BigDecimal price;
    private List<Long> categories_ids;
    private List<Long> products_ids;
}
