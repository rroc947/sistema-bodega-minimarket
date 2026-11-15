import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from './services/api.service';
import { Producto } from './models/producto.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  isLoggedIn = false;
  currentUser: any = null;
  loginData = { username: '', password: '' };

  currentView: string = 'menu';
  ventaForm = {
    productoId: null as number | null,
    cantidad: 1
  };

  productos: Producto[] = [];
  ventas: any[] = [];
  nuevoProducto: Producto = { nombre: '', precio: 0, stock: 0, descripcion: '', categoria: '' };

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    const savedUser = sessionStorage.getItem('currentUser');
    if (savedUser) {
      this.currentUser = JSON.parse(savedUser);
      this.isLoggedIn = true;
      this.cargarDatos();
    }
  }

  iniciarSesion() {
    if (!this.loginData.username || !this.loginData.password) {
      alert('Por favor ingrese usuario y contraseña.');
      return;
    }

    this.apiService.login(this.loginData).subscribe({
      next: (user) => {
        this.currentUser = user;
        this.isLoggedIn = true;
        this.currentView = 'menu';
        if (user.token) {
          sessionStorage.setItem('token', user.token);
        }
        sessionStorage.setItem('currentUser', JSON.stringify(user));
        this.cargarDatos();
      },
      error: (err) => {
        alert('Error de autenticación: ' + (err.error || 'Credenciales incorrectas'));
      }
    });
  }

  cerrarSesion() {
    this.isLoggedIn = false;
    this.currentUser = null;
    this.currentView = 'menu';
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('currentUser');
    sessionStorage.clear();
  }

  cambiarVista(vista: string) {
    this.currentView = vista;
  }

  cargarDatos() {
    this.cargarProductos();
    if (this.currentUser?.rol === 'ROLE_ENCARGADO') {
      this.cargarVentas();
    }
  }

  cargarProductos() {
    this.apiService.getProductos().subscribe({
      next: (data) => (this.productos = data),
      error: (err) => console.error('Error al cargar productos', err)
    });
  }

  cargarVentas() {
    this.apiService.getVentas().subscribe({
      next: (data) => (this.ventas = data),
      error: (err) => console.error('Error al cargar ventas', err)
    });
  }

  guardarProducto() {
    if (!this.nuevoProducto.nombre || !this.nuevoProducto.precio || this.nuevoProducto.precio <= 0) {
      alert('Por favor ingrese un nombre y un precio válido.');
      return;
    }
    this.apiService.crearProducto(this.nuevoProducto).subscribe({
      next: () => {
        this.cargarProductos();
        this.nuevoProducto = { nombre: '', precio: 0, stock: 0, descripcion: '', categoria: '' };
        alert('¡Producto guardado exitosamente!');
      },
      error: (err) => alert('Error al guardar: ' + (err.error?.message || err.error || 'Verifique los datos'))
    });
  }

  eliminar(id?: number) {
    if (!id) return;
    if (confirm('¿Está seguro de eliminar este producto?')) {
      this.apiService.eliminarProducto(id).subscribe({
        next: () => this.cargarProductos(),
        error: (err) => alert('Error al eliminar el producto')
      });
    }
  }

  realizarVenta() {
    if (!this.ventaForm.productoId) {
      alert('Por favor seleccione un producto.');
      return;
    }
    if (!this.ventaForm.cantidad || this.ventaForm.cantidad <= 0) {
      alert('La cantidad debe ser mayor a 0.');
      return;
    }

    const venta = {
      items: [
        {
          productoId: Number(this.ventaForm.productoId),
          cantidad: Number(this.ventaForm.cantidad)
        }
      ]
    };

    this.apiService.registrarVenta(venta).subscribe({
      next: (res) => {
        this.cargarProductos();
        if (this.currentUser?.rol === 'ROLE_ENCARGADO') {
          this.cargarVentas();
        }
        this.ventaForm = { productoId: null, cantidad: 1 };
        this.verBoleta(res.id);
      },
      error: (err) => alert('Error al registrar venta: ' + (err.error || err.message || 'Error desconocido'))
    });
  }

  comprar(productoId: number) {
    const venta = { items: [{ productoId, cantidad: 1 }] };
    this.apiService.registrarVenta(venta).subscribe({
      next: (res) => {
        alert(
          `¡Venta registrada con éxito!\nSubtotal: S/. ${res.subtotal}\nIGV (18%): S/. ${res.igv}\nTotal: S/. ${res.total}`
        );
        this.cargarProductos();
        if (this.currentUser?.rol === 'ROLE_ENCARGADO') {
          this.cargarVentas();
        }
      },
      error: (err) => alert('Error al registrar venta: ' + (err.error || err.message))
    });
  }

  verBoleta(id?: number) {
    if (!id) return;
    this.apiService.getBoleta(id).subscribe({
      next: (boleta) => {
        let htmlContent = `
          <html>
            <head>
              <title>Boleta de Venta N° ${boleta.id}</title>
              <style>
                body { font-family: 'Courier New', Courier, monospace; font-size: 14px; width: 320px; margin: auto; padding: 20px; }
                h3, h4 { text-align: center; margin: 5px 0; }
                .line { border-bottom: 1px dashed #000; margin: 10px 0; }
                table { width: 100%; font-size: 13px; }
                .text-right { text-align: right; }
              </style>
            </head>
            <body onload="window.print()">
              <h3>BODEGA / MINIMARKET</h3>
              <h4>RUC: 20123456789</h4>
              <p style="text-align: center; font-size: 12px;">Av. Principal 123 - Lima<br>BOLETA DE VENTA ELECTRÓNICA</p>
              <div class="line"></div>
              <p><b>N° Venta:</b> B001-${boleta.id}</p>
              <p><b>Fecha:</b> ${new Date(boleta.fecha).toLocaleString()}</p>
              <div class="line"></div>
              <table>
                <tr>
                  <th>Cant</th>
                  <th>Descripción</th>
                  <th class="text-right">Subt</th>
                </tr>
        `;

        boleta.detalles.forEach((d: any) => {
          htmlContent += `
            <tr>
              <td>${d.cantidad}</td>
              <td>${d.producto.nombre}</td>
              <td class="text-right">S/. ${d.subtotal.toFixed(2)}</td>
            </tr>
          `;
        });

        htmlContent += `
              </table>
              <div class="line"></div>
              <p class="text-right"><b>Subtotal:</b> S/. ${boleta.subtotal.toFixed(2)}</p>
              <p class="text-right"><b>IGV (18%):</b> S/. ${boleta.igv.toFixed(2)}</p>
              <p class="text-right" style="font-size: 16px;"><b>TOTAL: S/. ${boleta.total.toFixed(2)}</b></p>
              <div class="line"></div>
              <p style="text-align: center; font-size: 11px;">¡Gracias por su compra!<br>Representación impresa de Boleta Electrónica</p>
            </body>
          </html>
        `;

        const printWindow = window.open('', '_blank', 'width=400,height=600');
        if (printWindow) {
          printWindow.document.write(htmlContent);
          printWindow.document.close();
        }
      },
      error: () => alert('No se pudo cargar la boleta para impresión')
    });
  }
}
