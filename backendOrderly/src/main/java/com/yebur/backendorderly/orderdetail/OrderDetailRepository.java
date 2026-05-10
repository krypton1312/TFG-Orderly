package com.yebur.backendorderly.orderdetail;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.yebur.backendorderly.cashsessions.CashSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    @Override
    List<OrderDetail> findAll();
    
    List<OrderDetail> findAllByOrderId(Long orderId);
    
    @Query("SELECT new com.yebur.backendorderly.orderdetail.OrderDetailResponse(od.id, od.product.id, od.name, od.order.id, od.comment, od.amount, od.unitPrice, od.status, od.paymentMethod, od.createdAt, od.product.destination, od.batchId, od.paid) FROM OrderDetail od")
    List<OrderDetailResponse> findAllOrderDetailDTO();

    @Query("SELECT new com.yebur.backendorderly.orderdetail.OrderDetailResponse(od.id, od.product.id, od.name, od.order.id, od.comment, od.amount, od.unitPrice, od.status, od.paymentMethod, od.createdAt, od.product.destination, od.batchId, od.paid) FROM OrderDetail od WHERE od.order.id = :orderId")
    List<OrderDetailResponse> findAllOrderDetailDTOByOrderId(Long orderId);

    @Query("SELECT new com.yebur.backendorderly.orderdetail.OrderDetailResponse(od.id, od.product.id, od.name, od.order.id, od.comment, od.amount, od.unitPrice, od.status, od.paymentMethod, od.createdAt, od.product.destination, od.batchId, od.paid) FROM OrderDetail od WHERE od.order.id = :orderId AND od.paid = false")
    List<OrderDetailResponse> findUnpaidOrderDetailDTOByOrderId(Long orderId);

    @Query("SELECT new com.yebur.backendorderly.orderdetail.OrderDetailResponse(od.id, od.product.id, od.name, od.order.id, od.comment, od.amount, od.unitPrice, od.status, od.paymentMethod, od.createdAt, od.product.destination, od.batchId, od.paid) FROM OrderDetail od WHERE od.order.id = :orderId AND od.paid = false AND od.status <> 'SERVED'")
    List<OrderDetailResponse> findOrderDetailTablet(Long orderId);


    @Override
    Optional<OrderDetail> findById(Long id);

    @Query("SELECT new com.yebur.backendorderly.orderdetail.OrderDetailResponse(od.id, od.product.id, od.name, od.order.id, od.comment, od.amount, od.unitPrice, od.status, od.paymentMethod, od.createdAt, od.product.destination, od.batchId, od.paid) FROM OrderDetail od WHERE od.id = :id")
    Optional<OrderDetailResponse> findOrderDetailDTOById(Long id);

    boolean existsByOrderId(Long orderId);

    boolean existsByOrderIdAndStatusNot(Long orderId, OrderDetailStatus status);

    boolean existsByOrderIdAndStatus(Long orderId, OrderDetailStatus status);

    boolean existsByOrderIdAndPaid(Long orderId, boolean paid);

    @Query("""
    SELECT COALESCE(SUM(od.amount * od.unitPrice), 0)
    FROM OrderDetail od
    WHERE od.cashSession.id = :cashSessionId
    AND od.paymentMethod = :paymentMethod
    AND od.paid = true
    """)
    BigDecimal getPaidSalesByCashSessionAndPaymentMethod(Long cashSessionId,String paymentMethod);


    @Query("""
    SELECT COALESCE(SUM(od.amount * od.unitPrice), 0)
    FROM OrderDetail od
    WHERE od.cashSession.id = :cashSessionId
    AND od.paid = true
    """)
    BigDecimal getPaidSalesByCashSession(Long cashSessionId);

}
