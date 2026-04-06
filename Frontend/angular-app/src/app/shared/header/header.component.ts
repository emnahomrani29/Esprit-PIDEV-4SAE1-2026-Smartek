import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { DataService } from '../../core/services/data.service';
import { MenuItem } from '../../core/models/menu.model';
import { AuthService, AuthResponse } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { Notification } from '../../core/models/notification.model';

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
  learningPathMenuOpen = false;
  notificationMenuOpen = false;
  notifications: Notification[] = [];
  unreadCount = 0;
  currentUser: AuthResponse | null = null;

  constructor(
    private dataService: DataService,
    private router: Router,
    private authService: AuthService,
    private notificationService: NotificationService
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
          // Charger les notifications après avoir le rôle confirmé
          if (this.currentUser?.role === 'LEARNER') {
            this.loadNotifications();
            this.startNotificationPolling();
          }
        },
        error: (error) => {
          console.error('Error fetching user data:', error);
          // Fallback : utiliser les données du localStorage
          if (this.currentUser?.role === 'LEARNER') {
            this.loadNotifications();
            this.startNotificationPolling();
          }
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
    this.stopNotificationPolling();
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

  private notificationInterval: any;

  loadNotifications(): void {
    const user = this.authService.getUserInfo();
    if (!user?.userId) return;
    this.notificationService.getUnread(user.userId).subscribe({
      next: (data) => {
        this.notifications = data;
        this.unreadCount = data.filter(n => !n.isRead).length;
      },
      error: () => {}
    });
  }

  private startNotificationPolling(): void {
    this.notificationInterval = setInterval(() => {
      this.loadNotifications();
    }, 30000); // toutes les 30 secondes
  }

  private stopNotificationPolling(): void {
    if (this.notificationInterval) {
      clearInterval(this.notificationInterval);
    }
  }

  toggleNotificationMenu(): void {
    this.notificationMenuOpen = !this.notificationMenuOpen;
    if (this.notificationMenuOpen) {
      // Charger toutes les notifications (lues + non lues) à l'ouverture
      const user = this.authService.getUserInfo();
      if (user?.userId) {
        this.notificationService.getAll(user.userId).subscribe({
          next: (data) => { this.notifications = data; },
          error: () => {}
        });
      }
    }
  }

  markAsRead(notificationId: number, event: Event): void {
    event.stopPropagation();
    this.notificationService.markAsRead(notificationId).subscribe({
      next: () => {
        const n = this.notifications.find(n => n.notificationId === notificationId);
        if (n) { n.isRead = true; }
        this.unreadCount = this.notifications.filter(n => !n.isRead).length;
      },
      error: () => {}
    });
  }

  markAllAsRead(): void {
    const user = this.authService.getUserInfo();
    if (!user?.userId) return;
    this.notificationService.markAllAsRead(user.userId).subscribe({
      next: () => {
        this.notifications.forEach(n => n.isRead = true);
        this.unreadCount = 0;
      },
      error: () => {}
    });
  }

  deleteNotification(notificationId: number, event: Event): void {
    event.stopPropagation();
    this.notificationService.delete(notificationId).subscribe({
      next: () => {
        this.notifications = this.notifications.filter(n => n.notificationId !== notificationId);
        this.unreadCount = this.notifications.filter(n => !n.isRead).length;
      },
      error: () => {}
    });
  }

  getNotificationIcon(type: string): string {
    return type === 'APPROVAL' ? '✅' : '❌';
  }

  handleClickOutside(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (this.userMenuOpen && !target.closest('.user-menu-container')) {
      this.userMenuOpen = false;
    }
    if (this.academicMenuOpen && !target.closest('.academic-menu-container')) {
      this.academicMenuOpen = false;
    }
    if (this.learningPathMenuOpen && !target.closest('.academic-menu-container')) {
      this.learningPathMenuOpen = false;
    }
    if (this.notificationMenuOpen && !target.closest('.notification-menu-container')) {
      this.notificationMenuOpen = false;
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

  toggleLearningPathMenu(): void {
    this.learningPathMenuOpen = !this.learningPathMenuOpen;
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
