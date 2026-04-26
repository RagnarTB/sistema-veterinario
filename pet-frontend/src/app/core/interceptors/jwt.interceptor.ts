import {
  HttpInterceptorFn,
  HttpRequest,
  HttpHandlerFn,
  HttpEvent,
  HttpErrorResponse,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const jwtInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  // Clonar la petición añadiendo el token si existe
  const authReq = token ? addToken(req, token) : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Si recibimos 401 e intentamos renovar el token automáticamente
      if (error.status === 401 && !req.url.includes('/auth/')) {
        return handle401(req, next, authService);
      }
      return throwError(() => error);
    })
  );
};

function addToken(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
  return req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });
}

function handle401(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
  authService: AuthService
): Observable<HttpEvent<unknown>> {
  return authService.refreshToken().pipe(
    switchMap((res) => {
      // Reintentar la petición original con el nuevo token
      const retryReq = addToken(req, res.token);
      return next(retryReq);
    }),
    catchError((err) => {
      // Si el refresh falla, hacer logout
      authService.logout();
      return throwError(() => err);
    })
  );
}
