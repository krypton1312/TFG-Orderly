package com.yebur.backendorderly.overview;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yebur.backendorderly.order.Order;

@Repository
public interface OverviewRepository extends JpaRepository<Order, Long> {
    
}

