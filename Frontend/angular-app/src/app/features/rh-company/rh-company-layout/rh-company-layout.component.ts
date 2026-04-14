import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-rh-company-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './rh-company-layout.component.html',
  styleUrls: ['./rh-company-layout.component.css']
})
export class RhCompanyLayoutComponent implements OnInit, OnDestroy {
  userName: string = '';
  isDashboard = true;
  profileOpen = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    const userInfo = this.authService.getUserInfo();
    this.userName = userInfo?.firstName || 'RH Entreprise';

    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd)
    ).subscribe((e: any) => {
      this.isDashboard = e.urlAfterRedirects === '/rh-company/dashboard' || e.urlAfterRedirects === '/rh-company';
      this.profileOpen = false;
    });

    this.isDashboard = this.router.url === '/rh-company/dashboard' || this.router.url === '/rh-company';
  }

  ngOnDestroy() {}

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.relative')) {
      this.profileOpen = false;
    }
  }

  goToDashboard() {
    this.router.navigate(['/rh-company/dashboard']);
  }

  logout() {
    this.profileOpen = false;
    this.authService.logout();
  }
}
