package com.yebur.backendorderly.services.interfaces;

import java.util.List;
import java.util.Optional;

import com.yebur.backendorderly.dto.input.OrderRequest;
import com.yebur.backendorderly.dto.output.OrderResponse;
import com.yebur.backendorderly.entities.Order;

public interface OrderServiceInterface {

    List<OrderResponse> findAllOrderDTO();

    Optional<Order> findById(Long id);
    
    Optional<OrderResponse> findOrderDTOById(Long id);

    Order createOrder(OrderRequest orderRequest);

    Order updateOrder(Long id, OrderRequest orderRequest);

    void deleteOrder(Long id);
}
