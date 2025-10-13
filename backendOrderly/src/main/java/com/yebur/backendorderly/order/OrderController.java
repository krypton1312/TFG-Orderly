package com.yebur.backendorderly.order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yebur.backendorderly.orderdetail.OrderDetailResponse;
import com.yebur.backendorderly.orderdetail.OrderDetailService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderDetailService orderDetailService;

    public OrderController(OrderService orderService, OrderDetailService orderDetailService) {
        this.orderService = orderService;
        this.orderDetailService = orderDetailService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        try {
            return ResponseEntity.ok(orderService.findAllOrderDTO());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("{id}/details")
    public ResponseEntity<List<OrderDetailResponse>> getOrderDetailsByOrderId(@PathVariable Long id) {
        List<OrderDetailResponse> orderDetails = orderDetailService.findAllOrderDetailDTOByOrderId(id);
        if (orderDetails.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(orderDetails);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            return orderService.findOrderDTOById(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest, BindingResult result) {
        if (result.hasErrors()) {
            return validation(result);
        }
        try {
            Order order = orderService.createOrder(orderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(orderService.findOrderDTOById(order.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, OrderRequest orderRequest, BindingResult result) {
        if (result.hasErrors()) {
            return validation(result);
        }
        try {
            if (!orderService.findById(id).isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order is not found with this id: " + id);
            }
            orderService.updateOrder(id, orderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(orderRequest);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            if (!orderService.findById(id).isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order is not found with this id: " + id);
            }
            orderService.deleteOrder(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private ResponseEntity<?> validation(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(error -> {
            errors.put(error.getField(), "El campo: " + error.getField() + " " + error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
