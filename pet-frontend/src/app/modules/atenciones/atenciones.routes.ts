import { Routes } from '@angular/router';
export const ATENCIONES_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./atenciones.component').then(m => m.AtencionesComponent) },
];
