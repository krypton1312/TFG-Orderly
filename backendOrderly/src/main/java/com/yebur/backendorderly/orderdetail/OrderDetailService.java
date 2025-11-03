package com.yebur.backendorderly.orderdetail;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yebur.backendorderly.order.*;
import com.yebur.backendorderly.product.Product;
import com.yebur.backendorderly.product.ProductDestination;
import com.yebur.backendorderly.product.ProductService;
import com.yebur.backendorderly.websocket.OrdersTabletWebSocketHandler;

@Service("orderDetailService")
public class OrderDetailService implements OrderDetailServiceInterface {

    private final OrderDetailRepository orderDetailRepository;
    private final OrdersTabletWebSocketHandler ordersTabletHandler;
    private final ProductService productService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public OrderDetailService(OrderDetailRepository orderDetailRepository,
            ProductService productService,
            OrderService orderService,
            OrderRepository orderRepository, OrdersTabletWebSocketHandler ordersTabletHandler) {
        this.orderDetailRepository = orderDetailRepository;
        this.productService = productService;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.ordersTabletHandler = ordersTabletHandler;
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
    @Transactional
    public OrderDetailResponse createOrderDetail(OrderDetailRequest dto) {
        OrderDetail orderDetail = mapToEntity(dto);
        orderDetail.setCreatedAt(LocalDateTime.now());
        OrderDetail saved = orderDetailRepository.save(orderDetail);

        recalculateOrderTotal(saved.getOrder());
        checkAndUpdateOrderStatus(saved.getOrder().getId());

        ordersTabletHandler.broadcast(Map.of(
                "event", "ORDER_CHANGED"));

        return mapToResponse(saved);
    }

    @Transactional
    public List<OrderDetailResponse> createOrderDetailList(List<OrderDetailRequest> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            throw new IllegalArgumentException("OrderDetailRequest list cannot be empty");
        }

        List<OrderDetail> entities = new ArrayList<>();
        for (OrderDetailRequest dto : dtos) {
            OrderDetail entity = mapToEntity(dto);
            entity.setCreatedAt(LocalDateTime.now());
            entities.add(entity);
        }

        List<OrderDetail> saved = orderDetailRepository.saveAll(entities);
        saved.stream().map(OrderDetail::getOrder).distinct().forEach(o -> {
            recalculateOrderTotal(o);
            checkAndUpdateOrderStatus(o.getId());
        });

        ordersTabletHandler.broadcast(Map.of(
                "event", "ORDER_CHANGED"));

        return saved.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public OrderDetailResponse updateOrderDetail(Long id, OrderDetailRequest dto) {
        OrderDetail existing = orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderDetail not found with id " + id));

        existing.setAmount(dto.getAmount());
        existing.setUnitPrice(dto.getUnitPrice().setScale(2, RoundingMode.HALF_UP));
        existing.setComment(dto.getComment());
        existing.setStatus(OrderDetailStatus.valueOf(dto.getStatus().toUpperCase()));
        existing.setCreatedAt(LocalDateTime.now());
        existing.setPaymentMethod(dto.getPaymentMethod());

        Product product = productService.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id " + dto.getProductId()));
        existing.setProduct(product);

        Order order = orderService.findById(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id " + dto.getOrderId()));
        existing.setOrder(order);

        OrderDetail saved = orderDetailRepository.save(existing);

        recalculateOrderTotal(order);
        checkAndUpdateOrderStatus(order.getId());

        ordersTabletHandler.broadcast(Map.of(
                "event", "ORDER_CHANGED"));

        return mapToResponse(saved);
    }

    @Transactional
    public List<OrderDetailResponse> updateOrderDetailList(List<Long> ids, List<OrderDetailRequest> dtos) {
        if (ids == null || dtos == null || ids.isEmpty() || dtos.isEmpty()) {
            throw new IllegalArgumentException("IDs and DTO lists cannot be empty");
        }
        if (ids.size() != dtos.size()) {
            throw new IllegalArgumentException("IDs list and DTO list must have the same size");
        }

        List<OrderDetail> updatedEntities = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            OrderDetailRequest dto = dtos.get(i);

            OrderDetail existing = orderDetailRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("OrderDetail not found with id " + id));

            existing.setAmount(dto.getAmount());
            existing.setUnitPrice(dto.getUnitPrice().setScale(2, RoundingMode.HALF_UP));
            existing.setComment(dto.getComment());
            existing.setStatus(OrderDetailStatus.valueOf(dto.getStatus().toUpperCase()));
            existing.setCreatedAt(LocalDateTime.now());
            existing.setPaymentMethod(dto.getPaymentMethod());

            Product product = productService.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id " + dto.getProductId()));
            existing.setProduct(product);

            Order order = orderService.findById(dto.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found with id " + dto.getOrderId()));
            existing.setOrder(order);

            updatedEntities.add(existing);
        }

        List<OrderDetail> saved = orderDetailRepository.saveAll(updatedEntities);
        saved.stream().map(OrderDetail::getOrder).distinct().forEach(o -> {
            recalculateOrderTotal(o);
            checkAndUpdateOrderStatus(o.getId());
        });

        ordersTabletHandler.broadcast(Map.of(
                "event", "ORDER_CHANGED"));

        return saved.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public void deleteOrderDetail(Long id) {
        OrderDetail detail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderDetail not found with id " + id));

        Order order = detail.getOrder();
        orderDetailRepository.delete(detail);

        recalculateOrderTotal(order);

        ordersTabletHandler.broadcast(Map.of(
                "event", "ORDER_CHANGED"));

        checkAndUpdateOrderStatus(order.getId());
    }

    @Transactional
    public void updateOrderDetailStatus(List<Long> ids, String status) {
        OrderDetailStatus enumStatus;
        try {
            enumStatus = OrderDetailStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unknown status: " + status);
        }

        List<OrderDetail> details = orderDetailRepository.findAllById(ids);
        if (details.size() != ids.size()) {
            throw new RuntimeException("Some OrderDetail IDs were not found");
        }

        for (OrderDetail detail : details) {
            detail.setStatus(enumStatus);
            orderDetailRepository.save(detail);

            if (enumStatus == OrderDetailStatus.SERVED || enumStatus == OrderDetailStatus.PAID) {
                mergeSimilarDetails(detail, enumStatus);
            }
        }

        details.stream()
                .map(d -> d.getOrder().getId())
                .distinct()
                .forEach(this::checkAndUpdateOrderStatus);

        ordersTabletHandler.broadcast(Map.of("event", "ORDER_CHANGED"));
    }

    private void checkAndUpdateOrderStatus(Long orderId) {
        Order order = orderService.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id " + orderId));

        boolean hasUnpaid = orderDetailRepository.existsByOrderIdAndStatusNot(orderId, OrderDetailStatus.PAID);
        OrderStatus newState = hasUnpaid ? OrderStatus.OPEN : OrderStatus.PAID;

        if (order.getState() != newState) {
            order.setState(newState);
            orderRepository.save(order);
            ordersTabletHandler.broadcast(Map.of(
                    "event", "ORDER_CHANGED"));
        }
    }

    private void recalculateOrderTotal(Order order) {
        BigDecimal newTotal = orderDetailRepository.findAllByOrderId(order.getId()).stream()
                .map(od -> od.getUnitPrice().multiply(BigDecimal.valueOf(od.getAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

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

    @Transactional
    protected void mergeSimilarDetails(OrderDetail detail, OrderDetailStatus status) {
        List<OrderDetail> duplicates = orderDetailRepository.findAllByOrderId(detail.getOrder().getId()).stream()
                .filter(d -> !Objects.equals(d.getId(), detail.getId()) &&
                        d.getStatus() == status &&
                        d.getProduct().getId().equals(detail.getProduct().getId()) &&
                        d.getUnitPrice().compareTo(detail.getUnitPrice()) == 0)
                .collect(Collectors.toList());

        if (duplicates.isEmpty())
            return;

        OrderDetail main = duplicates.get(0);

        int totalAmount = main.getAmount() + detail.getAmount()
                + duplicates.stream().skip(1).mapToInt(OrderDetail::getAmount).sum();

        main.setAmount(totalAmount);

        List<OrderDetail> toDelete = new ArrayList<>(duplicates);
        toDelete.add(detail);
        toDelete.remove(main);

        orderDetailRepository.deleteAll(toDelete);
        orderDetailRepository.save(main);

        recalculateOrderTotal(main.getOrder());
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
        detail.setUnitPrice(dto.getUnitPrice().setScale(2, RoundingMode.HALF_UP));
        detail.setComment(dto.getComment());
        detail.setStatus(OrderDetailStatus.valueOf(dto.getStatus().toUpperCase()));
        detail.setPaymentMethod(dto.getPaymentMethod());
        detail.setBatchId(dto.getBatchId());
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
                entity.getStatus(),
                entity.getPaymentMethod(),
                entity.getCreatedAt(),
                entity.getProduct().getDestination(),
                entity.getBatchId());
    }
}
