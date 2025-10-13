package com.yebur.backendorderly.orderdetail;

import java.util.List;
import java.util.Optional;

public interface OrderDetailServiceInterface {
    List<OrderDetail> findAll();

    List<OrderDetailResponse> findAllOrderDetailDTO();

    List<OrderDetailResponse> findAllOrderDetailDTOByOrderId(Long orderId);

    Optional<OrderDetail> findById(Long id);

    Optional<OrderDetailResponse> findOrderDetailDTOById(Long id);

    OrderDetailResponse createOrderDetail(OrderDetailRequest orderDetail);

    OrderDetailResponse updateOrderDetail(Long id, OrderDetailRequest orderDetail);

    void deleteOrderDetail(Long id);
}
