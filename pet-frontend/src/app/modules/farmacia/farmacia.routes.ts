import { Routes } from '@angular/router';
export const FARMACIA_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./farmacia.component').then(m => m.FarmaciaComponent) },
];
