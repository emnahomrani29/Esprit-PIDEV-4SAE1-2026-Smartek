import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { BadgeService } from '../../../core/services/badge.service';
import { BadgeTemplate } from '../../../core/models/badge.model';

@Component({
  selector: 'app-award-badge',
  templateUrl: './award-badge.component.html',
  styleUrls: ['./award-badge.component.scss']
})
export class AwardBadgeComponent implements OnInit {
  awardForm: FormGroup;
  badges: BadgeTemplate[] = [];
  loading = false;
  error: string | null = null;
  success: string | null = null;

  constructor(
    private fb: FormBuilder,
    private badgeService: BadgeService,
    private router: Router
  ) {
    this.awardForm = this.fb.group({
      badgeTemplateId: ['', [Validators.required]],
      learnerId: ['', [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    this.loadBadges();
  }

  loadBadges(): void {
    this.badgeService.getAllTemplates().subscribe({
      next: (data) => {
        this.badges = data;
      },
      error: (err) => {
        this.error = 'Failed to load badges';
        console.error(err);
      }
    });
  }

  onSubmit(): void {
    if (this.awardForm.invalid) {
      return;
    }

    this.loading = true;
    this.error = null;
    this.success = null;

    this.badgeService.awardBadge(this.awardForm.value).subscribe({
      next: () => {
        this.success = 'Badge awarded successfully!';
        this.loading = false;
        this.awardForm.reset();
        setTimeout(() => {
          this.router.navigate(['/dashboard/badges']);
        }, 2000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to award badge';
        this.loading = false;
        console.error(err);
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/dashboard/badges']);
  }
}
