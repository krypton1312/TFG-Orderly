package com.yebur.backendorderly.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yebur.backendorderly.dto.output.OrderDetailResponse;
import com.yebur.backendorderly.entities.OrderDetail;
import com.yebur.backendorderly.repositories.OrderDetailRepository;

@Service("orderDetailService")
public class OrderDetailService implements com.yebur.backendorderly.services.interfaces.OrderDetailInterface {

    private final OrderDetailRepository orderDetailRepository;

    public OrderDetailService(OrderDetailRepository orderDetailRepository) {
        this.orderDetailRepository = orderDetailRepository;
    }

    @Override
    public List<OrderDetail> findAll() {
        return orderDetailRepository.findAll();
    }

    public List<OrderDetailResponse> findAllOrderDetailDTO() {
        return orderDetailRepository.findAllOrderDetailDTO();
    }

    public List<OrderDetailResponse> findAllOrderDetailDTOByOrderId(Long orderId) {
        return orderDetailRepository.findAllOrderDetailDTOByOrderId(orderId);
    }

    public Optional<OrderDetailResponse> findOrderDetailDTOById(Long id) {
        return orderDetailRepository.findOrderDetailDTOById(id);
    }

    @Override
    public Optional<OrderDetail> findById(Long id) {
        return orderDetailRepository.findById(id);
    }

    @Override
    public OrderDetail createOrderDetail(OrderDetail orderDetail) {
        return orderDetailRepository.save(orderDetail);
    }

    @Override
    public OrderDetail updateOrderDetail(Long id, OrderDetail orderDetail) {
        OrderDetail existingOrderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderDetail not found with id " + id));

        existingOrderDetail.setAmount(orderDetail.getAmount());
        existingOrderDetail.setUnitPrice(orderDetail.getUnitPrice());
        existingOrderDetail.setProduct(orderDetail.getProduct());
        existingOrderDetail.setOrder(orderDetail.getOrder());
        existingOrderDetail.setComment(orderDetail.getComment());
        existingOrderDetail.setCreatedAt(orderDetail.getCreatedAt());

        return orderDetailRepository.save(existingOrderDetail);
    }

    @Override
    public void deleteOrderDetail(Long id) {
        orderDetailRepository.deleteById(id);
    }

}
