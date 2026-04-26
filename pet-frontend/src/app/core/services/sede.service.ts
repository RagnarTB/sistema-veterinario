import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SedeRequest, SedeResponse } from '../models/models';

@Injectable({ providedIn: 'root' })
export class SedeService {
  private url = `${environment.apiUrl}/sedes`;
  constructor(private http: HttpClient) {}

  listar(): Observable<SedeResponse[]> {
    return this.http.get<SedeResponse[]>(this.url);
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
}
