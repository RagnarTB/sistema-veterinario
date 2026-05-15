import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ServicioMedicoRequest, ServicioMedicoResponse, ServicioMedicoInsumoRequest, ServicioMedicoInsumoResponse } from '../../core/models/models';

@Injectable({
  providedIn: 'root'
})
export class ServicioMedicoService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/servicios-medicos`;

  listar(activo?: boolean): Observable<ServicioMedicoResponse[]> {
    let params = new HttpParams();
    if (activo !== undefined) {
      params = params.set('activo', activo);
    }
    return this.http.get<ServicioMedicoResponse[]>(this.apiUrl, { params });
  }

  buscarPorId(id: number): Observable<ServicioMedicoResponse> {
    return this.http.get<ServicioMedicoResponse>(`${this.apiUrl}/${id}`);
  }

  crear(dto: ServicioMedicoRequest): Observable<ServicioMedicoResponse> {
    return this.http.post<ServicioMedicoResponse>(this.apiUrl, dto);
  }

  actualizar(id: number, dto: ServicioMedicoRequest): Observable<ServicioMedicoResponse> {
    return this.http.put<ServicioMedicoResponse>(`${this.apiUrl}/${id}`, dto);
  }

  cambiarEstado(id: number, activo: boolean): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/estado?activo=${activo}`, {});
  }

  // ========== INSUMOS ==========

  listarInsumos(servicioId: number): Observable<ServicioMedicoInsumoResponse[]> {
    return this.http.get<ServicioMedicoInsumoResponse[]>(`${this.apiUrl}/${servicioId}/insumos`);
  }

  agregarInsumo(servicioId: number, dto: ServicioMedicoInsumoRequest): Observable<ServicioMedicoInsumoResponse> {
    return this.http.post<ServicioMedicoInsumoResponse>(`${this.apiUrl}/${servicioId}/insumos`, dto);
  }

  actualizarInsumo(servicioId: number, insumoId: number, dto: ServicioMedicoInsumoRequest): Observable<ServicioMedicoInsumoResponse> {
    return this.http.put<ServicioMedicoInsumoResponse>(`${this.apiUrl}/${servicioId}/insumos/${insumoId}`, dto);
  }

  cambiarEstadoInsumo(servicioId: number, insumoId: number, activo: boolean): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${servicioId}/insumos/${insumoId}/estado?activo=${activo}`, {});
  }

  eliminarInsumo(servicioId: number, insumoId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${servicioId}/insumos/${insumoId}`);
  }
}
