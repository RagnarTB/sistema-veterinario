import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface DiaBloqueadoRequest {
  fecha: string;
  motivo: string;
  veterinarioId?: number;
}

export interface DiaBloqueadoResponse {
  id: number;
  fecha: string;
  motivo: string;
  veterinarioId?: number;
}

@Injectable({ providedIn: 'root' })
export class DiaBloqueadoService {
  private url = `${environment.apiUrl}/dias-bloqueados`;

  constructor(private http: HttpClient) {}

  listar(): Observable<DiaBloqueadoResponse[]> {
    return this.http.get<DiaBloqueadoResponse[]>(this.url);
  }

  crear(dto: DiaBloqueadoRequest): Observable<DiaBloqueadoResponse> {
    return this.http.post<DiaBloqueadoResponse>(this.url, dto);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
