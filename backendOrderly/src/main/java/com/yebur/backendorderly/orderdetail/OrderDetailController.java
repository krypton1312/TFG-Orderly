package com.yebur.backendorderly.orderdetail;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        if (details.isEmpty() || details == null){
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(details);
    }

    @GetMapping("order/{orderId}/unpaid")
    public ResponseEntity<List<OrderDetailResponse>> getUnpaidOrderDetailByOrderId(@PathVariable Long orderId){
        List<OrderDetailResponse> details = orderDetailService.findUnpaidOrderDetailDTOByOrderId(orderId);
        if (details.isEmpty() || details == null){
            return ResponseEntity.ok(Collections.emptyList());
        }
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

    @PutMapping("change-status/{status}")
    public void updateStatus(@RequestBody List<Long> ids, @PathVariable String status){
        orderDetailService.updateOrderDetailStatus(ids, status);
    }
    

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            orderDetailService.deleteOrderDetail(id);
            return ResponseEntity.noContent().build();
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
