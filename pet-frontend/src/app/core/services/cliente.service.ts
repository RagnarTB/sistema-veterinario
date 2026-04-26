import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ClienteRequest, ClienteResponse, Page } from '../models/models';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private url = `${environment.apiUrl}/clientes`;
  constructor(private http: HttpClient) {}

  listar(page = 0, size = 10, buscar = ''): Observable<Page<ClienteResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (buscar) params = params.set('buscar', buscar);
    return this.http.get<Page<ClienteResponse>>(this.url, { params });
  }

  buscarPorId(id: number): Observable<ClienteResponse> {
    return this.http.get<ClienteResponse>(`${this.url}/${id}`);
  }

  crear(dto: ClienteRequest): Observable<ClienteResponse> {
    return this.http.post<ClienteResponse>(this.url, dto);
  }

  actualizar(id: number, dto: ClienteRequest): Observable<ClienteResponse> {
    return this.http.put<ClienteResponse>(`${this.url}/${id}`, dto);
  }

  cambiarEstado(id: number, activo: boolean): Observable<void> {
    return this.http.patch<void>(`${this.url}/${id}/estado`, null, {
      params: new HttpParams().set('activo', activo),
    });
  }
}
