package com.bodega.pedidos.repository;

import com.bodega.pedidos.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p WHERE " +
           "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "(:categoria IS NULL OR LOWER(p.categoria) = LOWER(:categoria)) AND " +
           "(:precioMax IS NULL OR p.precio <= :precioMax)")
    List<Producto> buscarPorFiltros(
            @Param("nombre") String nombre,
            @Param("categoria") String categoria,
            @Param("precioMax") Double precioMax
    );
}
