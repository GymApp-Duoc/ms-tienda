package com.gymapp.ms_tienda.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productoId;

    @Column(nullable = false)
    private Long miembroId;

    @Column(nullable = false)
    private int cantidad;


    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;


    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaVenta = LocalDateTime.now();
}