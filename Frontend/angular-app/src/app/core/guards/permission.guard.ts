import { inject } from '@angular/core';
import { Router, CanActivateFn, ActivatedRouteSnapshot } from '@angular/router';
import { PermissionService } from '../services/permission.service';
import { Permission } from '../enums/permission.enum';
import { Role } from '../enums/role.enum';

export const permissionGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const permissionService = inject(PermissionService);
  const router = inject(Router);

  // Vérifier les permissions requises
  const requiredPermissions = route.data['permissions'] as Permission[];
  const requiredRoles = route.data['roles'] as Role[];
  const requireAll = route.data['requireAll'] as boolean; // true = toutes les permissions, false = au moins une

  // Si des rôles sont spécifiés, vérifier d'abord les rôles
  if (requiredRoles && requiredRoles.length > 0) {
    if (!permissionService.hasAnyRole(requiredRoles)) {
      console.warn('Access denied: User does not have required role');
      router.navigate(['/dashboard']);
      return false;
    }
  }

  // Si des permissions sont spécifiées, les vérifier
  if (requiredPermissions && requiredPermissions.length > 0) {
    const hasAccess = requireAll
      ? permissionService.hasAllPermissions(requiredPermissions)
      : permissionService.hasAnyPermission(requiredPermissions);

    if (!hasAccess) {
      console.warn('Access denied: User does not have required permissions');
      router.navigate(['/dashboard']);
      return false;
    }
  }

  return true;
};
