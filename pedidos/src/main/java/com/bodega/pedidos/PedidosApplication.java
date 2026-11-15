package com.bodega.pedidos;

import com.bodega.pedidos.entity.DetalleVenta;
import com.bodega.pedidos.entity.Producto;
import com.bodega.pedidos.entity.Usuario;
import com.bodega.pedidos.entity.Venta;
import com.bodega.pedidos.repository.ProductoRepository;
import com.bodega.pedidos.repository.UsuarioRepository;
import com.bodega.pedidos.repository.VentaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class PedidosApplication {

	public static void main(String[] args) {
		SpringApplication.run(PedidosApplication.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(UsuarioRepository usuarioRepository, ProductoRepository productoRepository, VentaRepository ventaRepository) {
		return args -> {
			if (usuarioRepository.findByUsername("encargado").isEmpty()) {
				Usuario encargado = new Usuario();
				encargado.setUsername("encargado");
				encargado.setPassword("123");
				encargado.setRol("ROLE_ENCARGADO");
				usuarioRepository.save(encargado);
			} else {
				Usuario encargado = usuarioRepository.findByUsername("encargado").get();
				if (!"123".equals(encargado.getPassword()) && !encargado.getPassword().startsWith("$2")) {
					encargado.setPassword("123");
					usuarioRepository.save(encargado);
				}
			}

			if (usuarioRepository.findByUsername("cajero").isEmpty()) {
				Usuario cajero = new Usuario();
				cajero.setUsername("cajero");
				cajero.setPassword("123");
				cajero.setRol("ROLE_CAJERO");
				usuarioRepository.save(cajero);
			} else {
				Usuario cajero = usuarioRepository.findByUsername("cajero").get();
				if (!"123".equals(cajero.getPassword()) && !cajero.getPassword().startsWith("$2")) {
					cajero.setPassword("123");
					usuarioRepository.save(cajero);
				}
			}

			if (productoRepository.count() == 0) {
				List<Producto> productos = new ArrayList<>();
				
				String[] nombres = {
					"Arroz Costeño 1kg", "Aceite Primor 1L", "Leche Gloria Azul 400g", 
					"Azúcar Rubia 1kg", "Atún Florida 170g", "Fideos Don Vittorio 500g", 
					"Detergente Bolívar 1kg", "Papel Higiénico Scott x4", "Agua San Luis 2.5L", "Galletas Casino"
				};
				String[] descripciones = {
					"Arroz extra superior", "Aceite vegetal", "Leche evaporada entera", 
					"Azúcar rubia en bolsa", "Atún en aceite vegetal", "Fideos tallarín 500g", 
					"Detergente en polvo", "Papel higiénico doble hoja", "Agua mineral sin gas", "Galletas rellenas"
				};
				Double[] precios = { 4.50, 9.80, 4.20, 4.00, 5.50, 3.20, 11.50, 6.80, 3.50, 1.20 };
				Integer[] stocks = { 60, 40, 50, 70, 40, 50, 30, 35, 60, 100 };
				String[] categorias = { "Abarrotes", "Abarrotes", "Lácteos", "Abarrotes", "Enlatados", "Abarrotes", "Limpieza", "Limpieza", "Bebidas", "Golosinas" };

				for (int i = 0; i < 10; i++) {
					Producto p = new Producto();
					p.setNombre(nombres[i]);
					p.setDescripcion(descripciones[i]);
					p.setPrecio(precios[i]);
					p.setStock(stocks[i]);
					p.setCategoria(categorias[i]);
					productos.add(productoRepository.save(p));
				}

				for (int v = 1; v <= 10; v++) {
					Venta venta = new Venta();
					venta.setFecha(LocalDateTime.now().minusHours(12 - v));
					List<DetalleVenta> detalles = new ArrayList<>();
					double sumaSubtotales = 0.0;

					int productoIndex = (v - 1) % productos.size();
					Producto prod = productos.get(productoIndex);
					int cantidad = (v % 3) + 1;

					if (prod.getStock() >= cantidad) {
						DetalleVenta detalle = new DetalleVenta();
						detalle.setVenta(venta);
						detalle.setProducto(prod);
						detalle.setCantidad(cantidad);
						detalle.setPrecioUnitario(prod.getPrecio());
						double subtotalDetalle = prod.getPrecio() * cantidad;
						detalle.setSubtotal(Math.round(subtotalDetalle * 100.0) / 100.0);

						sumaSubtotales += subtotalDetalle;
						detalles.add(detalle);

						prod.setStock(prod.getStock() - cantidad);
						productoRepository.save(prod);
					}

					double igv = sumaSubtotales * 0.18;
					double totalFinal = sumaSubtotales + igv;

					venta.setSubtotal(Math.round(sumaSubtotales * 100.0) / 100.0);
					venta.setIgv(Math.round(igv * 100.0) / 100.0);
					venta.setTotal(Math.round(totalFinal * 100.0) / 100.0);
					venta.setDetalles(detalles);

					ventaRepository.save(venta);
				}
			}
		};
	}
}
