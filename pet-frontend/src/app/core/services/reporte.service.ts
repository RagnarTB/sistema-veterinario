import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardResumen, TopProducto, CitasVeterinario } from '../models/models';

@Injectable({ providedIn: 'root' })
export class ReporteService {
  private url = `${environment.apiUrl}/reportes`;
  constructor(private http: HttpClient) {}

  getDashboard(): Observable<DashboardResumen> {
    return this.http.get<DashboardResumen>(`${this.url}/dashboard`);
  }

  getTopProductos(): Observable<TopProducto[]> {
    return this.http.get<TopProducto[]>(`${this.url}/top-productos`);
  }

  getRendimientoVeterinarios(): Observable<CitasVeterinario[]> {
    return this.http.get<CitasVeterinario[]>(`${this.url}/rendimiento-veterinarios`);
  }
}
