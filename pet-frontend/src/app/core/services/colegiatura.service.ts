import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ColegiaturaValidacion } from '../models/models';

@Injectable({ providedIn: 'root' })
export class ColegiaturaService {
  private readonly apiUrl = `${environment.apiUrl}/colegiaturas`;

  constructor(private http: HttpClient) {}

  validar(numeroColegiatura: string): Observable<ColegiaturaValidacion> {
    return this.http.get<ColegiaturaValidacion>(`${this.apiUrl}/validar/${numeroColegiatura}`);
  }
}
