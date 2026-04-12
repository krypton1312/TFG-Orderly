package com.yebur.backendorderly.order;

import com.yebur.backendorderly.resttable.RestTableRepository;
import com.yebur.backendorderly.resttable.RestTableService;
import com.yebur.backendorderly.websocket.WsNotifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private RestTableRepository restTableRepository;
    @Mock
    private RestTableService restTableService;
    @Mock
    private WsNotifier wsNotifier;

    @InjectMocks
    private OrderService orderService;

    @Test
    public void testUpdateOrder_NoNPE_WhenOrderDetailsIsNull() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setState(OrderStatus.OPEN);
        order.setOrderDetails(null);

        OrderRequest request = new OrderRequest();
        request.setState("OPEN");
        request.setTotal(BigDecimal.TEN);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        // Should NOT throw NPE anymore
        orderService.updateOrder(orderId, request);
    }

    @Test
    public void testUpdateOrder_NoPAID_WhenOrderDetailsIsEmpty() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setState(OrderStatus.OPEN);
        // orderDetails is initialized to empty list in Order entity

        OrderRequest request = new OrderRequest();
        request.setState("OPEN");
        request.setTotal(BigDecimal.TEN);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.updateOrder(orderId, request);
        
        // Assert that state is still OPEN (or whatever it was set to in request)
        // Since request had "OPEN", it should stay "OPEN"
        // If it was wrongly marked as PAID, it would be PAID
        assertEquals(OrderStatus.OPEN, order.getState());
    }

    @Test
    public void testCreateOrder_RejectsSecondOpenOrderForSameTable() {
        Long tableId = 9L;

        OrderRequest request = new OrderRequest();
        request.setIdTable(tableId);
        request.setState("OPEN");

        Order existingOrder = new Order();
        existingOrder.setId(99L);
        existingOrder.setState(OrderStatus.OPEN);

        when(orderRepository.findFirstByRestTableIdAndState(tableId, OrderStatus.OPEN))
                .thenReturn(Optional.of(existingOrder));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> orderService.createOrder(request));

        assertEquals("Open order already exists for table 9", exception.getMessage());
    }
}
