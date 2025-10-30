package com.yebur.backendorderly.overview;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yebur.backendorderly.order.OrderResponse;
import com.yebur.backendorderly.order.OrderService;
import com.yebur.backendorderly.order.OrderStatus;
import com.yebur.backendorderly.orderdetail.OrderDetailResponse;
import com.yebur.backendorderly.orderdetail.OrderDetailService;
import com.yebur.backendorderly.resttable.RestTableResponse;
import com.yebur.backendorderly.resttable.RestTableService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OverviewService {

        private final RestTableService restTableService;
        private final OrderService orderService;
        private final OrderDetailService orderDetailService;

        public List<TableWithOrderResponse> getOverview() {
                List<RestTableResponse> tables = restTableService.findAllRestTableDTO();
                List<OrderResponse> orders = orderService.findAllOrderDTOByStatus(OrderStatus.OPEN);

                List<TableWithOrderResponse> overview = new ArrayList<>();

                for (RestTableResponse table : tables) {
                        Optional<OrderResponse> matchingOrder = orders.stream()
                                        .filter(o -> o.getRestTable() != null &&
                                                        Objects.equals(o.getRestTable().getId(), table.getId()))
                                        .findFirst();

                        OrderSummary orderSummary = matchingOrder
                                        .map(o -> {
                                                var unpaidDetails = orderDetailService
                                                                .findUnpaidOrderDetailDTOByOrderId(o.getId());

                                                BigDecimal unpaidTotal = unpaidDetails.stream()
                                                                .map(d -> d.getUnitPrice()
                                                                                .multiply(BigDecimal.valueOf(
                                                                                                d.getAmount())))
                                                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                                                .setScale(2, RoundingMode.HALF_UP);

                                                return new OrderSummary(o.getId(), unpaidTotal.doubleValue());
                                        })
                                        .orElse(null);

                        overview.add(new TableWithOrderResponse(
                                        table.getId(),
                                        table.getName(),
                                        orderSummary == null ? new OrderSummary() : orderSummary));
                }

                List<OrderResponse> ordersWithoutTable = orders.stream()
                                .filter(o -> o.getRestTable() == null)
                                .toList();

                for (OrderResponse order : ordersWithoutTable) {
                        var unpaidDetails = orderDetailService.findUnpaidOrderDetailDTOByOrderId(order.getId());

                        BigDecimal unpaidTotal = unpaidDetails.stream()
                                        .map(d -> d.getUnitPrice()
                                                        .multiply(BigDecimal.valueOf(d.getAmount())))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                                        .setScale(2, RoundingMode.HALF_UP);

                        overview.add(new TableWithOrderResponse(
                                        null,
                                        "Sin mesa",
                                        new OrderSummary(order.getId(), unpaidTotal.doubleValue())));
                }

                return overview;
        }

        public List<OrderWithOrderDetailResponse> getOrderWithOrderDetails() {
                List<OrderWithOrderDetailResponse> overview = new ArrayList<>();

                // Получаем все открытые заказы
                List<OrderResponse> orders = orderService.findAllOrderDTOByStatus(OrderStatus.OPEN);

                for (OrderResponse order : orders) {

                        // Получаем все детали по заказу
                        List<OrderDetailResponse> details = orderDetailService
                                        .findUnpaidOrderDetailDTOByOrderId(order.getId());

                        // Группируем детали по времени добавления (до минуты)
                        Map<LocalDateTime, List<OrderDetailResponse>> groupedDetails = details.stream()
                                        .collect(Collectors.groupingBy(
                                                        detail -> detail.getCreatedAt().withSecond(0).withNano(0) // округляем
                                                                                                                  // до
                                                                                                                  // минуты
                                        ));

                        // Для каждой группы создаём отдельную карточку заказа
                        for (Map.Entry<LocalDateTime, List<OrderDetailResponse>> entry : groupedDetails.entrySet()) {
                                OrderWithOrderDetailResponse orderWithDetails = new OrderWithOrderDetailResponse();
                                orderWithDetails.setId(order.getId());
                                orderWithDetails.setTableName(
                                                order.getRestTable() == null ? "Sin mesa"
                                                                : order.getRestTable().getName());
                                orderWithDetails.setDatetime(entry.getKey());

                                List<OrderDetailSummary> ods = entry.getValue().stream()
                                                .map(detail -> new OrderDetailSummary(
                                                                detail.getId(),
                                                                detail.getProductName(),
                                                                detail.getComment(),
                                                                detail.getAmount(),
                                                                detail.getStatus()))
                                                .collect(Collectors.toList());

                                orderWithDetails.setDetails(ods);
                                overview.add(orderWithDetails);
                        }
                }

                overview.sort(Comparator.comparing(OrderWithOrderDetailResponse::getDatetime));

                return overview;
        }

}
