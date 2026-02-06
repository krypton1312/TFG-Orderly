package com.yebur.backendorderly.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.yebur.backendorderly.orderdetail.OrderDetailStatus;
import com.yebur.backendorderly.resttable.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yebur.backendorderly.websocket.WsEvent;
import com.yebur.backendorderly.websocket.WsEventType;
import com.yebur.backendorderly.websocket.WsNotifier;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService implements OrderServiceInterface {

    private final OrderRepository orderRepository;
    private final RestTableRepository restTableRepository;
    private final RestTableService restTableService;
    private final WsNotifier wsNotifier;

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

        BigDecimal total = Optional.ofNullable(orderRequest.getTotal())
                .orElse(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        order.setTotal(total);

        order.setDatetime(LocalDateTime.now());

        if(orderRequest.getIdTable() != null) {
            RestTable table = restTableRepository.findById(orderRequest.getIdTable()).orElseThrow(() -> new RuntimeException("Table not found"));
            order.setRestTable(table);
            table.setStatus(TableStatus.OCCUPIED);
            restTableRepository.save(table);
        }

        Order saved = orderRepository.save(order);

        notifyOrderChanged(WsEventType.ORDER_CREATED, saved);

        return findOrderDTOById(saved.getId())
                .map(this::mapWithTable)
                .orElseThrow(() -> new RuntimeException("Error creating order"));
    }

    @Override
    public Order updateOrder(Long id, OrderRequest orderRequest) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id " + id));

        order.setState(OrderStatus.valueOf(orderRequest.getState().toUpperCase()));
        order.setPaymentMethod(orderRequest.getPaymentMethod());

        BigDecimal total = Optional.ofNullable(orderRequest.getTotal())
                .orElse(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        order.setTotal(total);

        if (orderRequest.getIdTable() != null) {
            order.setRestTable(restTableRepository.findById(orderRequest.getIdTable())
                    .orElseThrow(() -> new RuntimeException("Table not found")));
        } else {
            order.setRestTable(null);
        }

        Order saved = orderRepository.save(order);

        isAllOrderDetailsPaid(saved.getId());

        notifyOrderChanged(WsEventType.ORDER_TOTAL_CHANGED, saved);

        return saved;
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id " + id));

        // Проверяем, есть ли оплаченные детали
        boolean hasPaidDetails = order.getOrderDetails().stream()
                .anyMatch(detail -> detail.getStatus() == OrderDetailStatus.PAID);

        if (!hasPaidDetails) {
            // Если оплаченных деталей нет, удаляем весь заказ целиком.
            // CascadeType.ALL + orphanRemoval обеспечат удаление всех OrderDetail в БД перед удалением Order.
            orderRepository.delete(order);

            if (order.getRestTable() != null) {
                RestTable table = order.getRestTable();
                table.setStatus(TableStatus.AVAILABLE);
                restTableRepository.save(table);
            }

            notifyOrderChanged(WsEventType.ORDER_DELETED, order);
        } else {
            // Если есть оплаченные детали, удаляем только неоплаченные.
            order.getOrderDetails().removeIf(detail -> detail.getStatus() != OrderDetailStatus.PAID);

            BigDecimal newTotal = order.getOrderDetails().stream()
                    .map(od -> od.getUnitPrice().multiply(BigDecimal.valueOf(od.getAmount())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            order.setTotal(newTotal);
            order.setState(OrderStatus.PAID);

            if (order.getRestTable() != null) {
                RestTable table = order.getRestTable();
                table.setStatus(TableStatus.AVAILABLE);
                restTableRepository.save(table);
            }

            orderRepository.save(order);
            notifyOrderChanged(WsEventType.ORDER_TOTAL_CHANGED, order);
        }
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
                order.getTotal() != null ? order.getTotal().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                order.getIdEmployee(),
                order.getIdClient(),
                tableResponse);
    }

    // 🔹 вспомогательный метод для уведомлений WS
    private void notifyOrderChanged(WsEventType type, Order order) {
        WsEvent event = new WsEvent(
                type,
                order.getId(),
                null,
                null,
                null,
                null           
        );
        wsNotifier.send(event);
    }

    private void isAllOrderDetailsPaid(Long orderId) {
           Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id " + orderId));

           if(order.getOrderDetails().stream().allMatch(odetail -> odetail.getStatus() == OrderDetailStatus.PAID)) {
               order.setState(OrderStatus.PAID);
               order.getRestTable().setStatus(TableStatus.AVAILABLE);
               orderRepository.save(order);
           }
    }
}
