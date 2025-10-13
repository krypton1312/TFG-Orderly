package com.yebur.backendorderly.orderdetail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/orderDetails")
public class OrderDetailController {

    private final OrderDetailService orderDetailService;

    public OrderDetailController(OrderDetailService orderDetailService) {
        this.orderDetailService = orderDetailService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDetailResponse>> getAllOrderDetails() {
        return ResponseEntity.ok(orderDetailService.findAllOrderDetailDTO());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponse> getOrderDetailById(@PathVariable Long id) {
        return orderDetailService.findOrderDetailDTOById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderDetailResponse>> getOrderDetailsByOrderId(@PathVariable Long orderId) {
        List<OrderDetailResponse> details = orderDetailService.findAllOrderDetailDTOByOrderId(orderId);
        if (details.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(details);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody OrderDetailRequest dto, BindingResult result) {
        if (result.hasErrors()) return validation(result);
        try {
            OrderDetailResponse response = orderDetailService.createOrderDetail(dto);
            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody OrderDetailRequest dto, BindingResult result) {
        if (result.hasErrors()) return validation(result);
        try {
            OrderDetailResponse response = orderDetailService.updateOrderDetail(id, dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    private ResponseEntity<?> validation(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(error ->
            errors.put(error.getField(), "El campo: " + error.getField() + " " + error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }
}
