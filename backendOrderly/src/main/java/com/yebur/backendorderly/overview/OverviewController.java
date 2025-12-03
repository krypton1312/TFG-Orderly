package com.yebur.backendorderly.overview;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/overview")
@RequiredArgsConstructor
public class OverviewController {

    private final OverviewService overviewService;

    @GetMapping
    public ResponseEntity<List<TableWithOrderResponse>> getOverview() {
        return ResponseEntity.ok(overviewService.getOverview());
    }

    @GetMapping("/tablet")
    public ResponseEntity<List<OrderWithOrderDetailResponse>> getOverviewTablet() {
        return ResponseEntity.ok(overviewService.getOrderWithOrderDetails());
    }

    @GetMapping("/products-with-supplements-by-category/id/{id}")
    public ResponseEntity<ProductsWithSupplements> getOverviewProducts(@PathVariable Long id) {
        return ResponseEntity.ok(overviewService.findProductsWithSupplementsByCategory(id));
    }
    
}
