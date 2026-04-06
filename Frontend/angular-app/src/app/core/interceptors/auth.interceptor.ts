import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();

  // Ajouter le token JWT uniquement pour les routes auth-service
  // Les autres microservices (skill-evidence, learning, etc.) n'ont pas de Spring Security
  const requiresAuth = req.url.includes('/api/auth/');

  if (token && requiresAuth) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Déconnecter uniquement si c'est une erreur auth sur les routes auth-service
      if ((error.status === 401 || error.status === 403) && req.url.includes('/api/auth/')) {
        console.log('Token invalide, déconnexion...');
        authService.logout();
      }

      // Si l'utilisateur a été supprimé (404 sur les endpoints utilisateur)
      if (error.status === 404 && error.url?.includes('/api/auth/user/')) {
        console.log('Utilisateur introuvable, déconnexion...');
        authService.logout();
      }

      return throwError(() => error);
    })
  );
};
