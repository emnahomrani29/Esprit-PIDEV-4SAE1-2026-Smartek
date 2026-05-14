import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-partner-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './partner-layout.component.html',
  styleUrls: ['./partner-layout.component.css']
})
export class PartnerLayoutComponent {
  userName: string = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    const userInfo = this.authService.getUserInfo();
    this.userName = userInfo?.firstName || 'Partenaire';
  }

  logout() {
    this.authService.logout();
  }
}
