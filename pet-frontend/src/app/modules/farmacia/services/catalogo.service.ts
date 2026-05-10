import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface CategoriaProducto {
  id: number;
  nombre: string;
  descripcion?: string;
  activo?: boolean;
}

export interface UnidadMedida {
  id: number;
  nombre: string;
  abreviatura: string;
  activo?: boolean;
}

export interface Proveedor {
  id: number;
  razonSocial: string;
  ruc?: string;
  contacto?: string;
  telefono?: string;
  email?: string;
  direccion?: string;
  activo?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class CatalogoService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/catalogos`;

  // Categorías
  listarCategorias(): Observable<CategoriaProducto[]> {
    return this.http.get<CategoriaProducto[]>(`${this.apiUrl}/categorias`);
  }
  listarCategoriasTodas(): Observable<CategoriaProducto[]> {
    return this.http.get<CategoriaProducto[]>(`${this.apiUrl}/categorias/todas`);
  }
  crearCategoria(cat: Partial<CategoriaProducto>): Observable<CategoriaProducto> {
    return this.http.post<CategoriaProducto>(`${this.apiUrl}/categorias`, cat);
  }
  actualizarCategoria(id: number, cat: Partial<CategoriaProducto>): Observable<CategoriaProducto> {
    return this.http.put<CategoriaProducto>(`${this.apiUrl}/categorias/${id}`, cat);
  }
  activarCategoria(id: number): Observable<CategoriaProducto> {
    return this.http.put<CategoriaProducto>(`${this.apiUrl}/categorias/${id}/activar`, {});
  }
  eliminarCategoria(id: number): Observable<{accion: string; mensaje: string}> {
    return this.http.delete<{accion: string; mensaje: string}>(`${this.apiUrl}/categorias/${id}`);
  }

  // Unidades
  listarUnidades(): Observable<UnidadMedida[]> {
    return this.http.get<UnidadMedida[]>(`${this.apiUrl}/unidades`);
  }
  listarUnidadesTodas(): Observable<UnidadMedida[]> {
    return this.http.get<UnidadMedida[]>(`${this.apiUrl}/unidades/todas`);
  }
  crearUnidad(uni: Partial<UnidadMedida>): Observable<UnidadMedida> {
    return this.http.post<UnidadMedida>(`${this.apiUrl}/unidades`, uni);
  }
  actualizarUnidad(id: number, uni: Partial<UnidadMedida>): Observable<UnidadMedida> {
    return this.http.put<UnidadMedida>(`${this.apiUrl}/unidades/${id}`, uni);
  }
  activarUnidad(id: number): Observable<UnidadMedida> {
    return this.http.put<UnidadMedida>(`${this.apiUrl}/unidades/${id}/activar`, {});
  }
  eliminarUnidad(id: number): Observable<{accion: string; mensaje: string}> {
    return this.http.delete<{accion: string; mensaje: string}>(`${this.apiUrl}/unidades/${id}`);
  }

  // Proveedores
  listarProveedores(): Observable<Proveedor[]> {
    return this.http.get<Proveedor[]>(`${this.apiUrl}/proveedores`);
  }
  listarProveedoresTodas(): Observable<Proveedor[]> {
    return this.http.get<Proveedor[]>(`${this.apiUrl}/proveedores/todas`);
  }
  crearProveedor(prov: Partial<Proveedor>): Observable<Proveedor> {
    return this.http.post<Proveedor>(`${this.apiUrl}/proveedores`, prov);
  }
  actualizarProveedor(id: number, prov: Partial<Proveedor>): Observable<Proveedor> {
    return this.http.put<Proveedor>(`${this.apiUrl}/proveedores/${id}`, prov);
  }
  activarProveedor(id: number): Observable<Proveedor> {
    return this.http.put<Proveedor>(`${this.apiUrl}/proveedores/${id}/activar`, {});
  }
  eliminarProveedor(id: number): Observable<{accion: string; mensaje: string}> {
    return this.http.delete<{accion: string; mensaje: string}>(`${this.apiUrl}/proveedores/${id}`);
  }

  // SUNAT RUC
  consultarRuc(ruc: string): Observable<any> {
    return this.http.get<any>(`${environment.apiUrl}/externo/reniec/sunat/ruc/${ruc}`);
  }
}
