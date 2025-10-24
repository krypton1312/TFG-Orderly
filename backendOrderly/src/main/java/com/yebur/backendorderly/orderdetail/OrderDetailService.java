package com.yebur.backendorderly.orderdetail;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yebur.backendorderly.order.Order;
import com.yebur.backendorderly.order.OrderRepository;
import com.yebur.backendorderly.order.OrderRequest;
import com.yebur.backendorderly.order.OrderService;
import com.yebur.backendorderly.order.OrderStatus;
import com.yebur.backendorderly.product.Product;
import com.yebur.backendorderly.product.ProductService;

@Service("orderDetailService")
public class OrderDetailService implements OrderDetailServiceInterface {

    private final OrderDetailRepository orderDetailRepository;
    private final ProductService productService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public OrderDetailService(OrderDetailRepository orderDetailRepository,
            ProductService productService, OrderService orderService, OrderRepository orderRepository) {
        this.orderDetailRepository = orderDetailRepository;
        this.productService = productService;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
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

    public List<OrderDetailResponse> findUnpaidOrderDetailDTOByOrderId(Long orderId) {
        return orderDetailRepository.findUnpaidOrderDetailDTOByOrderId(orderId);
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

        recalculateOrderTotal(saved.getOrder());

        return mapToResponse(saved);
    }

    @Override
    public void deleteOrderDetail(Long id) {
        OrderDetail detail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderDetail not found with id " + id));

        Order order = detail.getOrder();
        orderDetailRepository.delete(detail);

        recalculateOrderTotal(order);
    }

    public void updateOrderDetailStatus(List<Long> ids, String status) {
        OrderDetailStatus enumStatus;
        try {
            enumStatus = OrderDetailStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unknown status: " + status);
        }

        for (Long id : ids) {
            OrderDetail detail = findById(id)
                    .orElseThrow(() -> new RuntimeException("OrderDetail not found with id " + id));

            detail.setStatus(enumStatus);
            orderDetailRepository.save(detail);

            Order order = orderService.findById(detail.getOrder().getId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            boolean allPaid = true;
            for (OrderDetail d : order.getOrderDetails()) {
                if (d.getStatus() != OrderDetailStatus.PAID) {
                    allPaid = false;
                    break;
                }
            }

            if (allPaid) {
                order.setState(OrderStatus.PAID);
                orderRepository.save(order);
            }
        }
    }

    private void recalculateOrderTotal(Order order) {
        double newTotal = Math.round(
                orderDetailRepository.findAllByOrderId(order.getId()).stream()
                        .mapToDouble(od -> od.getUnitPrice() * od.getAmount())
                        .sum() * 100.0)
                / 100.0;

        order.setTotal(newTotal);

        orderService.updateOrder(order.getId(), new OrderRequest(
                order.getDatetime(),
                order.getState().name(),
                order.getPaymentMethod(),
                newTotal,
                order.getEmployee() != null ? order.getEmployee().getId() : null,
                order.getClient() != null ? order.getClient().getId() : null,
                order.getRestTable() != null ? order.getRestTable().getId() : null));
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
        detail.setStatus(OrderDetailStatus.valueOf(dto.getStatus()));
        return detail;
    }

    private OrderDetailResponse mapToResponse(OrderDetail entity) {
        return new OrderDetailResponse(
                entity.getId(),
                entity.getProduct().getId(),
                entity.getProduct().getName(),
                entity.getOrder().getId(),
                entity.getComment(),
                entity.getAmount(),
                entity.getUnitPrice(),
                entity.getStatus());
    }
}
