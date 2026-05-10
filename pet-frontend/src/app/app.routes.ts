import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  // ─── Ruta raíz ────────────────────────────────────────────────────────
  {
    path: '',
    redirectTo: 'app/citas',
    pathMatch: 'full',
  },
  {
    path: 'auth/confirmar',
    redirectTo: 'confirmar',
    pathMatch: 'full',
  },

  // ─── Auth Layout (sin sidebar) ────────────────────────────────────────
  {
    path: '',
    loadComponent: () =>
      import('./layouts/auth-layout/auth-layout.component').then(
        (m) => m.AuthLayoutComponent
      ),
    children: [
      {
        path: 'login',
        loadComponent: () =>
          import('./modules/auth/login/login.component').then(
            (m) => m.LoginComponent
          ),
      },
      {
        path: 'confirmar',
        loadComponent: () =>
          import('./modules/auth/confirmar-cuenta/confirmar-cuenta.component').then(
            (m) => m.ConfirmarCuentaComponent
          ),
      },
      {
        path: 'completar-registro',
        loadComponent: () =>
          import('./modules/auth/completar-registro/completar-registro.component').then(
            (m) => m.CompletarRegistroComponent
          ),
      },
    ],
  },

  // ─── Main Layout (con sidebar + header) ───────────────────────────────
  {
    path: 'app',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layouts/main-layout/main-layout.component').then(
        (m) => m.MainLayoutComponent
      ),
    children: [
      // Dashboard — solo ADMIN
      {
        path: 'dashboard',
        canActivate: [roleGuard(['ROLE_ADMIN'])],
        loadChildren: () =>
          import('./modules/dashboard/dashboard.routes').then(
            (m) => m.DASHBOARD_ROUTES
          ),
      },

      // Reportes
      {
        path: 'reportes',
        canActivate: [roleGuard(['ROLE_ADMIN'])],
        loadComponent: () =>
          import('./modules/reportes/reportes.component').then(
            (m) => m.ReportesComponent
          ),
      },

      // Citas — todos los empleados
      {
        path: 'citas',
        loadChildren: () =>
          import('./modules/citas/citas.routes').then((m) => m.CITAS_ROUTES),
      },

      // Clientes — ADMIN y RECEPCIONISTA
      {
        path: 'clientes',
        canActivate: [roleGuard(['ROLE_ADMIN', 'ROLE_RECEPCIONISTA'])],
        loadChildren: () =>
          import('./modules/clientes/clientes.routes').then(
            (m) => m.CLIENTES_ROUTES
          ),
      },

      // Pacientes — todos los autenticados
      {
        path: 'pacientes',
        loadChildren: () =>
          import('./modules/pacientes/pacientes.routes').then(
            (m) => m.PACIENTES_ROUTES
          ),
      },

      // Atenciones medicas — VETERINARIO y ADMIN
      {
        path: 'atenciones',
        canActivate: [roleGuard(['ROLE_ADMIN', 'ROLE_VETERINARIO'])],
        loadChildren: () =>
          import('./modules/atenciones/atenciones.routes').then(
            (m) => m.ATENCIONES_ROUTES
          ),
      },

      // Hospitalización — VETERINARIO y ADMIN
      {
        path: 'hospitalizacion',
        canActivate: [roleGuard(['ROLE_ADMIN', 'ROLE_VETERINARIO'])],
        loadChildren: () =>
          import('./modules/hospitalizacion/hospitalizacion.routes').then(
            (m) => m.HOSPITALIZACION_ROUTES
          ),
      },

      // Jaulas
      {
        path: 'jaulas',
        canActivate: [roleGuard(['ROLE_ADMIN', 'ROLE_VETERINARIO'])],
        loadComponent: () =>
          import('./modules/hospitalizacion/jaulas.component').then(
            (m) => m.JaulasComponent
          ),
      },

      // Farmacia (productos + ventas) — todos
      {
        path: 'farmacia',
        loadChildren: () =>
          import('./modules/farmacia/farmacia.routes').then(
            (m) => m.FARMACIA_ROUTES
          ),
      },

      // Inventario
      {
        path: 'inventario',
        canActivate: [roleGuard(['ROLE_ADMIN', 'ROLE_RECEPCIONISTA'])],
        loadComponent: () =>
          import('./modules/farmacia/inventario.component').then(
            (m) => m.InventarioComponent
          ),
      },

      // Caja — ADMIN y RECEPCIONISTA
      {
        path: 'caja',
        canActivate: [roleGuard(['ROLE_ADMIN', 'ROLE_RECEPCIONISTA'])],
        loadChildren: () =>
          import('./modules/caja/caja.routes').then((m) => m.CAJA_ROUTES),
      },

      // Empleados — solo ADMIN
      {
        path: 'empleados',
        canActivate: [roleGuard(['ROLE_ADMIN'])],
        loadChildren: () =>
          import('./modules/empleados/empleados.routes').then(
            (m) => m.EMPLEADOS_ROUTES
          ),
      },

      // Sedes — solo ADMIN
      {
        path: 'sedes',
        canActivate: [roleGuard(['ROLE_ADMIN'])],
        loadChildren: () =>
          import('./modules/sedes/sedes.routes').then((m) => m.SEDES_ROUTES),
      },

      // Servicios Médicos — solo ADMIN
      {
        path: 'servicios-medicos',
        canActivate: [roleGuard(['ROLE_ADMIN'])],
        loadComponent: () =>
          import('./modules/servicios-medicos/servicios-medicos.component').then(
            (m) => m.ServiciosMedicosComponent
          ),
      },

      // Redirección por defecto dentro de /app
      { path: '', redirectTo: 'citas', pathMatch: 'full' },

      // 403 Forbidden
      {
        path: 'forbidden',
        loadComponent: () =>
          import('./shared/components/forbidden/forbidden.component').then(
            (m) => m.ForbiddenComponent
          ),
      },
    ],
  },

  // Wildcard
  { path: '**', redirectTo: 'app/citas' },
];
