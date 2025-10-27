package com.yebur.backendorderly.overview;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yebur.backendorderly.order.Order;

@Repository
public interface OverviewRepository extends JpaRepository<Order, Long> {

    @Query(value = """
        SELECT 
            rt.id AS tableId,
            rt.name AS tableName,
            o.id AS orderId,
            COALESCE(SUM(od.amount * od.unit_price), 0) AS total
        FROM rest_table rt
        LEFT JOIN orders o ON rt.id = o.rest_table_id
        LEFT JOIN order_detail od ON o.id = od.order_id
        GROUP BY rt.id, rt.name, o.id
        UNION
        SELECT 
            NULL AS tableId,
            'Sin mesa' AS tableName,
            o.id AS orderId,
            COALESCE(SUM(od.amount * od.unit_price), 0) AS total
        FROM orders o
        LEFT JOIN order_detail od ON o.id = od.order_id
        WHERE o.rest_table_id IS NULL 
        GROUP BY o.id
        ORDER BY tableId NULLS LAST
        """,
        nativeQuery = true)
    List<Object[]> getTablesWithOrdersRaw();
}

