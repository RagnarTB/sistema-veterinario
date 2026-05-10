import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface InventarioSede {
  id: number;
  productoId: number;
  sedeId: number;
  stockActual: number;
  stockMinimo: number;
  sede?: {
    id: number;
    nombre: string;
  };
}

export interface IngresoStockDTO {
  productoId: number;
  sedeId: number;
  proveedorId?: number | null;
  numeroLote: string;
  fechaVencimiento: string;
  cantidadComprada: number;
  motivo?: string;
}

export interface LoteInventario {
  id: number;
  numeroLote: string;
  fechaVencimiento: string;
  stockRestante: number;
  proveedorNombre?: string;
}

export interface MovimientoInventario {
  id: number;
  tipoMovimiento: string;
  cantidad: number;
  motivo: string;
  fecha: string;
  responsableNombre: string;
}

@Injectable({
  providedIn: 'root'
})
export class InventarioService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/inventario`;

  ajustarStock(dto: any): Observable<InventarioSede> {
    return this.http.post<InventarioSede>(`${this.apiUrl}/ajustar`, dto);
  }

  registrarIngreso(dto: IngresoStockDTO): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/ingreso`, dto);
  }

  obtenerLotes(productoId: number, sedeId: number): Observable<LoteInventario[]> {
    return this.http.get<LoteInventario[]>(`${this.apiUrl}/producto/${productoId}/sede/${sedeId}/lotes`);
  }

  obtenerMovimientos(productoId: number, sedeId: number): Observable<MovimientoInventario[]> {
    return this.http.get<MovimientoInventario[]>(`${this.apiUrl}/producto/${productoId}/sede/${sedeId}/movimientos`);
  }
}
