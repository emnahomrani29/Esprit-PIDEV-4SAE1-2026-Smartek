import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-learner-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './learner-layout.component.html',
  styleUrls: ['./learner-layout.component.css']
})
export class LearnerLayoutComponent {
  userName: string = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    const userInfo = this.authService.getUserInfo();
    this.userName = userInfo?.firstName || 'Apprenant';
  }

  logout() {
    this.authService.logout();
  }
}
