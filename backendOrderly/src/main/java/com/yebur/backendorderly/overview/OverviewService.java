package com.yebur.backendorderly.overview;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yebur.backendorderly.order.OrderResponse;
import com.yebur.backendorderly.order.OrderService;
import com.yebur.backendorderly.order.OrderStatus;
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
                        var unpaidDetails = orderDetailService.findUnpaidOrderDetailDTOByOrderId(o.getId());
                        double unpaidTotal = unpaidDetails.stream()
                                .mapToDouble(d -> d.getUnitPrice() * d.getAmount())
                                .sum();
                        return new OrderSummary(o.getId(), unpaidTotal);
                    })
                    .orElse(null);

            overview.add(new TableWithOrderResponse(
                    table.getId(),
                    table.getName(),
                    orderSummary == null ? new OrderSummary() : orderSummary
            ));
        }

        List<OrderResponse> ordersWithoutTable = orders.stream()
                .filter(o -> o.getRestTable() == null)
                .toList();

        for (OrderResponse order : ordersWithoutTable) {
            overview.add(new TableWithOrderResponse(
                    null,
                    "Sin mesa",
                    new OrderSummary(order.getId(), order.getTotal())
            ));
        }

        return overview;
    }
}
