package com.bodega.pedidos.service;

import com.bodega.pedidos.dto.ItemVentaDTO;
import com.bodega.pedidos.dto.VentaRequestDTO;
import com.bodega.pedidos.entity.DetalleVenta;
import com.bodega.pedidos.entity.Producto;
import com.bodega.pedidos.entity.Venta;
import com.bodega.pedidos.repository.ProductoRepository;
import com.bodega.pedidos.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Transactional
    public Venta registrarVenta(VentaRequestDTO request) {
        Venta venta = new Venta();
        venta.setFecha(LocalDateTime.now());
        
        List<DetalleVenta> detalles = new ArrayList<>();
        double sumaSubtotales = 0.0;

        for (ItemVentaDTO item : request.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + item.getProductoId()));

            if (producto.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre() + 
                        " (Stock actual: " + producto.getStock() + ", Solicitado: " + item.getCantidad() + ")");
            }

            // Descontar stock
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            // Crear detalle
            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            double subtotalDetalle = producto.getPrecio() * item.getCantidad();
            detalle.setSubtotal(Math.round(subtotalDetalle * 100.0) / 100.0);

            sumaSubtotales += subtotalDetalle;
            detalles.add(detalle);
        }

        // Cálculo de IGV (18%)
        double igv = sumaSubtotales * 0.18;
        double totalFinal = sumaSubtotales + igv;

        venta.setSubtotal(Math.round(sumaSubtotales * 100.0) / 100.0);
        venta.setIgv(Math.round(igv * 100.0) / 100.0);
        venta.setTotal(Math.round(totalFinal * 100.0) / 100.0);
        venta.setDetalles(detalles);

        return ventaRepository.save(venta);
    }

    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    public Optional<Venta> obtenerPorId(Long id) {
        return ventaRepository.findById(id);
    }

    public com.bodega.pedidos.dto.CierreCajaDTO obtenerCierreCajaDiario(java.time.LocalDate fecha) {
        java.time.LocalDateTime inicio = fecha.atStartOfDay();
        java.time.LocalDateTime fin = fecha.atTime(java.time.LocalTime.MAX);

        List<Venta> ventasDelDia = ventaRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin);

        java.util.Map<Long, com.bodega.pedidos.dto.ResumenProductoVendidoDTO> mapaResumen = new java.util.LinkedHashMap<>();
        int totalUnidades = 0;
        double subtotalAcumulado = 0.0;
        double igvAcumulado = 0.0;
        double totalAcumulado = 0.0;

        for (Venta v : ventasDelDia) {
            subtotalAcumulado += (v.getSubtotal() != null ? v.getSubtotal() : 0.0);
            igvAcumulado += (v.getIgv() != null ? v.getIgv() : 0.0);
            totalAcumulado += (v.getTotal() != null ? v.getTotal() : 0.0);

            if (v.getDetalles() != null) {
                for (DetalleVenta d : v.getDetalles()) {
                    Producto p = d.getProducto();
                    if (p != null) {
                        Long pId = p.getId();
                        int cant = d.getCantidad() != null ? d.getCantidad() : 0;
                        double sub = d.getSubtotal() != null ? d.getSubtotal() : 0.0;
                        totalUnidades += cant;

                        if (mapaResumen.containsKey(pId)) {
                            com.bodega.pedidos.dto.ResumenProductoVendidoDTO dto = mapaResumen.get(pId);
                            dto.setCantidadTotal(dto.getCantidadTotal() + cant);
                            dto.setTotalRecaudado(Math.round((dto.getTotalRecaudado() + sub) * 100.0) / 100.0);
                        } else {
                            com.bodega.pedidos.dto.ResumenProductoVendidoDTO dto = new com.bodega.pedidos.dto.ResumenProductoVendidoDTO(
                                    p.getId(),
                                    p.getNombre(),
                                    p.getMarca(),
                                    p.getCategoria(),
                                    p.getImagenUrl(),
                                    cant,
                                    d.getPrecioUnitario(),
                                    Math.round(sub * 100.0) / 100.0
                            );
                            mapaResumen.put(pId, dto);
                        }
                    }
                }
            }
        }

        com.bodega.pedidos.dto.CierreCajaDTO cierre = new com.bodega.pedidos.dto.CierreCajaDTO();
        cierre.setFecha(fecha);
        cierre.setTotalTransacciones(ventasDelDia.size());
        cierre.setTotalUnidadesVendidas(totalUnidades);
        cierre.setSubtotal(Math.round(subtotalAcumulado * 100.0) / 100.0);
        cierre.setIgv(Math.round(igvAcumulado * 100.0) / 100.0);
        cierre.setTotal(Math.round(totalAcumulado * 100.0) / 100.0);
        cierre.setProductosVendidos(new java.util.ArrayList<>(mapaResumen.values()));
        cierre.setVentasDelDia(ventasDelDia);

        return cierre;
    }
}
