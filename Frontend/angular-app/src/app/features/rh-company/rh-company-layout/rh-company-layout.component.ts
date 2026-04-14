import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-rh-company-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './rh-company-layout.component.html',
  styleUrls: ['./rh-company-layout.component.css']
})
export class RhCompanyLayoutComponent {
  userName: string = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    const userInfo = this.authService.getUserInfo();
    this.userName = userInfo?.firstName || 'RH Entreprise';
  }

  logout() {
    this.authService.logout();
  }
}
