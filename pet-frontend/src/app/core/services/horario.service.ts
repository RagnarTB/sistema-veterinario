import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface HorarioVeterinarioRequest {
  veterinarioId: number;
  diaSemana: string;
  horaEntrada: string;
  horaSalida: string;
  inicioRefrigerio?: string;
  finRefrigerio?: string;
  sedeId: number;
}

export interface HorarioVeterinarioResponse {
  id: number;
  veterinarioId: number;
  diaSemana: string;
  horaEntrada: string;
  horaSalida: string;
  inicioRefrigerio?: string;
  finRefrigerio?: string;
  sedeId: number;
}

@Injectable({ providedIn: 'root' })
export class HorarioService {
  private url = `${environment.apiUrl}/horarios`;

  constructor(private http: HttpClient) {}

  listar(): Observable<HorarioVeterinarioResponse[]> {
    return this.http.get<HorarioVeterinarioResponse[]>(this.url);
  }

  crear(dto: HorarioVeterinarioRequest): Observable<HorarioVeterinarioResponse> {
    return this.http.post<HorarioVeterinarioResponse>(this.url, dto);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
