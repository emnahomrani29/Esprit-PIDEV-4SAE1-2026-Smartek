import { Injectable } from '@angular/core';
import { Role } from '../enums/role.enum';
import { Permission } from '../enums/permission.enum';
import { ROLE_PERMISSIONS } from '../config/role-permission.config';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class PermissionService {
  constructor(private authService: AuthService) {}

  /**
   * Vérifie si l'utilisateur actuel a une permission spécifique
   */
  hasPermission(permission: Permission): boolean {
    const userInfo = this.authService.getUserInfo();
    if (!userInfo || !userInfo.role) {
      return false;
    }

    const userRole = userInfo.role as Role;
    const permissions = ROLE_PERMISSIONS[userRole];
    
    return permissions ? permissions.includes(permission) : false;
  }

  /**
   * Vérifie si l'utilisateur a au moins une des permissions fournies
   */
  hasAnyPermission(permissions: Permission[]): boolean {
    return permissions.some(permission => this.hasPermission(permission));
  }

  /**
   * Vérifie si l'utilisateur a toutes les permissions fournies
   */
  hasAllPermissions(permissions: Permission[]): boolean {
    return permissions.every(permission => this.hasPermission(permission));
  }

  /**
   * Récupère toutes les permissions de l'utilisateur actuel
   */
  getUserPermissions(): Permission[] {
    const userInfo = this.authService.getUserInfo();
    if (!userInfo || !userInfo.role) {
      return [];
    }

    const userRole = userInfo.role as Role;
    return ROLE_PERMISSIONS[userRole] || [];
  }

  /**
   * Vérifie si l'utilisateur a un rôle spécifique
   */
  hasRole(role: Role): boolean {
    const userInfo = this.authService.getUserInfo();
    return userInfo?.role === role;
  }

  /**
   * Vérifie si l'utilisateur a au moins un des rôles fournis
   */
  hasAnyRole(roles: Role[]): boolean {
    const userInfo = this.authService.getUserInfo();
    if (!userInfo || !userInfo.role) {
      return false;
    }
    return roles.includes(userInfo.role as Role);
  }

  /**
   * Vérifie si l'utilisateur est un administrateur
   */
  isAdmin(): boolean {
    return this.hasRole(Role.ADMIN);
  }

  /**
   * Vérifie si l'utilisateur est RH Smartek
   */
  isRHSmartek(): boolean {
    return this.hasRole(Role.RH_SMARTEK);
  }

  /**
   * Vérifie si l'utilisateur a des droits d'administration (Admin ou RH Smartek)
   */
  hasAdminRights(): boolean {
    return this.hasAnyRole([Role.ADMIN, Role.RH_SMARTEK]);
  }
}
