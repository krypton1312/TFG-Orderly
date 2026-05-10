package com.yebur.backendorderly.analytics;

import com.yebur.backendorderly.cashsessions.CashSessionRepository;
import com.yebur.backendorderly.order.OrderRepository;
import com.yebur.backendorderly.orderdetail.OrderDetailRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceTest {

    @Mock
    private CashSessionRepository cashSessionRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void testMonthlyRevenue() {
        when(cashSessionRepository.getMonthlyRevenue(any(), any()))
                .thenReturn(new Object[]{new BigDecimal("1800.00"), new BigDecimal("2520.50")});
        when(orderRepository.countClosedOrdersByMonth(any(), any())).thenReturn(87L);
        when(orderDetailRepository.getTopProductsByMonth(any(), any(), any())).thenReturn(List.of());

        MonthlySummaryResponse result = analyticsService.getMonthlySummary(YearMonth.of(2026, 5));

        assertEquals(new BigDecimal("4320.50"), result.getTotalRevenue());
        assertEquals(new BigDecimal("1800.00"), result.getTotalSalesCash());
        assertEquals(new BigDecimal("2520.50"), result.getTotalSalesCard());
    }

    @Test
    void testOrderCount() {
        when(cashSessionRepository.getMonthlyRevenue(any(), any()))
                .thenReturn(new Object[]{BigDecimal.ZERO, BigDecimal.ZERO});
        when(orderRepository.countClosedOrdersByMonth(any(), any())).thenReturn(42L);
        when(orderDetailRepository.getTopProductsByMonth(any(), any(), any())).thenReturn(List.of());

        MonthlySummaryResponse result = analyticsService.getMonthlySummary(YearMonth.of(2026, 5));

        assertEquals(42, result.getOrderCount());
    }

    @Test
    void testTopProducts() {
        when(cashSessionRepository.getMonthlyRevenue(any(), any()))
                .thenReturn(new Object[]{BigDecimal.ZERO, BigDecimal.ZERO});
        when(orderRepository.countClosedOrdersByMonth(any(), any())).thenReturn(10L);
        when(orderDetailRepository.getTopProductsByMonth(any(), any(), any()))
                .thenReturn(List.of(
                        new Object[]{"Chuletón", 45L},
                        new Object[]{"Cerveza", 32L}
                ));

        MonthlySummaryResponse result = analyticsService.getMonthlySummary(YearMonth.of(2026, 5));

        assertEquals(2, result.getTopProducts().size());
        assertEquals("Chuletón", result.getTopProducts().get(0).getName());
        assertEquals(45L, result.getTopProducts().get(0).getQuantity());
    }

    @Test
    void testAvgOrderValue() {
        when(cashSessionRepository.getMonthlyRevenue(any(), any()))
                .thenReturn(new Object[]{new BigDecimal("1800.00"), new BigDecimal("2520.50")});
        when(orderRepository.countClosedOrdersByMonth(any(), any())).thenReturn(87L);
        when(orderDetailRepository.getTopProductsByMonth(any(), any(), any())).thenReturn(List.of());

        MonthlySummaryResponse result = analyticsService.getMonthlySummary(YearMonth.of(2026, 5));

        // 4320.50 / 87 = 49.66 (HALF_UP, scale 2)
        assertEquals(new BigDecimal("49.66"), result.getAvgOrderValue());
    }

    @Test
    void testAvgOrderValueZeroOrders() {
        when(cashSessionRepository.getMonthlyRevenue(any(), any()))
                .thenReturn(new Object[]{new BigDecimal("100.00"), new BigDecimal("200.00")});
        when(orderRepository.countClosedOrdersByMonth(any(), any())).thenReturn(0L);
        when(orderDetailRepository.getTopProductsByMonth(any(), any(), any())).thenReturn(List.of());

        MonthlySummaryResponse result = analyticsService.getMonthlySummary(YearMonth.of(2026, 5));

        assertEquals(BigDecimal.ZERO, result.getAvgOrderValue());
    }
}
