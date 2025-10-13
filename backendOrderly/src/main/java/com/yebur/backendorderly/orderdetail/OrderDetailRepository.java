package com.yebur.backendorderly.orderdetail;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    @Override
    List<OrderDetail> findAll();
    
    @Query("SELECT new com.yebur.backendorderly.orderdetail.OrderDetailResponse(od.id, od.product.id, od.order.id, od.comment, od.amount, od.unitPrice) FROM OrderDetail od")
    List<OrderDetailResponse> findAllOrderDetailDTO();

    @Query("SELECT new com.yebur.backendorderly.orderdetail.OrderDetailResponse(od.id, od.product.id, od.order.id, od.comment, od.amount, od.unitPrice) FROM OrderDetail od WHERE od.order.id = :orderId")
    List<OrderDetailResponse> findAllOrderDetailDTOByOrderId(Long orderId);

    @Override
    Optional<OrderDetail> findById(Long id);

    @Query("SELECT new com.yebur.backendorderly.orderdetail.OrderDetailResponse(od.id, od.product.id, od.order.id, od.comment, od.amount, od.unitPrice) FROM OrderDetail od WHERE od.id = :id")
    Optional<OrderDetailResponse> findOrderDetailDTOById(Long id);


}
