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
  currentUser: AuthResponse | null = null;
  openDropdown: string | null = null; // Pour gérer les dropdowns

  constructor(
    private dataService: DataService,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Vérifier si l'utilisateur est connecté
    this.currentUser = this.authService.getUserInfo();
    
    // Charger les données du header en fonction de l'état de connexion
    if (this.isAuthenticated() && this.currentUser) {
      // Si connecté, charger le menu personnalisé immédiatement
      this.updateHeaderForAuthenticatedUser();
      
      // Puis récupérer les données à jour depuis la base de données
      this.authService.fetchUserData().subscribe({
        next: (userData) => {
          this.currentUser = userData;
          this.updateHeaderForAuthenticatedUser();
        },
        error: () => {
          // Error fetching user data
        }
      });
      
      this.startUserValidation();
    } else {
      // Si non connecté, charger le menu public
      this.loadHeaderData();
    }

    window.addEventListener('scroll', this.handleScroll.bind(this));
    
    // Fermer le menu utilisateur quand on clique en dehors
    document.addEventListener('click', this.handleClickOutside.bind(this));
  }

  private loadHeaderData(): void {
    this.dataService.getData().subscribe(data => {
      this.headerData = data.HeaderData;
    });
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
            this.authService.logout();
            this.currentUser = null;
          }
        },
        error: () => {
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
    // Fermer les dropdowns si on clique en dehors
    if (this.openDropdown && !target.closest('.group')) {
      this.openDropdown = null;
    }
  }

  handleScroll(): void {
    this.sticky = window.scrollY >= 10;
  }

  toggleNavbar(): void {
    this.navbarOpen = !this.navbarOpen;
  }

  toggleUserMenu(): void {
    this.userMenuOpen = !this.userMenuOpen;
  }

  openSignIn(): void {
    this.navbarOpen = false;
    this.router.navigate(['/auth/sign-in']);
  }

  openSignUp(): void {
    this.navbarOpen = false;
    this.router.navigate(['/auth/sign-up']);
  }

  /**
   * Retourne true si le rôle courant a un dashboard dédié
   */
  hasDashboard(): boolean {
    const role = this.currentUser?.role;
    return ['ADMIN', 'RH_COMPANY', 'RH_SMARTEK', 'SPONSOR', 'PARTNER'].includes(role ?? '');
  }

  goToDashboard(): void {
    this.userMenuOpen = false;
    const role = this.currentUser?.role;
    switch (role) {
      case 'SPONSOR':
        this.router.navigate(['/sponsor']);
        break;
      case 'ADMIN':
      case 'RH_COMPANY':
      case 'RH_SMARTEK':
        this.router.navigate(['/dashboard']);
        break;
      case 'PARTNER':
        this.router.navigate(['/partner']);
        break;
      default:
        this.router.navigate(['/']);
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
    this.updateHeaderForAuthenticatedUser();
  }

  isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
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

  private updateHeaderForAuthenticatedUser(): void {
    if (this.isAuthenticated() && this.currentUser) {
      const role = this.currentUser.role;
      
      // Items de menu basés sur le rôle
      let authenticatedItems: MenuItem[] = [];
      
      if (role === 'LEARNER') {
        authenticatedItems = [
          { label: 'Home', href: '/' },
          { 
            label: 'My Learning', 
            children: [
              { label: 'My Trainings', href: '/learner/training' },
              { label: 'My Courses', href: '/learner/courses' },
              { label: 'My Exams', href: '/learner/exams' }
            ]
          },
          { label: 'Planning', href: '/learner/planning' },
          { label: 'Events', href: '/learner/events' },
          { label: 'Job Offers', href: '/learner/job-offers' },
          { label: 'My Skills', href: '/learner/skills' },
          { label: 'Certifications', href: '/learner/certifications' },
          { label: 'My Learning Path', href: '/learner/learning-path' }
        ];
      } else if (role === 'TRAINER') {
        authenticatedItems = [
          { label: 'Home', href: '/' },
          { 
            label: 'Content Management', 
            children: [
              { label: 'Trainings', href: '/trainer/training-management' },
              { label: 'Courses', href: '/trainer/courses' },
              { label: 'Exams', href: '/trainer/exams' }
            ]
          },
          { label: 'Planning', href: '/trainer/planning' },
          { label: 'Events', href: '/trainer/events' },
          { label: 'Learner Analytics', href: '/trainer/learner-analytics' },
          { 
            label: 'Certifications & Badges', 
            children: [
              { label: 'Badge Templates', href: '/trainer/badge-templates' },
              { label: 'Certification Templates', href: '/trainer/certification-templates' },
              { label: 'Award Badges', href: '/trainer/award-badges' }
            ]
          },
          { label: 'Learning Paths', href: '/trainer/learning-paths' }
        ];
      } else if (role === 'ADMIN') {
        authenticatedItems = [
          { label: 'Home', href: '/' },
          { label: 'Dashboard', href: '/dashboard' }
        ];
      } else if (role === 'SPONSOR') {
        authenticatedItems = [
          { label: 'Home', href: '/' },
          { label: 'Dashboard', href: '/sponsor' }
        ];
      } else if (role === 'RH_COMPANY' || role === 'RH_SMARTEK') {
        authenticatedItems = [
          { label: 'Home', href: '/' },
          { label: 'Dashboard', href: '/dashboard' }
        ];
      } else if (role === 'PARTNER') {
        authenticatedItems = [
          { label: 'Home', href: '/' },
          { label: 'Dashboard', href: '/partner' }
        ];
      }

      // Remplacer les items de base par les items personnalisés
      if (authenticatedItems.length > 0) {
        this.headerData = authenticatedItems;
      }
    } else {
      // Recharger les items de base si déconnecté
      this.dataService.getData().subscribe(data => {
        this.headerData = data.HeaderData;
      });
    }
  }

  toggleDropdown(label: string): void {
    this.openDropdown = this.openDropdown === label ? null : label;
  }

  isDropdownOpen(label: string): boolean {
    return this.openDropdown === label;
  }

  navigateTo(href: string): void {
    this.router.navigate([href]);
    this.navbarOpen = false;
    this.openDropdown = null;
  }

  getPermissionClass(label: string): string {
    // Tous les liens sont cliquables et ont le même style
    return 'text-gray-700 hover:bg-gray-50 hover:text-primary cursor-pointer';
  }
}
