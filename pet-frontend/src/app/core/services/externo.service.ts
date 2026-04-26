import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ReniecResponse {
  first_name: string;
  first_last_name: string;
  second_last_name: string;
  full_name: string;
  document_number: string;
}

@Injectable({
  providedIn: 'root'
})
export class ExternoService {

  private apiUrl = `${environment.apiUrl}/externo`;

  constructor(private http: HttpClient) {}

  consultarDni(dni: string): Observable<ReniecResponse> {
    return this.http.get<ReniecResponse>(`${this.apiUrl}/reniec/dni/${dni}`);
  }
}
