import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();

  // Exclure uniquement les endpoints publics (login/register)
  const isPublic = req.url.includes('/api/auth/login') || req.url.includes('/api/auth/register');

  if (token && !isPublic) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        console.log('Token invalide ou expiré, déconnexion...');
        authService.logout();
        router.navigate(['/login']);
      }

      if (error.status === 404 && error.url?.includes('/api/auth/user/')) {
        console.log('Utilisateur introuvable, déconnexion...');
        authService.logout();
      }

      return throwError(() => error);
    })
  );
};
