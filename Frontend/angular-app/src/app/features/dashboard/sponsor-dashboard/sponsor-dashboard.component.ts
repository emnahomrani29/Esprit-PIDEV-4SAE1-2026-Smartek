import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService, AuthResponse } from '../../../core/services/auth.service';
import { SponsorService } from '../../../shared/sponsor.service';
import { SponsorshipService } from '../../../shared/sponsorship.service';
import { SponsorDashboard, Contract, CreateSponsorshipRequest } from '../../../shared/models/sponsor.model';

@Component({
  selector: 'app-sponsor-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './sponsor-dashboard.component.html',
  styleUrl: './sponsor-dashboard.component.scss'
})
export class SponsorDashboardComponent implements OnInit {
  currentUser: AuthResponse | null = null;
  dashboard: SponsorDashboard | null = null;
  isLoading = true;
  errorMessage = '';

  // Create Sponsorship Modal
  showCreateSponsorshipModal = false;
  sponsorshipForm: FormGroup;
  selectedContract: Contract | null = null;
  suggestedTier: string = '';
  remainingBudget: number = 0;
  createErrorMessage: string = '';
  createSuccessMessage: string = '';
  createLoading: boolean = false;

  constructor(
    private authService: AuthService,
    private sponsorService: SponsorService,
    private sponsorshipService: SponsorshipService,
    private fb: FormBuilder
  ) {
    this.sponsorshipForm = this.fb.group({
      contractId: ['', Validators.required],
      sponsorshipType: ['', Validators.required],
      targetType: ['', Validators.required],
      targetId: ['', Validators.required],
      amountAllocated: ['', [Validators.required, Validators.min(1)]],
      visibilityLevel: ['', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.currentUser = this.authService.getUserInfo();
    if (this.currentUser?.email) {
      this.loadDashboard(this.currentUser.email);
    } else {
      this.isLoading = false;
      this.errorMessage = 'User not authenticated.';
    }
  }
  loadDashboard(email: string): void {
    this.sponsorService.getSponsorByEmail(email).subscribe({
      next: (sponsor) => {
        this.sponsorService.getSponsorDashboard(sponsor.id!).subscribe({
          next: (data) => {
            this.dashboard = data;
            this.isLoading = false;
          },
          error: (err) => {
            this.isLoading = false;
            this.errorMessage = 'Failed to load dashboard data.';
          }
        });
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Your sponsor profile has not been created yet. Please contact the administrator.';
      }
    });
  }

  getSpentPercentage(): number {
    if (!this.dashboard || this.dashboard.totalContractAmount === 0) return 0;
    return Math.min((this.dashboard.totalSpent / this.dashboard.totalContractAmount) * 100, 100);
  }

  getCurrency(): string {
    if (this.dashboard?.contracts?.length) {
      return this.dashboard.contracts[0].currency || 'TND';
    }
    return 'TND';
  }

  onContractChange(): void {
    const contractId = this.sponsorshipForm.get('contractId')?.value;
    this.selectedContract = this.dashboard?.contracts?.find(c => c.id === +contractId) || null;
    this.calculateRemainingBudget();
  }

  onAmountChange(): void {
    const amount = this.sponsorshipForm.get('amountAllocated')?.value;
    if (amount) {
      this.suggestTier(amount);
    }
    this.calculateRemainingBudget();
  }

  suggestTier(amount: number): void {
    if (amount >= 3000) {
      this.suggestedTier = 'TITLE';
    } else if (amount >= 1500) {
      this.suggestedTier = 'FEATURED';
    } else if (amount >= 500) {
      this.suggestedTier = 'LOGO';
    } else {
      this.suggestedTier = '';
    }
  }

  calculateRemainingBudget(): void {
    if (this.selectedContract && this.selectedContract.amount) {
      const amount = this.sponsorshipForm.get('amountAllocated')?.value || 0;
      this.remainingBudget = this.selectedContract.amount - amount;
    }
  }
  onCreateSponsorship(): void {
    if (this.sponsorshipForm.valid) {
      this.createLoading = true;
      this.createErrorMessage = '';
      this.createSuccessMessage = '';

      const formValue = this.sponsorshipForm.value;
      const request: CreateSponsorshipRequest = {
        contractId: +formValue.contractId,
        sponsorshipType: formValue.sponsorshipType,
        targetType: formValue.targetType, // Use the actual targetType field
        targetId: +formValue.targetId,
        amountAllocated: +formValue.amountAllocated,
        visibilityLevel: formValue.visibilityLevel,
        startDate: formValue.startDate,
        endDate: formValue.endDate
      };

      this.sponsorshipService.createSponsorship(request).subscribe({
        next: () => {
          this.createSuccessMessage = 'Sponsorship created successfully! Waiting for admin approval.';
          this.createLoading = false;
          setTimeout(() => {
            this.showCreateSponsorshipModal = false;
            this.sponsorshipForm.reset();
            this.loadDashboard(this.currentUser!.email);
          }, 2000);
        },
        error: (error: any) => {
          this.createErrorMessage = error.error?.message || 'Failed to create sponsorship';
          this.createLoading = false;
        }
      });
    }
  }

  logout(): void {
    this.authService.logout();
  }
}