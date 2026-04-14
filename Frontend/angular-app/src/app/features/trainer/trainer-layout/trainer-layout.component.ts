import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-trainer-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './trainer-layout.component.html',
  styleUrls: ['./trainer-layout.component.css']
})
export class TrainerLayoutComponent {
  userName: string = '';
  isAcademicMenuOpen: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    const userInfo = this.authService.getUserInfo();
    this.userName = userInfo?.firstName || 'Formateur';
  }

  toggleAcademicMenu() {
    this.isAcademicMenuOpen = !this.isAcademicMenuOpen;
  }

  closeAcademicMenu() {
    this.isAcademicMenuOpen = false;
  }

  logout() {
    this.authService.logout();
  }
}
