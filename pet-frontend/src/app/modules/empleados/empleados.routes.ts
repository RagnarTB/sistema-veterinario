import { Routes } from '@angular/router';
export const EMPLEADOS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./empleados.component').then(m => m.EmpleadosComponent) },
];
