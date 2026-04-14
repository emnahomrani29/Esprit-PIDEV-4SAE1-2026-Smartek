import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-rh-smartek-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './rh-smartek-layout.component.html',
  styleUrls: ['./rh-smartek-layout.component.css']
})
export class RhSmartekLayoutComponent {
  userName: string = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    const userInfo = this.authService.getUserInfo();
    this.userName = userInfo?.firstName || 'RH SMARTEK';
  }

  logout() {
    this.authService.logout();
  }
}
