package com.bodega.pedidos.controller;

import com.bodega.pedidos.dto.VentaRequestDTO;
import com.bodega.pedidos.entity.Venta;
import com.bodega.pedidos.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @GetMapping
    public List<Venta> listar() {
        return ventaService.listarVentas();
    }

    @GetMapping("/{id}/boleta")
    public ResponseEntity<?> obtenerBoleta(@PathVariable Long id) {
        Optional<Venta> venta = ventaService.obtenerPorId(id);
        if (venta.isPresent()) {
            return ResponseEntity.ok(venta.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Boleta de venta no encontrada con ID: " + id);
        }
    }

    @GetMapping("/cierre-caja")
    public ResponseEntity<?> obtenerCierreCaja(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fecha) {
        if (fecha == null) {
            fecha = java.time.LocalDate.now();
        }
        return ResponseEntity.ok(ventaService.obtenerCierreCajaDiario(fecha));
    }

    @PostMapping
    public ResponseEntity<?> registrarVenta(@Valid @RequestBody VentaRequestDTO request) {
        try {
            Venta nuevaVenta = ventaService.registrarVenta(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaVenta);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
