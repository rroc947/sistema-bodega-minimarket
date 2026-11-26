package com.bodega.pedidos.repository;

import com.bodega.pedidos.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findByFechaBetweenOrderByFechaDesc(LocalDateTime inicio, LocalDateTime fin);
}
