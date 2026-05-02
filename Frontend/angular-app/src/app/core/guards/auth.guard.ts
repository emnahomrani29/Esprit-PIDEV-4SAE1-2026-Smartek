import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map, catchError, of } from 'rxjs';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    router.navigate(['/auth/sign-in']);
    return false;
  }

  // Vérifier si l'utilisateur existe toujours dans la base de données
  return authService.validateUser().pipe(
    map(isValid => {
      if (!isValid) {
        authService.logout();
        return false;
      }
      return true;
    }),
    catchError(() => {
      authService.logout();
      return of(false);
    })
  );
};
