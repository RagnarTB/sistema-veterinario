import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface Producto {
  id: number;
  nombre: string;
  descripcion?: string;
  precio: number;
  activo: boolean;
  marca?: string;
  categoriaId?: number;
  categoriaNombre?: string;
  unidadCompraId?: number;
  unidadCompraNombre?: string;
  unidadVentaId?: number;
  unidadVentaNombre?: string;
  factorConversion?: number;
}

export interface ProductoPage {
  content: Producto[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class ProductoService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/productos`;

  listar(buscar: string = '', page: number = 0, size: number = 10): Observable<ProductoPage> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (buscar) {
      params = params.set('buscar', buscar);
    }

    return this.http.get<ProductoPage>(this.apiUrl, { params });
  }

  buscarPorId(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.apiUrl}/${id}`);
  }

  crear(producto: any): Observable<Producto> {
    return this.http.post<Producto>(this.apiUrl, producto);
  }

  actualizar(id: number, producto: any): Observable<Producto> {
    return this.http.put<Producto>(`${this.apiUrl}/${id}`, producto);
  }

  cambiarEstado(id: number, activo: boolean): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/estado?activo=${activo}`, {});
  }
}
