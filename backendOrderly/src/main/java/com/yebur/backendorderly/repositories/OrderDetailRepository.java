package com.yebur.backendorderly.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yebur.backendorderly.dto.output.OrderDetailResponse;
import com.yebur.backendorderly.entities.OrderDetail;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    @Override
    List<OrderDetail> findAll();
    
    @Query("SELECT new com.yebur.backendorderly.dto.output.OrderDetailResponse(od.id, od.product.id, od.order.id, od.comment, od.amount, od.unitPrice) FROM OrderDetail od")
    List<OrderDetailResponse> findAllOrderDetailDTO();

    @Query("SELECT new com.yebur.backendorderly.dto.output.OrderDetailResponse(od.id, od.product.id, od.order.id, od.comment, od.amount, od.unitPrice) FROM OrderDetail od WHERE od.order.id = :orderId")
    List<OrderDetailResponse> findAllOrderDetailDTOByOrderId(Long orderId);

    @Override
    Optional<OrderDetail> findById(Long id);

    @Query("SELECT new com.yebur.backendorderly.dto.output.OrderDetailResponse(od.id, od.product.id, od.order.id, od.comment, od.amount, od.unitPrice) FROM OrderDetail od WHERE od.id = :id")
    Optional<OrderDetailResponse> findOrderDetailDTOById(Long id);


}
