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
        Optional<Producto> existente = productoRepository.findByNombreIgnoreCase(producto.getNombre().trim());
        if (existente.isPresent()) {
            Producto prod = existente.get();
            // Si ya existe el producto con ese nombre, se recarga el stock
            prod.setStock(prod.getStock() + (producto.getStock() != null ? producto.getStock() : 0));
            if (producto.getPrecio() != null && producto.getPrecio() > 0) {
                prod.setPrecio(producto.getPrecio());
            }
            if (producto.getMarca() != null && !producto.getMarca().trim().isEmpty()) {
                prod.setMarca(producto.getMarca().trim());
            }
            if (producto.getCategoria() != null && !producto.getCategoria().trim().isEmpty()) {
                prod.setCategoria(producto.getCategoria().trim());
            }
            if (producto.getDescripcion() != null && !producto.getDescripcion().trim().isEmpty()) {
                prod.setDescripcion(producto.getDescripcion().trim());
            }
            return productoRepository.save(prod);
        }
        producto.setNombre(producto.getNombre().trim());
        return productoRepository.save(producto);
    }

    public Producto actualizarProducto(Long id, Producto productoDetalles) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        // Se permite editar nombre, precio, marca, categoría y descripción, pero NO se altera el stock
        producto.setNombre(productoDetalles.getNombre());
        if (productoDetalles.getPrecio() != null && productoDetalles.getPrecio() > 0) {
            producto.setPrecio(productoDetalles.getPrecio());
        }
        producto.setMarca(productoDetalles.getMarca());
        producto.setDescripcion(productoDetalles.getDescripcion());
        producto.setCategoria(productoDetalles.getCategoria());
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
