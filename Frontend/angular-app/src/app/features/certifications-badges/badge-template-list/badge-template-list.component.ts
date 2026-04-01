import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BadgeService } from '../../../core/services/badge.service';
import { BadgeTemplate } from '../../../core/models/badge.model';

@Component({
  selector: 'app-badge-template-list',
  templateUrl: './badge-template-list.component.html',
  styleUrls: ['./badge-template-list.component.scss']
})
export class BadgeTemplateListComponent implements OnInit {
  badges: BadgeTemplate[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private badgeService: BadgeService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadBadges();
  }

  loadBadges(): void {
    this.loading = true;
    this.error = null;
    this.badgeService.getAllTemplates().subscribe({
      next: (data) => {
        this.badges = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load badges';
        this.loading = false;
        console.error(err);
      }
    });
  }

  createBadge(): void {
    this.router.navigate(['/dashboard/badges/new']);
  }

  editBadge(id: number): void {
    this.router.navigate(['/dashboard/badges/edit', id]);
  }

  deleteBadge(id: number): void {
    if (confirm('Are you sure you want to delete this badge template?')) {
      this.badgeService.deleteTemplate(id).subscribe({
        next: () => {
          this.loadBadges();
        },
        error: (err) => {
          this.error = 'Failed to delete badge';
          console.error(err);
        }
      });
    }
  }

  awardBadge(): void {
    this.router.navigate(['/dashboard/badges/award']);
  }
}
