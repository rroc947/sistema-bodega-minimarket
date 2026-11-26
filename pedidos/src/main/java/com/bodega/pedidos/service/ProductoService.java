package com.bodega.pedidos.service;

import com.bodega.pedidos.entity.Producto;
import com.bodega.pedidos.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {
    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    public Page<Producto> listarProductosPaginados(Pageable pageable) {
        return productoRepository.findAll(pageable);
    }

    public List<Producto> buscarProductos(String nombre, String categoria, Double precioMax) {
        return productoRepository.buscarPorFiltros(nombre, categoria, precioMax);
    }

    public Optional<Producto> obtenerPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Producto guardarProducto(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre del producto no puede estar vacío");
        }
        String nombreLimpio = producto.getNombre().trim();
        Optional<Producto> existente = productoRepository.findByNombreIgnoreCase(nombreLimpio);
        if (existente.isPresent()) {
            throw new RuntimeException("Error: Ya existe un producto registrado con el nombre '" + nombreLimpio + "'. No se permiten nombres duplicados.");
        }
        producto.setNombre(nombreLimpio);
        if (producto.getMarca() != null) {
            producto.setMarca(producto.getMarca().trim());
        }
        if (producto.getImagenUrl() != null) {
            producto.setImagenUrl(producto.getImagenUrl().trim());
        }
        return productoRepository.save(producto);
    }

    public Producto actualizarProducto(Long id, Producto productoDetalles) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        if (productoDetalles.getNombre() == null || productoDetalles.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre del producto no puede estar vacío");
        }

        String nuevoNombre = productoDetalles.getNombre().trim();
        Optional<Producto> conMismoNombre = productoRepository.findByNombreIgnoreCase(nuevoNombre);
        if (conMismoNombre.isPresent() && !conMismoNombre.get().getId().equals(id)) {
            throw new RuntimeException("Error: Ya existe otro producto registrado con el nombre '" + nuevoNombre + "'. No se permiten nombres duplicados.");
        }

        // Se permite editar nombre, precio, marca, categoría, descripción e imagenUrl, pero NO se altera el stock
        producto.setNombre(nuevoNombre);
        if (productoDetalles.getPrecio() != null && productoDetalles.getPrecio() > 0) {
            producto.setPrecio(productoDetalles.getPrecio());
        }
        producto.setMarca(productoDetalles.getMarca() != null ? productoDetalles.getMarca().trim() : null);
        producto.setDescripcion(productoDetalles.getDescripcion() != null ? productoDetalles.getDescripcion().trim() : null);
        producto.setCategoria(productoDetalles.getCategoria() != null ? productoDetalles.getCategoria().trim() : null);
        producto.setImagenUrl(productoDetalles.getImagenUrl() != null ? productoDetalles.getImagenUrl().trim() : null);
        // El stock se mantiene inalterable: no se toma productoDetalles.getStock()
        return productoRepository.save(producto);
    }

    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        productoRepository.deleteById(id);
    }
}
