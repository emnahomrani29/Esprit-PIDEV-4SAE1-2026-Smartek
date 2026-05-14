import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Sponsorship } from '../../../shared/models/sponsor.model';
import { SponsorshipService } from '../../../shared/sponsorship.service';

@Component({
  selector: 'app-add-sponsorship',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="p-6 max-w-xl mx-auto">
      <h1 class="text-2xl font-bold text-gray-800 mb-6">Add Sponsorship</h1>

      <div *ngIf="successMessage" class="mb-4 p-3 bg-green-100 border border-green-400 text-green-700 rounded-lg">
        {{ successMessage }}
      </div>

      <div *ngIf="errorMessage" class="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded-lg">
        {{ errorMessage }}
      </div>

      <div class="bg-white rounded-lg shadow p-6 space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Contract ID</label>
          <input type="number" [(ngModel)]="contractId"
                 class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                 placeholder="Enter contract ID" />
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Sponsorship Type</label>
            <select [(ngModel)]="sponsorship.sponsorshipType"
                    class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500">
              <option value="COURSE">Course</option>
              <option value="EVENT">Event</option>
              <option value="CERTIFICATION">Certification</option>
              <option value="OFFER">Offer</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Amount Allocated</label>
            <input type="number" [(ngModel)]="sponsorship.amountAllocated"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                   placeholder="e.g. 5000" />
          </div>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Start Date</label>
            <input type="date" [(ngModel)]="sponsorship.startDate"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">End Date</label>
            <input type="date" [(ngModel)]="sponsorship.endDate"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500" />
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Visibility Level</label>
          <select [(ngModel)]="sponsorship.visibilityLevel"
                  class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500">
            <option value="LOGO">Logo</option>
            <option value="FEATURED">Featured</option>
            <option value="TITLE">Title</option>
          </select>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Target Type</label>
            <select [(ngModel)]="sponsorship.targetType"
                    class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500">
              <option value="COURSE">Course</option>
              <option value="EVENT">Event</option>
              <option value="CERTIFICATION">Certification</option>
              <option value="OFFER">Offer</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Target ID</label>
            <input type="number" [(ngModel)]="sponsorship.targetId"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                   placeholder="e.g. 1" />
          </div>
        </div>

        <div class="flex gap-3 pt-2">
          <button (click)="addSponsorship()"
                  [disabled]="isLoading"
                  class="bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 px-4 rounded-lg shadow transition disabled:opacity-50 disabled:cursor-not-allowed">
            {{ isLoading ? 'Saving...' : 'Save' }}
          </button>
          <button (click)="cancel()"
                  [disabled]="isLoading"
                  class="bg-gray-300 hover:bg-gray-400 text-gray-800 font-semibold py-2 px-4 rounded-lg shadow transition">
            Cancel
          </button>
        </div>
      </div>
    </div>
  `
})
export class AddSponsorshipComponent {
  contractId!: number;
  sponsorship: Sponsorship = {
    sponsorshipType: 'COURSE',
    visibilityLevel: 'LOGO',
    targetType: 'COURSE'
  } as Sponsorship;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private sponsorshipService: SponsorshipService,
    private router: Router
  ) {}

  addSponsorship(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.contractId) {
      this.errorMessage = 'Contract ID is required.';
      return;
    }

    this.isLoading = true;
    this.sponsorshipService.createSponsorship(this.contractId, this.sponsorship).subscribe({
      next: () => {
        this.successMessage = 'Sponsorship created successfully!';
        this.isLoading = false;
        setTimeout(() => this.router.navigate(['/dashboard/sponsorships']), 1000);
      },
      error: (err) => {
        console.error('Error creating sponsorship', err);
        this.errorMessage = err.error?.message || err.message || 'Failed to create sponsorship. Make sure the backend is running on port 8080.';
        this.isLoading = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/dashboard/sponsorships']);
  }
}

