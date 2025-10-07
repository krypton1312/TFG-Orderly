package com.yebur.backendorderly.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yebur.backendorderly.dto.output.OrderResponse;
import com.yebur.backendorderly.entities.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
                SELECT new com.yebur.backendorderly.dto.output.OrderResponse(
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
                SELECT new com.yebur.backendorderly.dto.output.OrderResponse(
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

}
