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
      if (error.status === 401) {
        console.error('Token invalide ou expiré, déconnexion...');
        authService.logout();
      }
      if (error.status === 403) {
        console.error('Accès refusé (403):', error.url);
        // Ne pas déconnecter sur 403 - juste loguer
      }
      if (error.status === 404 && error.url?.includes('/api/users/')) {
        console.log('Utilisateur introuvable, déconnexion...');
        authService.logout();
      }
      return throwError(() => error);
    })
  );
};
