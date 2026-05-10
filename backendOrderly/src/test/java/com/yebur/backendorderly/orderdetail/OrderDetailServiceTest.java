package com.yebur.backendorderly.orderdetail;

import com.yebur.backendorderly.cashsessions.CashSession;
import com.yebur.backendorderly.cashsessions.CashSessionService;
import com.yebur.backendorderly.order.Order;
import com.yebur.backendorderly.order.OrderRepository;
import com.yebur.backendorderly.order.OrderRequest;
import com.yebur.backendorderly.order.OrderService;
import com.yebur.backendorderly.order.OrderStatus;
import com.yebur.backendorderly.product.Product;
import com.yebur.backendorderly.product.ProductDestination;
import com.yebur.backendorderly.product.ProductService;
import com.yebur.backendorderly.resttable.RestTableService;
import com.yebur.backendorderly.supplements.SupplementService;
import com.yebur.backendorderly.websocket.OrdersTabletWebSocketHandler;
import com.yebur.backendorderly.websocket.WsNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderDetailServiceTest {

    @Mock
    private OrderDetailRepository orderDetailRepository;
    @Mock
    private ProductService productService;
    @Mock
    private OrderService orderService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrdersTabletWebSocketHandler ordersTabletHandler;
    @Mock
    private WsNotifier wsNotifier;
    @Mock
    private SupplementService supplementService;
    @Mock
    private CashSessionService cashSessionService;
    @Mock
    private RestTableService restTableService;

    @InjectMocks
    private OrderDetailService orderDetailService;

    private final Map<Long, OrderDetail> storedDetails = new LinkedHashMap<>();
    private final AtomicLong nextDetailId = new AtomicLong(200);

    private Order order;
    private Product product;
    private CashSession cashSession;

    @BeforeEach
    void setUp() {
        storedDetails.clear();
        nextDetailId.set(200);

        order = new Order();
        order.setId(10L);
        order.setDatetime(LocalDateTime.now());
        order.setState(OrderStatus.OPEN);
        order.setTotal(BigDecimal.ZERO.setScale(2));
        order.setOrderDetails(new ArrayList<>());

        product = new Product();
        product.setId(20L);
        product.setName("Cafe");
        product.setPrice(new BigDecimal("2.50"));
        product.setDestination(ProductDestination.BAR);

        cashSession = new CashSession();
        cashSession.setId(30L);

        when(orderService.findById(anyLong())).thenAnswer(invocation -> {
            Long orderId = invocation.getArgument(0);
            return orderId.equals(order.getId()) ? Optional.of(order) : Optional.empty();
        });
        when(orderRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long orderId = invocation.getArgument(0);
            return orderId.equals(order.getId()) ? Optional.of(order) : Optional.empty();
        });
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productService.findById(product.getId())).thenReturn(Optional.of(product));
        when(cashSessionService.findCashSessionById(cashSession.getId())).thenReturn(Optional.of(cashSession));
        when(orderService.updateOrder(anyLong(), any(OrderRequest.class))).thenAnswer(invocation -> {
            Long orderId = invocation.getArgument(0);
            OrderRequest request = invocation.getArgument(1);
            if (!orderId.equals(order.getId())) {
                throw new RuntimeException("Unexpected order id " + orderId);
            }
            order.setState(OrderStatus.valueOf(request.getState().toUpperCase()));
            order.setPaymentMethod(request.getPaymentMethod());
            order.setTotal(request.getTotal());
            return order;
        });

        when(orderDetailRepository.findById(anyLong())).thenAnswer(invocation ->
                Optional.ofNullable(storedDetails.get(invocation.getArgument(0))));
        when(orderDetailRepository.findAllByOrderId(anyLong())).thenAnswer(invocation -> {
            Long orderId = invocation.getArgument(0);
            return storedDetails.values().stream()
                    .filter(detail -> detail.getOrder().getId().equals(orderId))
                    .sorted(Comparator.comparing(OrderDetail::getId))
                    .collect(Collectors.toCollection(ArrayList::new));
        });
        when(orderDetailRepository.save(any(OrderDetail.class))).thenAnswer(invocation -> persist(invocation.getArgument(0)));
        when(orderDetailRepository.saveAll(any(List.class))).thenAnswer(invocation -> {
            List<OrderDetail> details = invocation.getArgument(0);
            details.forEach(this::persist);
            return details;
        });
        when(orderDetailRepository.existsByOrderIdAndPaid(anyLong(), anyBoolean())).thenAnswer(invocation -> {
            Long orderId = invocation.getArgument(0);
            boolean paid = invocation.getArgument(1);
            return storedDetails.values().stream()
                    .anyMatch(detail -> detail.getOrder().getId().equals(orderId) && detail.isPaid() == paid);
        });
        when(orderDetailRepository.existsByOrderIdAndStatusNot(anyLong(), any(OrderDetailStatus.class))).thenAnswer(invocation -> {
            Long orderId = invocation.getArgument(0);
            OrderDetailStatus status = invocation.getArgument(1);
            return storedDetails.values().stream()
                    .anyMatch(detail -> detail.getOrder().getId().equals(orderId) && detail.getStatus() != status);
        });

        doAnswer(invocation -> {
            OrderDetail detail = invocation.getArgument(0);
            remove(detail.getId());
            return null;
        }).when(orderDetailRepository).delete(any(OrderDetail.class));
        doAnswer(invocation -> {
            List<OrderDetail> details = invocation.getArgument(0);
            details.forEach(detail -> remove(detail.getId()));
            return null;
        }).when(orderDetailRepository).deleteAll(any(List.class));
    }

    @Test
    void partialPaymentForOneOfThreeIdenticalProductsLeavesTwoUnpaidAndOnePaid() {
        seedDetail(101L, 3, false, null, OrderDetailStatus.PENDING);

        orderDetailService.updateOrderDetailList(
                List.of(101L),
                List.of(detailRequest(2, false, null, OrderDetailStatus.PENDING)));

        orderDetailService.createOrderDetailList(
                List.of(detailRequest(1, true, "CARD", OrderDetailStatus.PENDING)));

        assertEquals(2, storedDetails.size());

        OrderDetail unpaid = findSingleDetail(false, null);
        assertEquals(2, unpaid.getAmount());
        assertEquals(OrderDetailStatus.PENDING, unpaid.getStatus());

        OrderDetail paid = findSingleDetail(true, "CARD");
        assertEquals(1, paid.getAmount());
        assertEquals(OrderDetailStatus.PENDING, paid.getStatus());

        assertEquals(OrderStatus.OPEN, order.getState());
        assertEquals(new BigDecimal("7.50"), order.getTotal());
    }

    @Test
    void fullPaymentUsingExplicitDeleteLeavesOnlyPaidLine() {
        seedDetail(101L, 3, false, null, OrderDetailStatus.PENDING);

        orderDetailService.deleteOrderDetail(101L);
        orderDetailService.createOrderDetailList(
                List.of(detailRequest(3, true, "CARD", OrderDetailStatus.PENDING)));

        assertEquals(1, storedDetails.size());
        OrderDetail remaining = storedDetails.values().iterator().next();
        assertTrue(remaining.isPaid());
        assertEquals("CARD", remaining.getPaymentMethod());
        assertEquals(3, remaining.getAmount());
        assertEquals(OrderDetailStatus.PENDING, remaining.getStatus());
        assertEquals(OrderStatus.OPEN, order.getState());
    }

    @Test
    void createOrderDetailListMergesPaidSlicesWithSamePaymentMethod() {
        orderDetailService.createOrderDetailList(List.of(
                detailRequest(1, true, "CARD", OrderDetailStatus.PENDING),
                detailRequest(2, true, "CARD", OrderDetailStatus.PENDING)));

        assertEquals(1, storedDetails.size());
        OrderDetail merged = storedDetails.values().iterator().next();
        assertTrue(merged.isPaid());
        assertEquals("CARD", merged.getPaymentMethod());
        assertEquals(3, merged.getAmount());
    }

    @Test
    void createOrderDetailListKeepsPaidSlicesSeparateAcrossPaymentMethods() {
        orderDetailService.createOrderDetailList(List.of(
                detailRequest(1, true, "CARD", OrderDetailStatus.PENDING),
                detailRequest(1, true, "CASH", OrderDetailStatus.PENDING)));

        assertEquals(2, storedDetails.size());
        assertNotNull(findSingleDetail(true, "CARD"));
        assertNotNull(findSingleDetail(true, "CASH"));
    }

    @Test
    void updateOrderDetailListDeletesZeroAmountRows() {
        seedDetail(101L, 1, false, null, OrderDetailStatus.PENDING);

        orderDetailService.updateOrderDetailList(
                List.of(101L),
                List.of(detailRequest(0, false, null, OrderDetailStatus.PENDING)));

        assertTrue(storedDetails.isEmpty());
        assertEquals(BigDecimal.ZERO.setScale(2), order.getTotal());
        assertEquals(OrderStatus.PAID, order.getState());
    }

    private OrderDetailRequest detailRequest(int amount, boolean paid, String paymentMethod, OrderDetailStatus status) {
        OrderDetailRequest request = new OrderDetailRequest();
        request.setProductId(product.getId());
        request.setOrderId(order.getId());
        request.setName(product.getName());
        request.setComment(null);
        request.setAmount(amount);
        request.setUnitPrice(product.getPrice());
        request.setStatus(status.name());
        request.setPaymentMethod(paymentMethod);
        request.setBatchId("batch-1");
        request.setCreatedAt(LocalDateTime.now());
        request.setCashSessionId(cashSession.getId());
        request.setPaid(paid);
        return request;
    }

    private void seedDetail(Long id, int amount, boolean paid, String paymentMethod, OrderDetailStatus status) {
        OrderDetail detail = new OrderDetail();
        detail.setId(id);
        detail.setOrder(order);
        detail.setProduct(product);
        detail.setCashSession(cashSession);
        detail.setName(product.getName());
        detail.setComment(null);
        detail.setAmount(amount);
        detail.setUnitPrice(product.getPrice());
        detail.setCreatedAt(LocalDateTime.now());
        detail.setStatus(status);
        detail.setPaymentMethod(paymentMethod);
        detail.setPaid(paid);
        detail.setBatchId("batch-1");
        persist(detail);
    }

    private OrderDetail persist(OrderDetail detail) {
        if (detail.getId() == null) {
            detail.setId(nextDetailId.incrementAndGet());
        }
        storedDetails.put(detail.getId(), detail);
        syncOrderDetails();
        return detail;
    }

    private void remove(Long id) {
        storedDetails.remove(id);
        syncOrderDetails();
    }

    private void syncOrderDetails() {
        order.setOrderDetails(storedDetails.values().stream()
                .filter(detail -> detail.getOrder().getId().equals(order.getId()))
                .sorted(Comparator.comparing(OrderDetail::getId))
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    private OrderDetail findSingleDetail(boolean paid, String paymentMethod) {
        List<OrderDetail> matches = storedDetails.values().stream()
                .filter(detail -> detail.isPaid() == paid)
                .filter(detail -> paymentMethod == null ? detail.getPaymentMethod() == null : paymentMethod.equals(detail.getPaymentMethod()))
                .toList();
        assertEquals(1, matches.size());
        OrderDetail match = matches.get(0);
        if (paid) {
            assertFalse(match.getPaymentMethod() == null || match.getPaymentMethod().isBlank());
        } else {
            assertNull(match.getPaymentMethod());
        }
        return match;
    }
}