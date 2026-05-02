import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WorkflowService } from '../../../shared/workflow.service';
import { Sponsorship, ApprovalRequest } from '../../../shared/models/sponsor.model';

@Component({
  selector: 'app-admin-pending-approvals',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="bg-white rounded-2xl shadow-lg border border-gray-100 overflow-hidden">
      <!-- Header -->
      <div class="p-6 border-b border-gray-100 bg-gradient-to-r from-orange-50 to-red-50">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-12 h-12 bg-orange-500 rounded-xl flex items-center justify-center">
              <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
            </div>
            <div>
              <h3 class="text-xl font-bold text-gray-800">Pending Approvals</h3>
              <p class="text-sm text-gray-600">Review and approve sponsorship requests</p>
            </div>
          </div>
          <div class="flex items-center gap-2">
            <span class="px-4 py-2 bg-orange-500 text-white font-bold rounded-full text-sm">
              {{ pendingCount }} Pending
            </span>
            <button (click)="loadPendingSponsorships()" 
                    class="p-2 hover:bg-white rounded-lg transition">
              <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- Loading -->
      <div *ngIf="isLoading" class="p-12 text-center">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-500 mx-auto"></div>
        <p class="text-gray-500 mt-4">Loading pending sponsorships...</p>
      </div>

      <!-- Empty State -->
      <div *ngIf="!isLoading && pendingSponsorships.length === 0" class="p-12 text-center">
        <div class="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <svg class="w-10 h-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
        </div>
        <h4 class="text-lg font-semibold text-gray-800 mb-2">All Caught Up!</h4>
        <p class="text-gray-500">No pending sponsorships to review</p>
      </div>

      <!-- Pending List -->
      <div *ngIf="!isLoading && pendingSponsorships.length > 0" class="divide-y divide-gray-100">
        <div *ngFor="let sponsorship of pendingSponsorships; let i = index" 
             class="p-6 hover:bg-gray-50 transition-colors">
          
          <!-- Sponsorship Info -->
          <div class="flex items-start justify-between mb-4">
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-2">
                <span class="px-3 py-1 text-xs font-bold rounded-full"
                      [ngClass]="{
                        'bg-blue-100 text-blue-700': sponsorship.sponsorshipType === 'COURSE',
                        'bg-purple-100 text-purple-700': sponsorship.sponsorshipType === 'EVENT',
                        'bg-green-100 text-green-700': sponsorship.sponsorshipType === 'CERTIFICATION',
                        'bg-orange-100 text-orange-700': sponsorship.sponsorshipType === 'OFFER'
                      }">
                  {{ sponsorship.sponsorshipType }}
                </span>
                <span class="px-3 py-1 text-xs font-bold rounded-full bg-yellow-100 text-yellow-700">
                  {{ sponsorship.visibilityLevel }}
                </span>
                <span *ngIf="isRecent(sponsorship.createdAt)" 
                      class="px-2 py-1 text-xs font-bold bg-red-500 text-white rounded-full animate-pulse">
                  NEW
                </span>
              </div>
              
              <h4 class="text-lg font-bold text-gray-800 mb-1">
                {{ sponsorship.sponsorName || 'Unknown Sponsor' }}
              </h4>
              
              <div class="grid grid-cols-2 gap-4 text-sm text-gray-600">
                <div>
                  <span class="font-medium">Contract:</span> {{ sponsorship.contractNumber }}
                </div>
                <div>
                  <span class="font-medium">Amount:</span> 
                  <span class="text-lg font-bold text-gray-800">{{ sponsorship.amountAllocated | number:'1.2-2' }}€</span>
                </div>
                <div>
                  <span class="font-medium">Target:</span> {{ sponsorship.targetType }} #{{ sponsorship.targetId }}
                </div>
                <div>
                  <span class="font-medium">Period:</span> {{ sponsorship.startDate }} → {{ sponsorship.endDate }}
                </div>
                <div class="col-span-2">
                  <span class="font-medium">Created:</span> {{ getTimeAgo(sponsorship.createdAt) }}
                </div>
              </div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="flex gap-3">
            <button (click)="openApproveModal(sponsorship)"
                    [disabled]="processingId === sponsorship.id"
                    class="flex-1 bg-green-500 hover:bg-green-600 text-white font-semibold py-3 px-6 rounded-lg shadow transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
              </svg>
              <span *ngIf="processingId !== sponsorship.id">Approve</span>
              <span *ngIf="processingId === sponsorship.id">Processing...</span>
            </button>
            
            <button (click)="openRejectModal(sponsorship)"
                    [disabled]="processingId === sponsorship.id"
                    class="flex-1 bg-red-500 hover:bg-red-600 text-white font-semibold py-3 px-6 rounded-lg shadow transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
              Reject
            </button>
          </div>

          <!-- Success/Error Messages -->
          <div *ngIf="successMessage && processingId === sponsorship.id" 
               class="mt-3 p-3 bg-green-50 border border-green-200 rounded text-sm text-green-800">
            {{ successMessage }}
          </div>
          <div *ngIf="errorMessage && processingId === sponsorship.id" 
               class="mt-3 p-3 bg-red-50 border border-red-200 rounded text-sm text-red-800">
            {{ errorMessage }}
          </div>
        </div>
      </div>
    </div>

    <!-- Reject Modal -->
    <div *ngIf="showRejectModal" 
         class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
         (click)="closeRejectModal()">
      <div class="bg-white rounded-2xl shadow-2xl max-w-md w-full mx-4 p-6" 
           (click)="$event.stopPropagation()">
        <h3 class="text-xl font-bold text-gray-800 mb-4">Reject Sponsorship</h3>
        <p class="text-gray-600 mb-4">Please provide a reason for rejection:</p>
        
        <textarea [(ngModel)]="rejectionReason"
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent resize-none"
                  rows="4"
                  placeholder="e.g., This course already has a title sponsor..."></textarea>
        
        <div *ngIf="rejectionError" class="mt-3 p-3 bg-red-50 border border-red-200 rounded text-sm text-red-800">
          {{ rejectionError }}
        </div>
        
        <div class="flex gap-3 mt-6">
          <button (click)="confirmReject()"
                  [disabled]="!rejectionReason || rejectionReason.length < 10"
                  class="flex-1 bg-red-500 hover:bg-red-600 text-white font-semibold py-3 px-6 rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed">
            Confirm Rejection
          </button>
          <button (click)="closeRejectModal()"
                  class="px-6 py-3 border border-gray-300 text-gray-700 font-semibold rounded-lg hover:bg-gray-50 transition">
            Cancel
          </button>
        </div>
      </div>
    </div>
  `
})
export class AdminPendingApprovalsComponent implements OnInit {
  pendingSponsorships: Sponsorship[] = [];
  pendingCount: number = 0;
  isLoading = true;
  processingId: number | null = null;
  successMessage = '';
  errorMessage = '';
  
  // Reject modal
  showRejectModal = false;
  selectedSponsorship: Sponsorship | null = null;
  rejectionReason = '';
  rejectionError = '';

  // Admin ID (should come from auth service)
  adminId = 1;

  constructor(private workflowService: WorkflowService) {}

  ngOnInit(): void {
    this.loadPendingSponsorships();
    // Auto-refresh every 30 seconds
    setInterval(() => this.loadPendingSponsorships(), 30000);
  }

  loadPendingSponsorships(): void {
    this.isLoading = true;
    this.workflowService.getPendingSponsorships().subscribe({
      next: (data) => {
        this.pendingSponsorships = data;
        this.pendingCount = data.length;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
      }
    });
  }

  openApproveModal(sponsorship: Sponsorship): void {
    if (confirm(`Approve sponsorship from ${sponsorship.sponsorName} for ${sponsorship.amountAllocated}€?`)) {
      this.approveSponsorsh(sponsorship);
    }
  }

  approveSponsorsh(sponsorship: Sponsorship): void {
    this.processingId = sponsorship.id!;
    this.successMessage = '';
    this.errorMessage = '';

    this.workflowService.approveSponsorship(sponsorship.id!, this.adminId).subscribe({
      next: () => {
        this.successMessage = '✅ Sponsorship approved successfully!';
        setTimeout(() => {
          this.loadPendingSponsorships();
          this.processingId = null;
          this.successMessage = '';
        }, 2000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to approve sponsorship';
        this.processingId = null;
      }
    });
  }

  openRejectModal(sponsorship: Sponsorship): void {
    this.selectedSponsorship = sponsorship;
    this.rejectionReason = '';
    this.rejectionError = '';
    this.showRejectModal = true;
  }

  closeRejectModal(): void {
    this.showRejectModal = false;
    this.selectedSponsorship = null;
    this.rejectionReason = '';
    this.rejectionError = '';
  }

  confirmReject(): void {
    if (!this.rejectionReason || this.rejectionReason.length < 10) {
      this.rejectionError = 'Rejection reason must be at least 10 characters';
      return;
    }

    if (!this.selectedSponsorship) return;

    this.processingId = this.selectedSponsorship.id!;
    this.successMessage = '';
    this.errorMessage = '';

    const request: ApprovalRequest = {
      adminId: this.adminId,
      reason: this.rejectionReason
    };

    this.workflowService.rejectSponsorship(this.selectedSponsorship.id!, request).subscribe({
      next: () => {
        this.successMessage = '✅ Sponsorship rejected';
        this.closeRejectModal();
        setTimeout(() => {
          this.loadPendingSponsorships();
          this.processingId = null;
          this.successMessage = '';
        }, 2000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to reject sponsorship';
        this.processingId = null;
        this.closeRejectModal();
      }
    });
  }

  isRecent(createdAt?: string): boolean {
    if (!createdAt) return false;
    const created = new Date(createdAt);
    const now = new Date();
    const diffHours = (now.getTime() - created.getTime()) / (1000 * 60 * 60);
    return diffHours < 1;
  }

  getTimeAgo(createdAt?: string): string {
    if (!createdAt) return 'Unknown';
    const created = new Date(createdAt);
    const now = new Date();
    const diffMs = now.getTime() - created.getTime();
    const diffMins = Math.floor(diffMs / (1000 * 60));
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffMins < 60) return `${diffMins} minute${diffMins !== 1 ? 's' : ''} ago`;
    if (diffHours < 24) return `${diffHours} hour${diffHours !== 1 ? 's' : ''} ago`;
    return `${diffDays} day${diffDays !== 1 ? 's' : ''} ago`;
  }

  getTotalPendingAmount(): number {
    return this.pendingSponsorships.reduce((total, sponsorship) => {
      return total + (sponsorship.amountAllocated || 0);
    }, 0);
  }
}
