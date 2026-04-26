import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PacienteRequest, PacienteResponse, Page } from '../models/models';

@Injectable({ providedIn: 'root' })
export class PacienteService {
  private url = `${environment.apiUrl}/pacientes`;
  constructor(private http: HttpClient) {}

  listar(page = 0, size = 10, buscar = ''): Observable<Page<PacienteResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (buscar) params = params.set('buscar', buscar);
    return this.http.get<Page<PacienteResponse>>(this.url, { params });
  }

  buscarPorId(id: number): Observable<PacienteResponse> {
    return this.http.get<PacienteResponse>(`${this.url}/${id}`);
  }

  crear(dto: PacienteRequest): Observable<PacienteResponse> {
    return this.http.post<PacienteResponse>(this.url, dto);
  }

  actualizar(id: number, dto: PacienteRequest): Observable<PacienteResponse> {
    return this.http.put<PacienteResponse>(`${this.url}/${id}`, dto);
  }

  cambiarEstado(id: number, activo: boolean): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}/estado`, {
      params: new HttpParams().set('activo', activo),
    });
  }
}
