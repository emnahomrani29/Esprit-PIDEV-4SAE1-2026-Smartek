import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BadgeService } from '../../../core/services/badge.service';
import { AuthService, AuthResponse } from '../../../core/services/auth.service';
import { EarnedBadge } from '../../../core/models/badge.model';

@Component({
  selector: 'app-my-badges',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './my-badges.component.html',
  styleUrl: './my-badges.component.scss'
})
export class MyBadgesComponent implements OnInit {
  currentUser: AuthResponse | null = null;
  badges: EarnedBadge[] = [];
  loading = false;
  error: string | null = null;
  sharingId: number | null = null;

  constructor(
    private badgeService: BadgeService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUserInfo();
    if (!this.currentUser) {
      this.error = 'Not authenticated';
      return;
    }
    this.loadBadges(this.currentUser.userId);
  }

  private loadBadges(userId: number): void {
    this.loading = true;
    this.error = null;
    this.badgeService.getBadgesByLearner(userId).subscribe({
      next: (data) => {
        this.badges = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading badges:', err);
        this.error = 'Failed to load badges';
        this.loading = false;
      }
    });
  }

  shareOnLinkedIn(badge: EarnedBadge): void {
    if (!badge.id || this.sharingId === badge.id) return;
    this.sharingId = badge.id;

    this.badgeService.shareOnLinkedIn(badge.id).subscribe({
      next: (res) => {
        window.open(res.linkedInUrl, '_blank');
        this.sharingId = null;
      },
      error: () => {
        this.error = 'Failed to generate LinkedIn share link';
        this.sharingId = null;
      }
    });
  }
}
