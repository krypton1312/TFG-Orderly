package com.yebur.backendorderly.order;

import java.util.List;
import java.util.Optional;

public interface OrderServiceInterface {

    List<OrderResponse> findAllOrderDTO();

    List<OrderResponse> findAllOrderDTOByStatus(OrderStatus status);

    Optional<Order> findById(Long id);
    
    Optional<OrderResponse> findOrderDTOById(Long id);

    OrderResponse createOrder(OrderRequest orderRequest);

    Order updateOrder(Long id, OrderRequest orderRequest);

    void deleteOrder(Long id);
}
