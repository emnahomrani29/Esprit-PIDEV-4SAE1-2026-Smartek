import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Contract, Sponsor } from '../../../shared/models/sponsor.model';
import { ContractService } from '../../../shared/contract.service';
import { SponsorService } from '../../../shared/sponsor.service';

@Component({
  selector: 'app-edit-contract',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-gray-50 via-blue-50 to-purple-50 py-12 px-4 flex items-center justify-center">
      <div class="w-full max-w-3xl">
        <!-- Success message -->
        <div *ngIf="successMessage" class="mb-6 p-4 bg-green-50 border border-green-200 text-green-700 rounded-xl shadow-sm animate-fade-in">
          <div class="flex items-center">
            <svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
            {{ successMessage }}
          </div>
        </div>

        <!-- Error message -->
        <div *ngIf="errorMessage" class="mb-6 p-4 bg-red-50 border border-red-200 text-red-700 rounded-xl shadow-sm animate-fade-in">
          <div class="flex items-center">
            <svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
            </svg>
            {{ errorMessage }}
          </div>
        </div>

        <!-- Main Card -->
        <div *ngIf="contract" class="bg-white rounded-3xl shadow-2xl p-8 animate-scale-in">
          <!-- Header -->
          <div class="text-center mb-8">
            <h1 class="text-4xl font-bold text-gray-800 mb-3">Edit Contract</h1>
            <p class="text-gray-500">Update sponsorship agreement and partnership contract details.</p>
          </div>

          <!-- Form -->
          <div class="space-y-5">
            <!-- Sponsor Selection -->
            <div>
              <label class="block text-sm font-semibold text-gray-700 mb-2">Sponsor <span class="text-red-500">*</span></label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                  <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
                  </svg>
                </div>
                <select [(ngModel)]="sponsorId"
                        class="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all bg-gray-50 hover:bg-white appearance-none cursor-pointer">
                  <option [ngValue]="null" disabled>-- Select a Sponsor --</option>
                  <option *ngFor="let s of sponsors" [ngValue]="s.id">
                    {{ s.name }} ({{ s.email }})
                  </option>
                </select>
                <div class="absolute inset-y-0 right-0 flex items-center pr-4 pointer-events-none">
                  <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
                  </svg>
                </div>
              </div>
            </div>

            <!-- Contract Number -->
            <div>
              <label class="block text-sm font-semibold text-gray-700 mb-2">Contract Number <span class="text-red-500">*</span></label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                  <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 20l4-16m2 16l4-16M6 9h14M4 15h14"/>
                  </svg>
                </div>
                <input type="text" [(ngModel)]="contract.contractNumber"
                       class="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all bg-gray-50 hover:bg-white"
                       placeholder="e.g. CNTR-2026-001" />
              </div>
            </div>

            <!-- Date Range -->
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-semibold text-gray-700 mb-2">Start Date <span class="text-red-500">*</span></label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                    <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>
                    </svg>
                  </div>
                  <input type="date" [(ngModel)]="contract.startDate"
                         class="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all bg-gray-50 hover:bg-white" />
                </div>
              </div>
              <div>
                <label class="block text-sm font-semibold text-gray-700 mb-2">End Date <span class="text-red-500">*</span></label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                    <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>
                    </svg>
                  </div>
                  <input type="date" [(ngModel)]="contract.endDate"
                         class="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all bg-gray-50 hover:bg-white" />
                </div>
              </div>
            </div>

            <!-- Amount and Currency -->
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-semibold text-gray-700 mb-2">Amount</label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                    <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                  </div>
                  <input type="number" [(ngModel)]="contract.amount"
                         class="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all bg-gray-50 hover:bg-white"
                         placeholder="10000" />
                </div>
              </div>
              <div>
                <label class="block text-sm font-semibold text-gray-700 mb-2">Currency</label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                    <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                  </div>
                  <input type="text" [(ngModel)]="contract.currency"
                         class="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all bg-gray-50 hover:bg-white"
                         placeholder="USD" />
                </div>
              </div>
            </div>

            <!-- Description -->
            <div>
              <label class="block text-sm font-semibold text-gray-700 mb-2">Description</label>
              <textarea [(ngModel)]="contract.description" rows="4"
                        class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all bg-gray-50 hover:bg-white resize-none"
                        placeholder="Enter contract description and terms..."></textarea>
            </div>

            <!-- Status and Type -->
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-semibold text-gray-700 mb-2">Status</label>
                <div class="relative">
                  <select [(ngModel)]="contract.status"
                          class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all bg-gray-50 hover:bg-white appearance-none cursor-pointer">
                    <option value="DRAFT">✎ Draft</option>
                    <option value="ACTIVE">✓ Active</option>
                    <option value="EXPIRED">✗ Expired</option>
                    <option value="TERMINATED">⊗ Terminated</option>
                  </select>
                  <div class="absolute inset-y-0 right-0 flex items-center pr-4 pointer-events-none">
                    <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
                    </svg>
                  </div>
                </div>
              </div>
              <div>
                <label class="block text-sm font-semibold text-gray-700 mb-2">Type</label>
                <div class="relative">
                  <select [(ngModel)]="contract.type"
                          class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all bg-gray-50 hover:bg-white appearance-none cursor-pointer">
                    <option value="COURSE">📚 Course</option>
                    <option value="EVENT">🎉 Event</option>
                    <option value="CERTIFICATION">🎓 Certification</option>
                    <option value="GLOBAL">🌍 Global</option>
                  </select>
                  <div class="absolute inset-y-0 right-0 flex items-center pr-4 pointer-events-none">
                    <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
                    </svg>
                  </div>
                </div>
              </div>
            </div>

            <!-- Buttons -->
            <div class="pt-4 flex gap-4 items-center">
              <button (click)="updateContract()"
                      [disabled]="isLoading"
                      class="flex-1 bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white font-semibold py-4 px-6 rounded-xl shadow-lg hover:shadow-2xl transition-all duration-300 transform hover:scale-[1.02] hover:-translate-y-1 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none flex items-center justify-center space-x-2 relative overflow-hidden group">
                <div class="absolute inset-0 bg-gradient-to-r from-white/0 via-white/20 to-white/0 transform -skew-x-12 -translate-x-full group-hover:translate-x-full transition-transform duration-1000"></div>
                <svg class="w-5 h-5 relative z-10 transform group-hover:rotate-12 transition-transform duration-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
                </svg>
                <span class="relative z-10">{{ isLoading ? 'Updating...' : 'Update Contract' }}</span>
              </button>
              
              <button (click)="cancel()"
                      [disabled]="isLoading"
                      class="bg-white hover:bg-gray-50 text-gray-600 hover:text-gray-800 font-medium py-4 px-8 rounded-xl border border-gray-300 hover:border-gray-400 transition-all duration-300 transform hover:scale-105">
                Cancel
              </button>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="text-center mt-8 text-gray-500 text-sm">
          © 2024 Smartek Event Management Systems. All rights reserved.
        </div>
      </div>
    </div>
  `
})
export class EditContractComponent implements OnInit {
  contract: Contract | null = null;
  sponsors: Sponsor[] = [];
  sponsorId: number | null = null;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  private contractId!: number;

  constructor(
    private contractService: ContractService,
    private sponsorService: SponsorService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.contractId = Number(this.route.snapshot.paramMap.get('id'));

    this.sponsorService.getAllSponsors().subscribe({
      next: (data) => this.sponsors = data,
      error: (err) => { /* Error loading sponsors */ }
    });

    this.contractService.getContractById(this.contractId).subscribe({
      next: (data) => {
        this.contract = data;
        this.sponsorId = data.sponsor?.id || null;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load contract.';
      }
    });
  }

  updateContract(): void {
    if (!this.contract || !this.sponsorId) return;
    this.errorMessage = '';
    this.successMessage = '';
    this.isLoading = true;
    this.contractService.updateContract(this.contractId, this.sponsorId, this.contract).subscribe({
      next: () => {
        this.successMessage = 'Contract updated successfully!';
        this.isLoading = false;
        setTimeout(() => this.router.navigate(['/dashboard/contracts']), 1000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to update contract.';
        this.isLoading = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/dashboard/contracts']);
  }
}

