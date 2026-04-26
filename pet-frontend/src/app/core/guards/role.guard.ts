import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { RolNombre } from '../models/models';

/**
 * Guard de rol. Uso:
 *   canActivate: [roleGuard(['ROLE_ADMIN', 'ROLE_VETERINARIO'])]
 */
export function roleGuard(roles: RolNombre[]): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      router.navigate(['/login']);
      return false;
    }

    if (authService.hasAnyRole(...roles)) {
      return true;
    }

    // Autenticado pero sin el rol requerido → página 403
    router.navigate(['/app/forbidden']);
    return false;
  };
}
