import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();

  // Ajouter le token JWT à toutes les requêtes
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Si l'utilisateur n'existe plus ou le token est invalide (401 ou 403)
      if (error.status === 401 || error.status === 403) {
        authService.logout();
      }
      
      // Si l'utilisateur a été supprimé (404 sur les endpoints utilisateur)
      if (error.status === 404 && error.url?.includes('/api/users/')) {
        authService.logout();
      }

      return throwError(() => error);
    })
  );
};
