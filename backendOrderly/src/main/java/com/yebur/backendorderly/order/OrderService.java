package com.yebur.backendorderly.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yebur.backendorderly.resttable.RestTableRepository;
import com.yebur.backendorderly.resttable.RestTableResponse;
import com.yebur.backendorderly.resttable.RestTableService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService implements OrderServiceInterface {

    private final OrderRepository orderRepository;
    private final RestTableRepository restTableRepository;
    private final RestTableService restTableService;

    @Override
    public List<OrderResponse> findAllOrderDTO() {
        return orderRepository.findAllOrderDTO().stream()
                .map(this::mapWithTable)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> findAllOrderDTOByStatus(OrderStatus status) {
        return orderRepository.findAllOrderDTO().stream()
                .filter(o -> o.getState().equalsIgnoreCase(status.name()))
                .map(this::mapWithTable)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public Optional<OrderResponse> findOrderDTOById(Long id) {
        return orderRepository.findOrderDTOById(id)
                .map(this::mapWithTable);
    }

    @Override
    public OrderResponse createOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setState(OrderStatus.valueOf(orderRequest.getState().toUpperCase()));
        order.setPaymentMethod(Objects.requireNonNullElse(orderRequest.getPaymentMethod(), "N/A"));
        order.setTotal(Objects.requireNonNullElse(orderRequest.getTotal(), 0.0));
        order.setDatetime(LocalDateTime.now());

        // order.setEmployee(...);
        // order.setClient(...);
        order.setRestTable(orderRequest.getIdTable() != null
                ? restTableRepository.findById(orderRequest.getIdTable())
                        .orElseThrow(() -> new RuntimeException("Table not found"))
                : null);
        
        OrderResponse savedOrder = findOrderDTOById(orderRepository.save(order).getId())
                .orElseThrow(() -> new RuntimeException("Error creating order"));
        return mapWithTable(savedOrder);
    }

    @Override
    public Order updateOrder(Long id, OrderRequest orderRequest) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id " + id));

        order.setState(OrderStatus.valueOf(orderRequest.getState().toUpperCase()));
        order.setPaymentMethod(orderRequest.getPaymentMethod());
        order.setTotal(orderRequest.getTotal());

        // Привязываем стол, если передан
        if (orderRequest.getIdTable() != null) {
            order.setRestTable(restTableRepository.findById(orderRequest.getIdTable())
                    .orElseThrow(() -> new RuntimeException("Table not found")));
        } else {
            order.setRestTable(null);
        }

        return orderRepository.save(order);
    }

    @Override
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found with id " + id);
        }
        orderRepository.deleteById(id);
    }

    private OrderResponse mapWithTable(OrderResponse order) {
        RestTableResponse tableResponse = null;

        if (order.getRestTable() != null && order.getRestTable().getId() != null) {
            tableResponse = restTableService
                    .findRestTableDTOById(order.getRestTable().getId())
                    .orElse(null);
        }

        return new OrderResponse(
                order.getId(),
                order.getDatetime(),
                order.getState(),
                order.getPaymentMethod(),
                order.getTotal(),
                order.getIdEmployee(),
                order.getIdClient(),
                tableResponse);
    }
}
