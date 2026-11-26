package com.bodega.pedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenProductoVendidoDTO {
    private Long productoId;
    private String nombre;
    private String marca;
    private String categoria;
    private String imagenUrl;
    private Integer cantidadTotal;
    private Double precioUnitario;
    private Double totalRecaudado;
}
