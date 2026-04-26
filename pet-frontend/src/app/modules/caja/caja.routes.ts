import { Routes } from '@angular/router';
export const CAJA_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./caja.component').then(m => m.CajaComponent) },
];
