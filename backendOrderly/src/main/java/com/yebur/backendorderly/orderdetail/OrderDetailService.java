package com.yebur.backendorderly.orderdetail;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yebur.backendorderly.order.Order;
import com.yebur.backendorderly.order.OrderRequest;
import com.yebur.backendorderly.order.OrderService;
import com.yebur.backendorderly.product.Product;
import com.yebur.backendorderly.product.ProductService;

@Service("orderDetailService")
public class OrderDetailService implements OrderDetailServiceInterface {

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

        // Пересчёт total заказа после добавления позиции
        recalculateOrderTotal(saved.getOrder());

        return mapToResponse(saved);
    }

    @Override
    public OrderDetailResponse updateOrderDetail(Long id, OrderDetailRequest dto) {
        OrderDetail existing = orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderDetail not found with id " + id));

        existing.setAmount(dto.getAmount());
        existing.setUnitPrice(dto.getUnitPrice());
        existing.setComment(dto.getComment());

        Product product = productService.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id " + dto.getProductId()));
        existing.setProduct(product);

        Order order = orderService.findById(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id " + dto.getOrderId()));
        existing.setOrder(order);

        existing.setCreatedAt(LocalDateTime.now());

        OrderDetail saved = orderDetailRepository.save(existing);

        // Пересчёт total заказа после обновления позиции
        recalculateOrderTotal(saved.getOrder());

        return mapToResponse(saved);
    }

    @Override
    public void deleteOrderDetail(Long id) {
        OrderDetail detail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderDetail not found with id " + id));

        Order order = detail.getOrder();

        orderDetailRepository.delete(detail);

        // Пересчёт total заказа после удаления позиции
        recalculateOrderTotal(order);
    }

    // 🔹 Пересчёт общей суммы заказа
    private void recalculateOrderTotal(Order order) {
        double newTotal = orderDetailRepository.findAllByOrderId(order.getId()).stream()
                .mapToDouble(od -> od.getUnitPrice() * od.getAmount())
                .sum();

        order.setTotal(newTotal);

        orderService.updateOrder(order.getId(), new OrderRequest(
                order.getDatetime(),
                order.getState().name(),
                order.getPaymentMethod(),
                newTotal,
                order.getEmployee() != null ? order.getEmployee().getId() : null,
                order.getClient() != null ? order.getClient().getId() : null,
                order.getRestTable() != null ? order.getRestTable().getId() : null
        ));
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
