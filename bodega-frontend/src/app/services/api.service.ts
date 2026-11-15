import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Producto } from '../models/producto.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8888/api';

  constructor(private http: HttpClient) {}

  login(credentials: { username: string; password: string }): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/auth/login`, credentials);
  }

  getProductos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.baseUrl}/productos`);
  }

  crearProducto(producto: Producto): Observable<Producto> {
    return this.http.post<Producto>(`${this.baseUrl}/productos`, producto);
  }

  eliminarProducto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/productos/${id}`);
  }

  getVentas(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/ventas`);
  }

  registrarVenta(venta: { items: { productoId: number; cantidad: number }[] }): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/ventas`, venta);
  }

  getBoleta(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/ventas/${id}/boleta`);
  }
}
