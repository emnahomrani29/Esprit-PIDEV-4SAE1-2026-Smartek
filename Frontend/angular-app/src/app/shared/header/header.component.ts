import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { DataService } from '../../core/services/data.service';
import { MenuItem } from '../../core/models/menu.model';
import { AuthService, AuthResponse } from '../../core/services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent implements OnInit, OnDestroy {
  headerData: MenuItem[] = [];
  navbarOpen = false;
  sticky = false;
  userMenuOpen = false;
  academicMenuOpen = false;
  currentUser: AuthResponse | null = null;

  constructor(
    private dataService: DataService,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.dataService.getData().subscribe(data => {
      this.headerData = data.HeaderData;
    });

    window.addEventListener('scroll', this.handleScroll.bind(this));
    
    // Vérifier si l'utilisateur est connecté
    this.currentUser = this.authService.getUserInfo();
    
    // Si connecté, récupérer les données à jour depuis la base de données
    if (this.isAuthenticated()) {
      this.authService.fetchUserData().subscribe({
        next: (userData) => {
          this.currentUser = userData;
        },
        error: (error) => {
          console.error('Error fetching user data:', error);
        }
      });
      
      this.startUserValidation();
    }
    
    // Fermer le menu utilisateur quand on clique en dehors
    document.addEventListener('click', this.handleClickOutside.bind(this));
  }

  ngOnDestroy(): void {
    document.removeEventListener('click', this.handleClickOutside.bind(this));
    this.stopUserValidation();
  }

  private validationInterval: any;

  private startUserValidation(): void {
    this.validationInterval = setInterval(() => {
      this.authService.validateUser().subscribe({
        next: (isValid) => {
          if (!isValid) {
            console.log('Utilisateur supprimé ou invalide, déconnexion automatique...');
            this.authService.logout();
            this.currentUser = null;
          }
        },
        error: () => {
          console.log('Erreur de validation, déconnexion...');
          this.authService.logout();
          this.currentUser = null;
        }
      });
    }, 30000); // Vérifier toutes les 30 secondes
  }

  private stopUserValidation(): void {
    if (this.validationInterval) {
      clearInterval(this.validationInterval);
    }
  }

  handleClickOutside(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (this.userMenuOpen && !target.closest('.user-menu-container')) {
      this.userMenuOpen = false;
    }
    if (this.academicMenuOpen && !target.closest('.academic-menu-container')) {
      this.academicMenuOpen = false;
    }
  }

  handleScroll(): void {
    this.sticky = window.scrollY >= 10;
  }

  toggleNavbar(): void {
    this.navbarOpen = !this.navbarOpen;
  }

  toggleMobileMenu(): void {
    this.navbarOpen = !this.navbarOpen;
  }

  toggleUserMenu(): void {
    this.userMenuOpen = !this.userMenuOpen;
  }

  toggleAcademicMenu(): void {
    this.academicMenuOpen = !this.academicMenuOpen;
  }
  openSignIn(): void {
    this.navbarOpen = false;
    this.router.navigate(['/auth/sign-in']);
  }

  openSignUp(): void {
    this.navbarOpen = false;
    this.router.navigate(['/auth/sign-up']);
  }

  goToDashboard(): void {
    this.userMenuOpen = false;
    if (this.isRhCompany()) {
      this.router.navigate(['/rh-company/dashboard']);
    } else {
      this.router.navigate(['/dashboard']);
    }
  }

  goToSettings(): void {
    this.userMenuOpen = false;
    this.router.navigate(['/settings']);
  }

  logout(): void {
    this.userMenuOpen = false;
    this.authService.logout();
    this.currentUser = null;
  }

  isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  isLearner(): boolean {
    return this.currentUser?.role === 'LEARNER';
  }

  isTrainer(): boolean {
    return this.currentUser?.role === 'TRAINER';
  }

  isAdmin(): boolean {
    return this.currentUser?.role === 'ADMIN';
  }

  isRhCompany(): boolean {
    return this.currentUser?.role === 'RH_COMPANY';
  }

  isRhSmartek(): boolean {
    return this.currentUser?.role === 'RH_SMARTEK';
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
}
