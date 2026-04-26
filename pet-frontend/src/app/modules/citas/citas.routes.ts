import { Routes } from '@angular/router';
export const CITAS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./citas.component').then(m => m.CitasComponent) },
];
