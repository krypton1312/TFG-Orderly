package com.yebur.backendorderly.resttable;

import java.util.ArrayList;
import java.util.List;

import com.yebur.backendorderly.order.Order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
        name="rest_tables",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"number", "position"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class RestTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;
    
    @Column(nullable = false)
    @ToString.Include
    private int number;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @ToString.Include
    private RestTablePosition position;

    @OneToMany(mappedBy = "restTable")
    private List<Order> orders = new ArrayList<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @ToString.Include
    private TableStatus status;
}
