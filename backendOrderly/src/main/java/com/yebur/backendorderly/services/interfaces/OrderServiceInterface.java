package com.yebur.backendorderly.services.interfaces;

import java.util.List;
import java.util.Optional;

import com.yebur.backendorderly.dto.input.OrderRequest;
import com.yebur.backendorderly.dto.output.OrderResponse;
import com.yebur.backendorderly.entities.Order;

public interface OrderServiceInterface {

    List<OrderResponse> findAllOrderDTO();
    
    Optional<OrderResponse> findOrderDTOById(Long id);

    Order createOrder(Order order);

    Order updateOrder(Long id, OrderRequest order);

    void deleteOrder(Long id);
}
