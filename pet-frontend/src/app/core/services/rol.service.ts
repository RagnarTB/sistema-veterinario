import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RolRequest, RolResponse } from '../models/models';

@Injectable({ providedIn: 'root' })
export class RolService {
  private url = `${environment.apiUrl}/roles`;
  constructor(private http: HttpClient) {}

  listarTodos(): Observable<RolResponse[]> {
    return this.http.get<RolResponse[]>(this.url);
  }

  crear(dto: RolRequest): Observable<RolResponse> {
    return this.http.post<RolResponse>(this.url, dto);
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
