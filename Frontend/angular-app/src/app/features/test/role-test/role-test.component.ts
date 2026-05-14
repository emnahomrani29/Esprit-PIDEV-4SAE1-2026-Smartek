import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-role-test',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './role-test.component.html',
  styleUrls: ['./role-test.component.css']
})
export class RoleTestComponent {
  currentRole: string | null = null;
  isAuthenticated: boolean = false;

  roles = [
    { value: 'ADMIN', label: 'Administrateur', path: '/admin/users' },
    { value: 'LEARNER', label: 'Apprenant', path: '/learner/courses' },
    { value: 'TRAINER', label: 'Formateur', path: '/trainer/courses' },
    { value: 'RH_SMARTEK', label: 'RH SMARTEK', path: '/rh-smartek/certifications' },
    { value: 'RH_COMPANY', label: 'RH Entreprise', path: '/rh-company/offers' },
    { value: 'PARTNER', label: 'Partenaire', path: '/partner/sponsorship' }
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.updateStatus();
  }

  updateStatus() {
    this.currentRole = this.authService.getUserRole();
    this.isAuthenticated = this.authService.isAuthenticated();
  }

  simulateRole(role: string) {
    // Simuler une connexion avec ce rôle
    const mockAuthResponse = {
      token: 'mock-token-' + Date.now(),
      userId: 1,
      email: `test-${role.toLowerCase()}@smartek.com`,
      firstName: `Test ${role}`,
      role: role,
      message: 'Mock login successful'
    };

    // Sauvegarder dans localStorage
    localStorage.setItem('token', mockAuthResponse.token);
    localStorage.setItem('userInfo', JSON.stringify(mockAuthResponse));

    this.updateStatus();
    
    // Rediriger vers le dashboard du rôle
    const dashboardPath = this.authService.getRoleDashboardPath();
    this.router.navigate([dashboardPath]);
  }

  logout() {
    this.authService.logout();
    this.updateStatus();
  }

  navigateToRole(path: string) {
    this.router.navigate([path]);
  }
}
