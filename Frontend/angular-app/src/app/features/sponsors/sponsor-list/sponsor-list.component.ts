import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Sponsor } from '../../../shared/models/sponsor.model';
import { SponsorService } from '../../../shared/sponsor.service';

@Component({
  selector: 'app-sponsor-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="p-8 bg-gray-50 min-h-screen">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-2">Sponsors Management</h1>
        <p class="text-gray-600">Oversee and organize all event partnership tiers and relationships.</p>
      </div>

      <!-- Tabs and Actions Bar -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 mb-6">
        <div class="flex items-center justify-between p-6 border-b border-gray-200">
          <!-- Tabs -->
          <div class="flex items-center space-x-6">
            <button (click)="activeTab = 'all'; filterSponsors()" 
                    [class]="activeTab === 'all' ? 'text-blue-600 border-b-2 border-blue-600 font-semibold' : 'text-gray-600 hover:text-gray-900'"
                    class="pb-2 px-1 transition-colors">
              All Sponsors
              <span class="ml-2 px-2 py-0.5 bg-gray-100 text-gray-600 rounded-full text-xs font-medium">{{ getTotalCount() }}</span>
            </button>
            <button (click)="activeTab = 'active'; filterSponsors()" 
                    [class]="activeTab === 'active' ? 'text-blue-600 border-b-2 border-blue-600 font-semibold' : 'text-gray-600 hover:text-gray-900'"
                    class="pb-2 px-1 transition-colors">
              Active
            </button>
            <button (click)="activeTab = 'inactive'; filterSponsors()" 
                    [class]="activeTab === 'inactive' ? 'text-blue-600 border-b-2 border-blue-600 font-semibold' : 'text-gray-600 hover:text-gray-900'"
                    class="pb-2 px-1 transition-colors">
              Inactive
            </button>
          </div>

          <!-- Actions -->
          <div class="flex items-center space-x-3">
            <a routerLink="/dashboard/sponsors/add"
               class="flex items-center space-x-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg shadow-sm transition-colors">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
              </svg>
              <span class="text-sm font-medium">Add Sponsor</span>
            </a>
          </div>
        </div>

        <!-- Search and Filter Bar -->
        <div class="p-6 bg-gray-50 border-b border-gray-200">
          <div class="flex items-center space-x-4">
            <!-- Search -->
            <div class="flex-1 relative">
              <svg class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
              </svg>
              <input type="text" 
                     [(ngModel)]="searchTerm"
                     (input)="filterSponsors()"
                     placeholder="Search sponsors..." 
                     class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
            </div>

            <!-- Filter Button -->
            <button (click)="showFilters = !showFilters"
                    class="flex items-center space-x-2 px-4 py-2 border border-gray-300 rounded-lg hover:bg-white transition-colors">
              <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"/>
              </svg>
              <span class="text-sm font-medium text-gray-700">Filters</span>
            </button>
          </div>
        </div>

        <!-- Table Header -->
        <div class="grid grid-cols-12 gap-4 px-6 py-4 bg-gray-50 border-b border-gray-200 text-xs font-semibold text-gray-600 uppercase tracking-wider">
          <div class="col-span-3">Sponsor Details</div>
          <div class="col-span-3">Company</div>
          <div class="col-span-3">Email</div>
          <div class="col-span-2">Status</div>
          <div class="col-span-1 text-right">Actions</div>
        </div>

        <!-- Sponsor List -->
        <div class="divide-y divide-gray-200">
          <div *ngFor="let sponsor of paginatedSponsors; let i = index" 
               class="grid grid-cols-12 gap-4 px-6 py-4 hover:bg-gray-50 transition-colors items-center">
            
            <!-- Sponsor Details -->
            <div class="col-span-3 flex items-center space-x-3">
              <div class="w-12 h-12 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-lg flex items-center justify-center text-white font-bold text-lg shadow-md">
                {{ getInitials(sponsor.name) }}
              </div>
              <div>
                <div class="font-semibold text-gray-900">{{ sponsor.name }}</div>
                <div class="text-sm text-gray-500">{{ sponsor.phone || 'N/A' }}</div>
              </div>
            </div>

            <!-- Company -->
            <div class="col-span-3">
              <div class="font-medium text-gray-900">{{ sponsor.companyName || 'N/A' }}</div>
              <div class="text-sm text-gray-500">{{ sponsor.industry || 'N/A' }}</div>
            </div>

            <!-- Email -->
            <div class="col-span-3">
              <div class="font-medium text-gray-900">{{ sponsor.email }}</div>
            </div>

            <!-- Status -->
            <div class="col-span-2">
              <span *ngIf="sponsor.status === 'ACTIVE'" 
                    class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                <span class="w-1.5 h-1.5 bg-green-500 rounded-full mr-2"></span>
                ACTIVE
              </span>
              <span *ngIf="sponsor.status !== 'ACTIVE'" 
                    class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                <span class="w-1.5 h-1.5 bg-gray-500 rounded-full mr-2"></span>
                INACTIVE
              </span>
            </div>

            <!-- Actions -->
            <div class="col-span-1 flex items-center justify-end space-x-2">
              <a [routerLink]="['/dashboard/sponsors/edit', sponsor.id]"
                 class="action-icon edit-icon p-2 text-blue-600 hover:text-white bg-blue-50 hover:bg-blue-600 rounded-lg transition-all duration-300 transform hover:scale-110 hover:rotate-12"
                 title="Edit">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                </svg>
              </a>
              <button (click)="confirmDelete(sponsor)"
                      class="action-icon delete-icon p-2 text-red-600 hover:text-white bg-red-50 hover:bg-red-600 rounded-lg transition-all duration-300 transform hover:scale-110 hover:-rotate-12"
                      title="Delete">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                </svg>
              </button>
            </div>
          </div>

          <!-- Empty State -->
          <div *ngIf="filteredSponsors.length === 0" class="px-6 py-12 text-center">
            <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"/>
            </svg>
            <h3 class="mt-2 text-sm font-medium text-gray-900">No sponsors found</h3>
            <p class="mt-1 text-sm text-gray-500">Get started by creating a new sponsor.</p>
          </div>
        </div>

        <!-- Pagination -->
        <div class="px-6 py-4 bg-gray-50 border-t border-gray-200 flex items-center justify-between">
          <div class="text-sm text-gray-700">
            Showing <span class="font-medium">{{ getStartIndex() }}</span> to <span class="font-medium">{{ getEndIndex() }}</span> of <span class="font-medium">{{ filteredSponsors.length }}</span> sponsors
          </div>
          <div class="flex items-center space-x-2">
            <button (click)="previousPage()" 
                    [disabled]="currentPage === 1"
                    [class.opacity-50]="currentPage === 1"
                    [class.cursor-not-allowed]="currentPage === 1"
                    class="px-3 py-2 border border-gray-300 rounded-lg hover:bg-white transition-colors disabled:hover:bg-gray-50">
              <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
              </svg>
            </button>
            
            <button *ngFor="let page of getPageNumbers()" 
                    (click)="goToPage(page)"
                    [class.bg-blue-600]="page === currentPage"
                    [class.text-white]="page === currentPage"
                    [class.hover:bg-gray-100]="page !== currentPage"
                    class="px-4 py-2 border border-gray-300 rounded-lg transition-colors">
              {{ page }}
            </button>

            <button (click)="nextPage()" 
                    [disabled]="currentPage === getTotalPages()"
                    [class.opacity-50]="currentPage === getTotalPages()"
                    [class.cursor-not-allowed]="currentPage === getTotalPages()"
                    class="px-3 py-2 border border-gray-300 rounded-lg hover:bg-white transition-colors disabled:hover:bg-gray-50">
              <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div *ngIf="showDeleteModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-2xl p-6 max-w-md w-full mx-4 animate-scale-in">
        <div class="flex items-center justify-center w-12 h-12 bg-red-100 rounded-full mx-auto mb-4">
          <svg class="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z"/>
          </svg>
        </div>
        <h3 class="text-lg font-semibold text-gray-900 text-center mb-2">Delete Sponsor</h3>
        <p class="text-sm text-gray-600 text-center mb-6">
          Are you sure you want to delete <span class="font-semibold">{{ sponsorToDelete?.name }}</span>? This action cannot be undone.
        </p>
        <div class="flex space-x-3">
          <button (click)="showDeleteModal = false" 
                  class="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors font-medium">
            Cancel
          </button>
          <button (click)="deleteSponsor()" 
                  class="flex-1 px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors font-medium">
            Delete
          </button>
        </div>
      </div>
    </div>
  `
})
export class SponsorListComponent implements OnInit {
  sponsors: Sponsor[] = [];
  filteredSponsors: Sponsor[] = [];
  paginatedSponsors: Sponsor[] = [];
  
  activeTab: 'all' | 'active' | 'inactive' = 'all';
  searchTerm: string = '';
  showFilters: boolean = false;
  
  currentPage: number = 1;
  itemsPerPage: number = 4;
  
  showDeleteModal: boolean = false;
  sponsorToDelete: Sponsor | null = null;

  constructor(private sponsorService: SponsorService) {}

  ngOnInit(): void {
    this.loadSponsors();
  }

  loadSponsors(): void {
    this.sponsorService.getAllSponsors().subscribe({
      next: (data) => {
        this.sponsors = data;
        this.filterSponsors();
      },
      error: (err) => { /* Error loading sponsors */ }
    });
  }

  filterSponsors(): void {
    let filtered = [...this.sponsors];

    // Filter by tab
    if (this.activeTab === 'active') {
      filtered = filtered.filter(s => s.status === 'ACTIVE');
    } else if (this.activeTab === 'inactive') {
      filtered = filtered.filter(s => s.status !== 'ACTIVE');
    }

    // Filter by search term
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(s => 
        s.name?.toLowerCase().includes(term) ||
        s.email?.toLowerCase().includes(term) ||
        s.companyName?.toLowerCase().includes(term) ||
        s.industry?.toLowerCase().includes(term)
      );
    }

    this.filteredSponsors = filtered;
    this.currentPage = 1;
    this.updatePagination();
  }

  updatePagination(): void {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    this.paginatedSponsors = this.filteredSponsors.slice(start, end);
  }

  getTotalPages(): number {
    return Math.ceil(this.filteredSponsors.length / this.itemsPerPage);
  }

  getPageNumbers(): number[] {
    const total = this.getTotalPages();
    const pages: number[] = [];
    
    if (total <= 5) {
      for (let i = 1; i <= total; i++) {
        pages.push(i);
      }
    } else {
      if (this.currentPage <= 3) {
        pages.push(1, 2, 3, 4, total);
      } else if (this.currentPage >= total - 2) {
        pages.push(1, total - 3, total - 2, total - 1, total);
      } else {
        pages.push(1, this.currentPage - 1, this.currentPage, this.currentPage + 1, total);
      }
    }
    
    return pages;
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.updatePagination();
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.updatePagination();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.getTotalPages()) {
      this.currentPage++;
      this.updatePagination();
    }
  }

  getStartIndex(): number {
    return this.filteredSponsors.length === 0 ? 0 : (this.currentPage - 1) * this.itemsPerPage + 1;
  }

  getEndIndex(): number {
    const end = this.currentPage * this.itemsPerPage;
    return end > this.filteredSponsors.length ? this.filteredSponsors.length : end;
  }

  getTotalCount(): number {
    return this.sponsors.length;
  }

  getInitials(name: string): string {
    if (!name) return '?';
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  confirmDelete(sponsor: Sponsor): void {
    this.sponsorToDelete = sponsor;
    this.showDeleteModal = true;
  }

  deleteSponsor(): void {
    if (this.sponsorToDelete?.id) {
      this.sponsorService.deleteSponsor(this.sponsorToDelete.id).subscribe({
        next: () => {
          this.showDeleteModal = false;
          this.sponsorToDelete = null;
          this.loadSponsors();
        },
        error: (err) => {
          this.showDeleteModal = false;
        }
      });
    }
  }
}
