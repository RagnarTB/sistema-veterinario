import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { EmpleadoRequest, EmpleadoResponse, Page } from '../models/models';

@Injectable({ providedIn: 'root' })
export class EmpleadoService {
  private url = `${environment.apiUrl}/empleados`;
  constructor(private http: HttpClient) {}

  listar(page = 0, size = 10, buscar = ''): Observable<Page<EmpleadoResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (buscar) params = params.set('buscar', buscar);
    return this.http.get<Page<EmpleadoResponse>>(this.url, { params });
  }

  buscarPorId(id: number): Observable<EmpleadoResponse> {
    return this.http.get<EmpleadoResponse>(`${this.url}/${id}`);
  }

  crear(dto: EmpleadoRequest): Observable<EmpleadoResponse> {
    return this.http.post<EmpleadoResponse>(this.url, dto);
  }

  actualizar(id: number, dto: EmpleadoRequest): Observable<EmpleadoResponse> {
    return this.http.put<EmpleadoResponse>(`${this.url}/${id}`, dto);
  }

  cambiarEstado(id: number, estado: boolean): Observable<void> {
    return this.http.patch<void>(`${this.url}/${id}/estado`, null, {
      params: new HttpParams().set('estado', estado),
    });
  }
}
