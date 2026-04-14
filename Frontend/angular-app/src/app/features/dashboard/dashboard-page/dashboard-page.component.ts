import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService, AuthResponse } from '../../../core/services/auth.service';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.scss'
})
export class DashboardPageComponent implements OnInit {
  currentUser: AuthResponse | null = null;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    // Charger les données depuis le localStorage d'abord
    this.currentUser = this.authService.getUserInfo();
    console.log('Current user data from localStorage:', this.currentUser);
    
    // Puis récupérer les données à jour depuis la base de données
    this.authService.fetchUserData().subscribe({
      next: (userData) => {
        this.currentUser = userData;
        console.log('Current user data from database:', this.currentUser);
      },
      error: (error) => {
        console.error('Error fetching user data:', error);
      }
    });
  }

  getUserImage(): string | null {
    if (this.currentUser?.imageBase64) {
      return `data:image/jpeg;base64,${this.currentUser.imageBase64}`;
    }
    return null;
  }

  getUserInitial(): string {
    return this.currentUser?.firstName?.charAt(0).toUpperCase() || 'U';
  }

  getExperience(): number {
    return this.currentUser?.experience ?? 0;
  }

  formatRole(role: string | undefined): string {
    if (!role) return 'User';
    
    const roleMap: { [key: string]: string } = {
      'LEARNER': 'Learner',
      'TRAINER': 'Trainer',
      'RH_COMPANY': 'HR Company',
      'RH_SMARTEK': 'HR Smartek',
      'PARTNER': 'Partner',
      'ADMIN': 'Admin'
    };
    
    return roleMap[role] || role;
  }
}
