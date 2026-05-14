import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { BadgeService } from '../../../core/services/badge.service';
import { BadgeTemplate } from '../../../core/models/badge.model';

@Component({
  selector: 'app-badge-template-form',
  templateUrl: './badge-template-form.component.html',
  styleUrls: ['./badge-template-form.component.scss']
})
export class BadgeTemplateFormComponent implements OnInit {
  badgeForm: FormGroup;
  isEditMode = false;
  badgeId: number | null = null;
  loading = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private badgeService: BadgeService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.badgeForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.badgeId = +params['id'];
        this.loadBadge(this.badgeId);
      }
    });
  }

  loadBadge(id: number): void {
    this.loading = true;
    this.badgeService.getTemplateById(id).subscribe({
      next: (badge) => {
        this.badgeForm.patchValue(badge);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load badge';
        this.loading = false;
        console.error(err);
      }
    });
  }

  onSubmit(): void {
    if (this.badgeForm.invalid) {
      return;
    }

    this.loading = true;
    this.error = null;
    const badge: BadgeTemplate = this.badgeForm.value;

    const request = this.isEditMode
      ? this.badgeService.updateTemplate(this.badgeId!, badge)
      : this.badgeService.createTemplate(badge);

    request.subscribe({
      next: () => {
        this.router.navigate(['/dashboard/badges']);
      },
      error: (err) => {
        this.error = this.isEditMode ? 'Failed to update badge' : 'Failed to create badge';
        this.loading = false;
        console.error(err);
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/dashboard/badges']);
  }
}
