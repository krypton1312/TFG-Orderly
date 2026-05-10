package com.yebur.backendorderly.analytics;

import com.yebur.backendorderly.cashsessions.CashSessionRepository;
import com.yebur.backendorderly.order.OrderRepository;
import com.yebur.backendorderly.orderdetail.OrderDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService implements AnalyticsServiceInterface {

    private final CashSessionRepository cashSessionRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Override
    public MonthlySummaryResponse getMonthlySummary(YearMonth ym) {
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.plusDays(1).atStartOfDay();

        List<Object[]> revenueRows = cashSessionRepository.getMonthlyRevenue(start, end);
        Object[] revenue = revenueRows.isEmpty() ? new Object[]{BigDecimal.ZERO, BigDecimal.ZERO} : revenueRows.get(0);
        BigDecimal cash = revenue[0] instanceof BigDecimal bd ? bd : new BigDecimal(revenue[0].toString());
        BigDecimal card = revenue[1] instanceof BigDecimal bd ? bd : new BigDecimal(revenue[1].toString());
        BigDecimal total = cash.add(card);

        long orderCount = orderRepository.countClosedOrdersByMonth(startDt, endDt);

        BigDecimal avg = orderCount == 0 ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP);

        List<Object[]> rawProducts = orderDetailRepository.getTopProductsByMonth(
                start, end, PageRequest.of(0, 5));
        List<TopProductEntry> topProducts = rawProducts.stream()
                .map(row -> new TopProductEntry((String) row[0], ((Number) row[1]).longValue()))
                .toList();

        return new MonthlySummaryResponse(ym.getYear(), ym.getMonthValue(),
                total, cash, card, (int) orderCount, avg, topProducts);
    }
}
