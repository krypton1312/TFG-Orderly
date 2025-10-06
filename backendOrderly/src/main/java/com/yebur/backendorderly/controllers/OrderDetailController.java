package com.yebur.backendorderly.controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yebur.backendorderly.dto.input.OrderDetailRequest;
import com.yebur.backendorderly.dto.output.OrderDetailResponse;
import com.yebur.backendorderly.entities.Order;
import com.yebur.backendorderly.entities.OrderDetail;
import com.yebur.backendorderly.entities.Product;
import com.yebur.backendorderly.services.OrderDetailService;
import com.yebur.backendorderly.services.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/orderDetails")
public class OrderDetailController {

    private final OrderDetailService orderDetailService;
    private final ProductService productService;
    private final OrderService orderService;

    public OrderDetailController(OrderDetailService orderDetailService, ProductService productService,
            OrderService orderService) {
        this.orderDetailService = orderDetailService;
        this.productService = productService;
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDetailResponse>> getAllOrderDetails() {
        List<OrderDetailResponse> orderDetails = orderDetailService.findAllOrderDetailDTO();
        return ResponseEntity.ok(orderDetails);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponse> getOrderDetailById(@PathVariable Long id) {
        return orderDetailService.findOrderDetailDTOById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderDetailResponse>> getOrderDetailsByOrderId(@PathVariable Long orderId) {
        List<OrderDetailResponse> orderDetails = orderDetailService.findAllOrderDetailDTOByOrderId(orderId);
        if (orderDetails.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(orderDetails);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody OrderDetailRequest dto, BindingResult result) {
        if (result.hasErrors())
            return validation(result);

        try {
            OrderDetail orderDetail = buildOrderDetail(dto);
            orderDetail.setCreatedAt(LocalDateTime.now());
            OrderDetail saved = orderDetailService.createOrderDetail(orderDetail);
            OrderDetailResponse response = new OrderDetailResponse(
                    saved.getProduct().getId(),
                    saved.getOrder().getId(),
                    saved.getComment(),
                    saved.getAmount(),
                    saved.getUnitPrice());
            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody OrderDetailRequest dto,
            BindingResult result) {
        if (result.hasErrors())
            return validation(result);

        try {
            OrderDetail updated = orderDetailService.updateOrderDetail(id, buildOrderDetail(dto));
            OrderDetailResponse response = new OrderDetailResponse(
                    updated.getProduct().getId(),
                    updated.getOrder().getId(),
                    updated.getComment(),
                    updated.getAmount(),
                    updated.getUnitPrice());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    private OrderDetail buildOrderDetail(OrderDetailRequest dto) {
        Product product = productService.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id " + dto.getProductId()));

        Order order = orderService.findById(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id " + dto.getOrderId()));

        OrderDetail detail = new OrderDetail();
        detail.setProduct(product);
        detail.setOrder(order);
        detail.setAmount(dto.getAmount());
        detail.setUnitPrice(dto.getUnitPrice());
        detail.setComment(dto.getComment());

        return detail;
    }

    private ResponseEntity<?> validation(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(error -> {
            errors.put(error.getField(), "El campo: " + error.getField() + " " + error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
