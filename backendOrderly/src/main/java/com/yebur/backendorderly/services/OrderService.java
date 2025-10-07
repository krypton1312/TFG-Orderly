package com.yebur.backendorderly.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yebur.backendorderly.dto.input.OrderRequest;
import com.yebur.backendorderly.dto.output.OrderDetailResponse;
import com.yebur.backendorderly.dto.output.OrderResponse;
import com.yebur.backendorderly.entities.Order;
import com.yebur.backendorderly.entities.OrderDetail;
import com.yebur.backendorderly.enums.OrderStatus;
import com.yebur.backendorderly.repositories.OrderDetailRepository;
import com.yebur.backendorderly.repositories.OrderRepository;
import com.yebur.backendorderly.repositories.ProductRepository;
import com.yebur.backendorderly.services.interfaces.OrderServiceInterface;

@Service("orderService")
public class OrderService implements OrderServiceInterface {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, OrderDetailRepository orderDetailRepository,
            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<OrderResponse> findAllOrderDTO() {
        return orderRepository.findAllOrderDTO();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public Optional<OrderResponse> findOrderDTOById(Long id) {
        return orderRepository.findOrderDTOById(id);
    }

    @Override
    public Order createOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setState(OrderStatus.valueOf(orderRequest.getState().toUpperCase()));
        order.setPaymentMethod(orderRequest.getPaymentMethod() != null ? orderRequest.getPaymentMethod() : "N/A");
        order.setTotal(Objects.requireNonNullElse(orderRequest.getTotal(), 0.0));
        // order.setEmployee(orderRequest.getEmployee());
        // order.setClient(orderRequest.getClient());
        // order.setRestTable(orderRequest.getRestTable());
        order.setDatetime(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    public Order updateOrder(Long id, OrderRequest order) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id " + id));

        existingOrder.setState(OrderStatus.valueOf(order.getState()));
        existingOrder.setPaymentMethod(order.getPaymentMethod());
        existingOrder.setTotal(order.getTotal());
        // existingOrder.setEmployee(order.());
        // existingOrder.setClient(order.getClient());
        // existingOrder.setRestTable(order.getRestTable());
        List<OrderDetail> details = order.getOrderDetails().stream()
                .map(d -> {
                    OrderDetail detail = new OrderDetail();
                    detail.setOrder(existingOrder);
                    detail.setProduct(productRepository.findById(d.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found with id " + d.getProductId())));
                    detail.setComment(d.getComment());
                    detail.setAmount(d.getAmount());
                    detail.setUnitPrice(d.getUnitPrice());
                    return detail;
                })
                .collect(Collectors.toList());

        existingOrder.setOrderDetails(details);

        return orderRepository.save(existingOrder);
    }

    @Override
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found with id " + id);
        }
        orderRepository.deleteById(id);
    }
}
