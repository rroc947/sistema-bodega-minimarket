package com.bodega.pedidos.dto;

import com.bodega.pedidos.entity.Venta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CierreCajaDTO {
    private LocalDate fecha;
    private int totalTransacciones;
    private int totalUnidadesVendidas;
    private Double subtotal;
    private Double igv;
    private Double total;
    private List<ResumenProductoVendidoDTO> productosVendidos;
    private List<Venta> ventasDelDia;
}
