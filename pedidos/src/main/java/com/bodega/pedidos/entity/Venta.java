package com.bodega.pedidos.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ventas")
@Data
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
    
    @Column(nullable = false)
    private Double subtotal;
    
    @Column(nullable = false)
    private Double igv;
    
    @Column(nullable = false)
    private Double total;
    
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles;
}
