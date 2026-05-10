package com.yebur.backendorderly.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findFirstByRestTableIdAndState(Long restTableId, OrderStatus state);

    @Query("""
                SELECT new com.yebur.backendorderly.order.OrderResponse(
                    o.id,
                    o.datetime,
                    CAST(o.state AS string),
                    o.paymentMethod,
                    o.total,
                    e.id,
                    c.id,
                    r.id
                )
                FROM Order o
                LEFT JOIN o.employee e
                LEFT JOIN o.client c
                LEFT JOIN o.restTable r
            """)
    List<OrderResponse> findAllOrderDTO();

    @Query("""
            SELECT new com.yebur.backendorderly.order.OrderResponse(
                o.id,
                o.datetime,
                CAST(o.state AS string),
                o.paymentMethod,
                o.total,
                e.id,
                c.id,
                r.id
            )
            FROM Order o
            LEFT JOIN o.employee e
            LEFT JOIN o.client c
            LEFT JOIN o.restTable r
            WHERE o.state = :state
            """)
    List<OrderResponse> findAllByStateDTO(OrderStatus state);

    @Query("""
            SELECT new com.yebur.backendorderly.order.OrderResponse(
                o.id,
                o.datetime,
                CAST(o.state AS string),
                o.paymentMethod,
                o.total,
                e.id,
                c.id,
                r.id
            )
            FROM Order o
            LEFT JOIN o.employee e
            LEFT JOIN o.client c
            LEFT JOIN o.restTable r
            WHERE o.id = :id
            """)
    Optional<OrderResponse> findOrderDTOById(@Param("id") Long id);

    @Query("""
            SELECT COUNT(o)
            FROM Order o
            WHERE o.datetime >= :start AND o.datetime < :end
              AND o.state = com.yebur.backendorderly.order.OrderStatus.PAID
            """)
    long countClosedOrdersByMonth(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

}
