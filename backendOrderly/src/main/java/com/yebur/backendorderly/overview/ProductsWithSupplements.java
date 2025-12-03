package com.yebur.backendorderly.overview;

import com.yebur.backendorderly.product.ProductResponse;
import com.yebur.backendorderly.supplements.SupplementResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductsWithSupplements {
    List<ProductResponse> products;
    List<SupplementResponse> supplements;
}
