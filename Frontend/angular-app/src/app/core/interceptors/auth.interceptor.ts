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
      // Log the error but don't auto-logout for now
      if (error.status === 401 || error.status === 403) {
        console.error('Authorization error:', error);
        console.error('URL:', error.url);
        console.error('Status:', error.status);
        console.error('Message:', error.message);
        // Temporarily commented out to debug
        // authService.logout();
      }
      
      return throwError(() => error);
    })
  );
};
