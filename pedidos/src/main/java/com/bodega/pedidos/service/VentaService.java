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
}
