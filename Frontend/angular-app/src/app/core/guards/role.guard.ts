import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    console.warn('[RoleGuard] ❌ Not authenticated — redirecting to sign-in');
    router.navigate(['/auth/sign-in']);
    return false;
  }

  const userInfo = authService.getUserInfo();
  const allowedRoles = route.data['roles'] as string[];

  console.group('[RoleGuard] 🔐 Route access check');
  console.log('👤 User     :', userInfo?.firstName, `<${userInfo?.email}>`);
  console.log('🎭 Role     :', userInfo?.role);
  console.log('🛣️  Route    :', state.url);
  console.log('✅ Allowed  :', allowedRoles);

  if (allowedRoles && allowedRoles.length > 0) {
    if (!userInfo || !allowedRoles.includes(userInfo.role)) {
      console.warn('[RoleGuard] 🚫 Access DENIED — role not in allowed list');
      console.groupEnd();

      switch (userInfo?.role) {
        case 'ADMIN':
          router.navigate(['/dashboard']);
          break;
        case 'SPONSOR':
          router.navigate(['/sponsor']);
          break;
        case 'RH_COMPANY':
          router.navigate(['/dashboard']);
          break;
        case 'RH_SMARTEK':
          router.navigate(['/rh-smartek']);
          break;
        case 'PARTNER':
          router.navigate(['/partner']);
          break;
        case 'LEARNER':
          router.navigate(['/learner']);
          break;
        case 'TRAINER':
          router.navigate(['/trainer']);
          break;
        default:
          router.navigate(['/auth/sign-in']);
      }
      return false;
    }
  }

  console.log('[RoleGuard] ✅ Access GRANTED');
  console.groupEnd();
  return true;
};
