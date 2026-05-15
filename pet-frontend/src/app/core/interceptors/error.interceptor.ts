import {
  HttpInterceptorFn,
  HttpRequest,
  HttpHandlerFn,
  HttpEvent,
  HttpErrorResponse,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';

export const errorInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  const snackBar = inject(MatSnackBar);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Ignorar errores de 401 porque ya los maneja el jwtInterceptor
      if (error.status !== 401) {
        let errorMsg = 'Ha ocurrido un error inesperado. Intente nuevamente.';
        
        // Si el backend envía un mensaje personalizado en la respuesta
        if (error.error && error.error.mensaje) {
          errorMsg = error.error.mensaje;
        } else if (error.error && error.error.message) {
          errorMsg = error.error.message;
        } else if (error.status === 403) {
          errorMsg = 'No tiene permisos para realizar esta acción.';
        } else if (error.status === 404) {
          errorMsg = 'Recurso no encontrado.';
        }

        // Mostrar el Toast Global
        snackBar.open(errorMsg, 'Cerrar', {
          duration: 4000,
          horizontalPosition: 'right',
          verticalPosition: 'bottom',
          panelClass: ['error-snackbar']
        });
      }

      return throwError(() => error);
    })
  );
};
