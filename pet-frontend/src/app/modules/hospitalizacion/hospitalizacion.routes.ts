import { Routes } from '@angular/router';
export const HOSPITALIZACION_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./hospitalizacion.component').then(m => m.HospitalizacionComponent) },
];
