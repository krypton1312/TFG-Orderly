package com.yebur.backendorderly.resttable;

import java.util.ArrayList;
import java.util.List;

import com.yebur.backendorderly.order.Order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="rest_tables")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private int number;

    @OneToMany(mappedBy = "restTable")
    private List<Order> orders = new ArrayList<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TableStatus status;
}
