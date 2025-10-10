package com.yebur.backendorderly.services.interfaces;

import java.util.List;
import java.util.Optional;

import com.yebur.backendorderly.dto.input.OrderDetailRequest;
import com.yebur.backendorderly.dto.output.OrderDetailResponse;
import com.yebur.backendorderly.entities.OrderDetail;

public interface OrderDetailInterface {
    List<OrderDetail> findAll();

    List<OrderDetailResponse> findAllOrderDetailDTO();

    List<OrderDetailResponse> findAllOrderDetailDTOByOrderId(Long orderId);

    Optional<OrderDetail> findById(Long id);

    Optional<OrderDetailResponse> findOrderDetailDTOById(Long id);

    OrderDetailResponse createOrderDetail(OrderDetailRequest orderDetail);

    OrderDetailResponse updateOrderDetail(Long id, OrderDetailRequest orderDetail);

    void deleteOrderDetail(Long id);
}
