package com.yebur.backendorderly.orderdetail;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yebur.backendorderly.order.Order;
import com.yebur.backendorderly.order.OrderService;
import com.yebur.backendorderly.product.Product;
import com.yebur.backendorderly.product.ProductService;

@Service("orderDetailService")
public class OrderDetailService implements com.yebur.backendorderly.orderdetail.OrderDetailServiceInterface {

    private final OrderDetailRepository orderDetailRepository;
    private final ProductService productService;
    private final OrderService orderService;

    public OrderDetailService(OrderDetailRepository orderDetailRepository,
                              ProductService productService,
                              OrderService orderService) {
        this.orderDetailRepository = orderDetailRepository;
        this.productService = productService;
        this.orderService = orderService;
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
    public OrderDetailResponse createOrderDetail(OrderDetailRequest dto) {
        OrderDetail orderDetail = mapToEntity(dto);
        orderDetail.setCreatedAt(LocalDateTime.now());
        OrderDetail saved = orderDetailRepository.save(orderDetail);
        return mapToResponse(saved);
    }

    @Override
    public OrderDetailResponse updateOrderDetail(Long id, OrderDetailRequest dto) {
        OrderDetail existing = orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderDetail not found with id " + id));

        OrderDetail updated = mapToEntity(dto);
        existing.setAmount(updated.getAmount());
        existing.setUnitPrice(updated.getUnitPrice());
        existing.setProduct(updated.getProduct());
        existing.setOrder(updated.getOrder());
        existing.setComment(updated.getComment());
        existing.setCreatedAt(updated.getCreatedAt());

        OrderDetail saved = orderDetailRepository.save(existing);
        return mapToResponse(saved);
    }

    @Override
    public void deleteOrderDetail(Long id) {
        orderDetailRepository.deleteById(id);
    }

    private OrderDetail mapToEntity(OrderDetailRequest dto) {
        Product product = productService.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id " + dto.getProductId()));

        Order order = orderService.findById(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id " + dto.getOrderId()));

        OrderDetail detail = new OrderDetail();
        detail.setProduct(product);
        detail.setOrder(order);
        detail.setAmount(dto.getAmount());
        detail.setUnitPrice(dto.getUnitPrice());
        detail.setComment(dto.getComment());
        return detail;
    }

    private OrderDetailResponse mapToResponse(OrderDetail entity) {
        return new OrderDetailResponse(
                entity.getId(),
                entity.getProduct().getId(),
                entity.getOrder().getId(),
                entity.getComment(),
                entity.getAmount(),
                entity.getUnitPrice()
        );
    }
}
