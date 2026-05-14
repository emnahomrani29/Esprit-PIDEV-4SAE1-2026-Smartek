import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class PermissionService {

  constructor(private authService: AuthService) {}

  /**
   * Vérifie si l'utilisateur a l'un des rôles spécifiés
   */
  hasRole(roles: string | string[]): boolean {
    const userInfo = this.authService.getUserInfo();
    if (!userInfo) return false;

    const allowedRoles = Array.isArray(roles) ? roles : [roles];
    return allowedRoles.includes(userInfo.role);
  }

  /**
   * Alias pour hasRole (pour compatibilité)
   */
  hasAnyRole(roles: string | string[]): boolean {
    return this.hasRole(roles);
  }

  /**
   * Vérifie si l'utilisateur a toutes les permissions spécifiées
   */
  hasAllPermissions(permissions: string[]): boolean {
    // Pour l'instant, on utilise la même logique que hasRole
    // À adapter selon votre système de permissions
    return this.hasRole(permissions);
  }

  /**
   * Vérifie si l'utilisateur a au moins une des permissions spécifiées
   */
  hasAnyPermission(permissions: string[]): boolean {
    // Pour l'instant, on utilise la même logique que hasRole
    // À adapter selon votre système de permissions
    return this.hasRole(permissions);
  }

  /**
   * Vérifie si l'utilisateur peut créer du contenu (TRAINER ou ADMIN)
   */
  canCreate(): boolean {
    return this.hasRole(['TRAINER', 'ADMIN']);
  }

  /**
   * Vérifie si l'utilisateur peut modifier du contenu (TRAINER ou ADMIN)
   */
  canEdit(): boolean {
    return this.hasRole(['TRAINER', 'ADMIN']);
  }

  /**
   * Vérifie si l'utilisateur peut supprimer du contenu (TRAINER ou ADMIN)
   */
  canDelete(): boolean {
    return this.hasRole(['TRAINER', 'ADMIN']);
  }

  /**
   * Vérifie si l'utilisateur est un apprenant
   */
  isLearner(): boolean {
    return this.hasRole('LEARNER');
  }

  /**
   * Vérifie si l'utilisateur est un formateur
   */
  isTrainer(): boolean {
    return this.hasRole('TRAINER');
  }

  /**
   * Vérifie si l'utilisateur est un administrateur
   */
  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  /**
   * Vérifie si l'utilisateur est un sponsor
   */
  isSponsor(): boolean {
    return this.hasRole('SPONSOR');
  }

  /**
   * Vérifie si l'utilisateur est RH (COMPANY ou SMARTEK)
   */
  isRH(): boolean {
    return this.hasRole(['RH_COMPANY', 'RH_SMARTEK']);
  }

  /**
   * Vérifie si l'utilisateur peut approuver des preuves de compétences
   */
  canApproveSkills(): boolean {
    return this.hasRole(['ADMIN', 'RH_SMARTEK']);
  }

  /**
   * Vérifie si l'utilisateur peut gérer les sponsors
   */
  canManageSponsors(): boolean {
    return this.hasRole('ADMIN');
  }

  /**
   * Vérifie si l'utilisateur peut voir les analytics
   */
  canViewAnalytics(): boolean {
    return this.hasRole(['TRAINER', 'ADMIN', 'RH_COMPANY', 'RH_SMARTEK']);
  }

  /**
   * Vérifie si l'utilisateur peut créer des offres d'emploi
   */
  canCreateJobOffers(): boolean {
    return this.hasRole(['PARTNER', 'RH_COMPANY', 'ADMIN']);
  }

  /**
   * Vérifie si l'utilisateur peut postuler à des offres
   */
  canApplyToJobs(): boolean {
    return this.hasRole('LEARNER');
  }
}
