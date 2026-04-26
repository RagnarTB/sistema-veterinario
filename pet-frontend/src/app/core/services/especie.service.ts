import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { EspecieResponse } from '../models/models';

@Injectable({ providedIn: 'root' })
export class EspecieService {
  private url = `${environment.apiUrl}/especies`;
  constructor(private http: HttpClient) {}

  listar(): Observable<EspecieResponse[]> {
    return this.http.get<EspecieResponse[]>(this.url);
  }

  crear(dto: any): Observable<EspecieResponse> {
    return this.http.post<EspecieResponse>(this.url, dto);
  }

  actualizar(id: number, dto: any): Observable<EspecieResponse> {
    return this.http.put<EspecieResponse>(`${this.url}/${id}`, dto);
  }

  cambiarEstado(id: number): Observable<void> {
    return this.http.patch<void>(`${this.url}/${id}/estado`, {});
  }

  eliminarFisicamente(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
