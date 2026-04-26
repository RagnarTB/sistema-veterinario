import { Routes } from '@angular/router';
export const SEDES_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./sedes.component').then(m => m.SedesComponent) },
];
