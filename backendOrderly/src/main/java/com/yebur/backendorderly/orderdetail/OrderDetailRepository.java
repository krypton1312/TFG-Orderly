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
    
    List<OrderDetail> findAllByOrderId(Long orderId);
    
    @Query("SELECT new com.yebur.backendorderly.orderdetail.OrderDetailResponse(od.id, od.product.id, od.product.name, od.order.id, od.comment, od.amount, od.unitPrice, od.status, od.paymentMethod, od.createdAt, od.product.destination, od.batchId) FROM OrderDetail od")
    List<OrderDetailResponse> findAllOrderDetailDTO();

    @Query("SELECT new com.yebur.backendorderly.orderdetail.OrderDetailResponse(od.id, od.product.id, od.product.name, od.order.id, od.comment, od.amount, od.unitPrice, od.status, od.paymentMethod, od.createdAt, od.product.destination, od.batchId) FROM OrderDetail od WHERE od.order.id = :orderId")
    List<OrderDetailResponse> findAllOrderDetailDTOByOrderId(Long orderId);

    @Query("SELECT new com.yebur.backendorderly.orderdetail.OrderDetailResponse(od.id, od.product.id, od.product.name, od.order.id, od.comment, od.amount, od.unitPrice, od.status, od.paymentMethod, od.createdAt, od.product.destination, od.batchId) FROM OrderDetail od WHERE od.order.id = :orderId AND od.status <> 'PAID'")
    List<OrderDetailResponse> findUnpaidOrderDetailDTOByOrderId(Long orderId);

    @Query("SELECT new com.yebur.backendorderly.orderdetail.OrderDetailResponse(od.id, od.product.id, od.product.name, od.order.id, od.comment, od.amount, od.unitPrice, od.status, od.paymentMethod, od.createdAt, od.product.destination, od.batchId) FROM OrderDetail od WHERE od.order.id = :orderId AND od.status <> 'PAID' AND od.status <> 'SERVED'")
    List<OrderDetailResponse> findOrderDetailTablet(Long orderId);


    @Override
    Optional<OrderDetail> findById(Long id);

    @Query("SELECT new com.yebur.backendorderly.orderdetail.OrderDetailResponse(od.id, od.product.id, od.product.name, od.order.id, od.comment, od.amount, od.unitPrice, od.status, od.paymentMethod, od.createdAt, od.product.destination, od.batchId) FROM OrderDetail od WHERE od.id = :id")
    Optional<OrderDetailResponse> findOrderDetailDTOById(Long id);

    boolean existsByOrderIdAndStatusNot(Long orderId, OrderDetailStatus status);
}
