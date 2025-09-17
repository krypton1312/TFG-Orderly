package com.yebur.backendorderly.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDateTime datetime;

    @Column(nullable = false)
    private String state;

    @Column
    private String metodo_pago;

    @Column
    private double total;

    //@ManyToOne
    //@JoinColumn(name = "id_employee", nullable = false)
    //private Employee employee;

    @ManyToOne
    @JoinColumn(name = "id_client")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "id_restable")
    private RestTable restTable;
}
