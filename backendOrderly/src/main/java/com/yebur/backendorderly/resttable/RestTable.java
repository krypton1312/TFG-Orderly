package com.yebur.backendorderly.resttable;

import java.util.ArrayList;
import java.util.List;

import com.yebur.backendorderly.order.Order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name="rest_tables",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"number", "position"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private int number;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RestTablePosition position;

    @OneToMany(mappedBy = "restTable")
    private List<Order> orders = new ArrayList<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TableStatus status;
}
