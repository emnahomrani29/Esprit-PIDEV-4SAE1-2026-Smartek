import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService, AuthResponse } from '../../../core/services/auth.service';
import { PermissionService } from '../../../core/services/permission.service';
import { HasPermissionDirective } from '../../../core/directives/has-permission.directive';
import { MENU_ITEMS, MenuItem } from '../../../core/config/menu.config';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule, HasPermissionDirective],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent implements OnInit {
  currentUser: AuthResponse | null = null;
  menuItems: MenuItem[] = [];
  expandedMenus: Set<string> = new Set();

  constructor(
    private authService: AuthService,
    private permissionService: PermissionService
  ) {}

  ngOnInit(): void {
    // Charger les données depuis le localStorage d'abord
    this.currentUser = this.authService.getUserInfo();
    
    // Filtrer les menus selon les permissions
    this.filterMenuItems();
    
    // Puis récupérer les données à jour depuis la base de données
    this.authService.fetchUserData().subscribe({
      next: (userData) => {
        this.currentUser = userData;
        this.filterMenuItems();
      },
      error: (error) => {
        console.error('Error fetching user data:', error);
      }
    });
  }

  filterMenuItems(): void {
    this.menuItems = MENU_ITEMS.filter(item => {
      // Toujours afficher les dividers et headers
      if (item.divider || item.header) {
        return true;
      }

      // Si pas de permissions/rôles requis, afficher
      if ((!item.permissions || item.permissions.length === 0) && 
          (!item.roles || item.roles.length === 0)) {
        return true;
      }

      // Vérifier les rôles
      if (item.roles && item.roles.length > 0) {
        if (!this.permissionService.hasAnyRole(item.roles)) {
          return false;
        }
      }

      // Vérifier les permissions
      if (item.permissions && item.permissions.length > 0) {
        return this.permissionService.hasAnyPermission(item.permissions);
      }

      return true;
    }).map(item => {
      // Filtrer les enfants si présents
      if (item.children) {
        return {
          ...item,
          children: item.children.filter(child => {
            if (!child.permissions || child.permissions.length === 0) {
              return true;
            }
            return this.permissionService.hasAnyPermission(child.permissions);
          })
        };
      }
      return item;
    });
  }

  toggleMenu(label: string): void {
    if (this.expandedMenus.has(label)) {
      this.expandedMenus.delete(label);
    } else {
      this.expandedMenus.add(label);
    }
  }

  isMenuExpanded(label: string): boolean {
    return this.expandedMenus.has(label);
  }

  getUserInitial(): string {
    return this.currentUser?.firstName?.charAt(0).toUpperCase() || 'U';
  }

  getUserImage(): string | null {
    if (this.currentUser?.imageBase64) {
      return `data:image/jpeg;base64,${this.currentUser.imageBase64}`;
    }
    return null;
  }

  formatRole(role: string | undefined): string {
    if (!role) return 'User';
    
    const roleMap: { [key: string]: string } = {
      'LEARNER': 'Learner',
      'TRAINER': 'Trainer',
      'RH_COMPANY': 'HR Company',
      'RH_SMARTEK': 'HR Smartek',
      'SPONSOR': 'Sponsor',
      'ADMIN': 'Administrator'
    };
    
    return roleMap[role] || role;
  }
}
