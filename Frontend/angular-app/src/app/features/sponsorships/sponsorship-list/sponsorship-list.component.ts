import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Sponsorship } from '../../../shared/models/sponsor.model';
import { SponsorshipService } from '../../../shared/sponsorship.service';
import { WorkflowService } from '../../../shared/workflow.service';

@Component({
  selector: 'app-sponsorship-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-gray-50 via-blue-50 to-purple-50 p-6">
      <div class="max-w-7xl mx-auto">
        <!-- Header -->
        <div class="mb-8">
          <div class="flex items-center justify-between mb-4">
            <div>
              <h1 class="text-4xl font-bold text-gray-800 mb-2">Sponsorships</h1>
              <p class="text-gray-600">Manage and track your sponsorship applications</p>
            </div>
          </div>

          <!-- Tab Navigation -->
          <div class="flex space-x-2 bg-white rounded-xl p-2 shadow-md">
            <button (click)="selectedStatus = ''; filterByStatus()"
                    [class]="selectedStatus === '' ? 'bg-gradient-to-r from-blue-500 to-purple-500 text-white' : 'text-gray-600 hover:bg-gray-100'"
                    class="px-6 py-2.5 rounded-lg font-medium transition-all duration-300">
              All Sponsorships
            </button>
            <button (click)="selectedStatus = 'PENDING'; filterByStatus()"
                    [class]="selectedStatus === 'PENDING' ? 'bg-gradient-to-r from-yellow-500 to-orange-500 text-white' : 'text-gray-600 hover:bg-gray-100'"
                    class="px-6 py-2.5 rounded-lg font-medium transition-all duration-300">
              Pending
            </button>
            <button (click)="selectedStatus = 'APPROVED'; filterByStatus()"
                    [class]="selectedStatus === 'APPROVED' ? 'bg-gradient-to-r from-green-500 to-emerald-500 text-white' : 'text-gray-600 hover:bg-gray-100'"
                    class="px-6 py-2.5 rounded-lg font-medium transition-all duration-300">
              Approved
            </button>
            <button (click)="selectedStatus = 'REJECTED'; filterByStatus()"
                    [class]="selectedStatus === 'REJECTED' ? 'bg-gradient-to-r from-red-500 to-pink-500 text-white' : 'text-gray-600 hover:bg-gray-100'"
                    class="px-6 py-2.5 rounded-lg font-medium transition-all duration-300">
              Rejected
            </button>
            <button (click)="selectedStatus = 'COMPLETED'; filterByStatus()"
                    [class]="selectedStatus === 'COMPLETED' ? 'bg-gradient-to-r from-blue-500 to-indigo-500 text-white' : 'text-gray-600 hover:bg-gray-100'"
                    class="px-6 py-2.5 rounded-lg font-medium transition-all duration-300">
              Completed
            </button>
          </div>
        </div>

        <!-- Sponsorships Grid -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div *ngFor="let s of filteredSponsorships; let i = index"
               class="bg-white rounded-2xl shadow-lg hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-2 overflow-hidden card-3d">
            
            <!-- Card Header with Gradient -->
            <div class="bg-gradient-to-r from-purple-500 via-pink-500 to-red-500 p-6 relative">
              <div class="flex items-center justify-between">
                <div class="w-16 h-16 rounded-xl bg-white/20 backdrop-blur-sm flex items-center justify-center shadow-lg">
                  <svg class="w-8 h-8 text-white" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                </div>
                <span class="px-3 py-1 rounded-full text-xs font-bold bg-white/30 backdrop-blur-sm text-white border border-white/40">
                  #{{ i + 1 }}
                </span>
              </div>
              <div class="mt-4">
                <h3 class="text-xl font-bold text-white">{{ s.sponsorshipType || 'N/A' }}</h3>
                <p class="text-white/80 text-sm mt-1">{{ s.targetType }} #{{ s.targetId }}</p>
              </div>
            </div>

            <!-- Card Body -->
            <div class="p-6 space-y-4">
              <!-- Status Badge -->
              <div class="flex items-center justify-between">
                <span class="text-sm font-medium text-gray-500">Status</span>
                <span class="px-3 py-1.5 rounded-lg text-xs font-bold flex items-center space-x-1.5 whitespace-nowrap"
                      [ngClass]="{
                        'bg-yellow-100 text-yellow-800': s.status === 'PENDING',
                        'bg-green-100 text-green-800': s.status === 'APPROVED',
                        'bg-red-100 text-red-800': s.status === 'REJECTED',
                        'bg-gray-100 text-gray-800': s.status === 'CANCELLED',
                        'bg-blue-100 text-blue-800': s.status === 'COMPLETED'
                      }">
                  <span class="w-2 h-2 rounded-full animate-pulse"
                        [ngClass]="{
                          'bg-yellow-500': s.status === 'PENDING',
                          'bg-green-500': s.status === 'APPROVED',
                          'bg-red-500': s.status === 'REJECTED',
                          'bg-gray-500': s.status === 'CANCELLED',
                          'bg-blue-500': s.status === 'COMPLETED'
                        }"></span>
                  <span>{{ s.status || 'N/A' }}</span>
                </span>
              </div>

              <!-- Rejection Reason -->
              <div *ngIf="s.status === 'REJECTED' && s.rejectionReason" class="p-3 bg-red-50 border border-red-200 rounded-lg">
                <p class="text-xs text-red-700 font-medium">❌ {{ s.rejectionReason }}</p>
              </div>

              <!-- Amount -->
              <div class="flex items-center justify-between">
                <span class="text-sm font-medium text-gray-500">Amount</span>
                <span class="text-lg font-bold text-gray-800">{{ s.amountAllocated | number:'1.2-2' }}€</span>
              </div>

              <!-- Visibility Level -->
              <div class="flex items-center justify-between">
                <span class="text-sm font-medium text-gray-500">Visibility</span>
                <span class="px-3 py-1 rounded-lg text-xs font-bold"
                      [ngClass]="{
                        'bg-yellow-100 text-yellow-800': s.visibilityLevel === 'LOGO',
                        'bg-blue-100 text-blue-800': s.visibilityLevel === 'FEATURED',
                        'bg-purple-100 text-purple-800': s.visibilityLevel === 'TITLE'
                      }">
                  {{ s.visibilityLevel || 'N/A' }}
                </span>
              </div>

              <!-- Period -->
              <div class="pt-3 border-t border-gray-100">
                <div class="flex items-center justify-between text-sm">
                  <div>
                    <p class="text-gray-500 text-xs">Start Date</p>
                    <p class="font-medium text-gray-800">{{ s.startDate }}</p>
                  </div>
                  <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"/>
                  </svg>
                  <div class="text-right">
                    <p class="text-gray-500 text-xs">End Date</p>
                    <p class="font-medium text-gray-800">{{ s.endDate }}</p>
                  </div>
                </div>
              </div>

              <!-- Actions -->
              <div class="pt-4 flex gap-2">
                <button *ngIf="s.status === 'PENDING'" 
                        (click)="cancelSponsorship(s.id!)"
                        class="flex-1 bg-orange-500 hover:bg-orange-600 text-white font-semibold py-2.5 px-4 rounded-xl shadow-md hover:shadow-lg transition-all duration-300 transform hover:scale-105 flex items-center justify-center space-x-2">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                  </svg>
                  <span>Cancel</span>
                </button>
                <button *ngIf="s.status === 'PENDING' || s.status === 'REJECTED' || s.status === 'CANCELLED'"
                        (click)="deleteSponsorship(s.id!)"
                        class="flex-1 bg-red-500 hover:bg-red-600 text-white font-semibold py-2.5 px-4 rounded-xl shadow-md hover:shadow-lg transition-all duration-300 transform hover:scale-105 flex items-center justify-center space-x-2">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                  </svg>
                  <span>Delete</span>
                </button>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div *ngIf="filteredSponsorships.length === 0" class="col-span-full">
            <div class="bg-white rounded-2xl shadow-lg p-12 text-center">
              <svg class="w-24 h-24 mx-auto text-gray-300 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"/>
              </svg>
              <h3 class="text-xl font-semibold text-gray-700 mb-2">No sponsorships found</h3>
              <p class="text-gray-500 mb-6">Start by creating your first sponsorship application</p>
              <a routerLink="/dashboard/sponsorships/add"
                 class="inline-flex items-center space-x-2 bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white font-semibold py-3 px-6 rounded-xl shadow-lg hover:shadow-2xl transition-all duration-300 transform hover:scale-105">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
                </svg>
                <span>Create Sponsorship</span>
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class SponsorshipListComponent implements OnInit {
  sponsorships: Sponsorship[] = [];
  filteredSponsorships: Sponsorship[] = [];
  selectedStatus: string = '';

  constructor(
    private sponsorshipService: SponsorshipService,
    private workflowService: WorkflowService
  ) {}

  ngOnInit(): void {
    this.loadSponsorships();
  }

  loadSponsorships(): void {
    this.sponsorshipService.getAllSponsorships().subscribe({
      next: (data) => {
        this.sponsorships = data;
        this.filteredSponsorships = data;
      },
      error: (err) => { /* Error loading sponsorships */ }
    });
  }

  filterByStatus(): void {
    if (this.selectedStatus) {
      this.filteredSponsorships = this.sponsorships.filter(s => s.status === this.selectedStatus);
    } else {
      this.filteredSponsorships = this.sponsorships;
    }
  }

  cancelSponsorship(id: number): void {
    if (confirm('Are you sure you want to cancel this sponsorship? The budget will be returned to your contract.')) {
      this.workflowService.cancelSponsorship(id).subscribe({
        next: () => {
          alert('Sponsorship cancelled successfully!');
          this.loadSponsorships();
        },
        error: (err) => {
          alert('Failed to cancel sponsorship: ' + (err.error?.message || 'Unknown error'));
        }
      });
    }
  }

  deleteSponsorship(id: number): void {
    if (confirm('Are you sure you want to delete this sponsorship?')) {
      this.sponsorshipService.deleteSponsorship(id).subscribe({
        next: () => {
          alert('Sponsorship deleted successfully!');
          this.loadSponsorships();
        },
        error: (err) => {
          alert('Failed to delete sponsorship: ' + (err.error?.message || 'Unknown error'));
        }
      });
    }
  }
}

