import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface CategoriaProducto {
  id: number;
  nombre: string;
  descripcion?: string;
}

export interface UnidadMedida {
  id: number;
  nombre: string;
  abreviatura: string;
}

export interface Proveedor {
  id: number;
  razonSocial: string;
  ruc?: string;
  contacto?: string;
  telefono?: string;
  email?: string;
  direccion?: string;
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
  crearCategoria(cat: Partial<CategoriaProducto>): Observable<CategoriaProducto> {
    return this.http.post<CategoriaProducto>(`${this.apiUrl}/categorias`, cat);
  }
  actualizarCategoria(id: number, cat: Partial<CategoriaProducto>): Observable<CategoriaProducto> {
    return this.http.put<CategoriaProducto>(`${this.apiUrl}/categorias/${id}`, cat);
  }
  eliminarCategoria(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/categorias/${id}`);
  }

  // Unidades
  listarUnidades(): Observable<UnidadMedida[]> {
    return this.http.get<UnidadMedida[]>(`${this.apiUrl}/unidades`);
  }
  crearUnidad(uni: Partial<UnidadMedida>): Observable<UnidadMedida> {
    return this.http.post<UnidadMedida>(`${this.apiUrl}/unidades`, uni);
  }
  actualizarUnidad(id: number, uni: Partial<UnidadMedida>): Observable<UnidadMedida> {
    return this.http.put<UnidadMedida>(`${this.apiUrl}/unidades/${id}`, uni);
  }

  // Proveedores
  listarProveedores(): Observable<Proveedor[]> {
    return this.http.get<Proveedor[]>(`${this.apiUrl}/proveedores`);
  }
  crearProveedor(prov: Partial<Proveedor>): Observable<Proveedor> {
    return this.http.post<Proveedor>(`${this.apiUrl}/proveedores`, prov);
  }
  actualizarProveedor(id: number, prov: Partial<Proveedor>): Observable<Proveedor> {
    return this.http.put<Proveedor>(`${this.apiUrl}/proveedores/${id}`, prov);
  }
}
