import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SedeRequest, SedeResponse } from '../models/models';

@Injectable({ providedIn: 'root' })
export class SedeService {
  private url = `${environment.apiUrl}/sedes`;
  constructor(private http: HttpClient) {}

  listar(page: number = 0, size: number = 10, search: string = ''): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (search) {
      params = params.set('buscar', search);
    }
    return this.http.get<any>(this.url, { params });
  }

  buscarPorId(id: number): Observable<SedeResponse> {
    return this.http.get<SedeResponse>(`${this.url}/${id}`);
  }

  crear(dto: SedeRequest): Observable<SedeResponse> {
    return this.http.post<SedeResponse>(this.url, dto);
  }

  actualizar(id: number, dto: SedeRequest): Observable<SedeResponse> {
    return this.http.put<SedeResponse>(`${this.url}/${id}`, dto);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  cambiarEstado(id: number, estado: boolean): Observable<void> {
    return this.http.patch<void>(`${this.url}/${id}/estado`, null, {
      params: new HttpParams().set('estado', estado),
    });
  }
}
