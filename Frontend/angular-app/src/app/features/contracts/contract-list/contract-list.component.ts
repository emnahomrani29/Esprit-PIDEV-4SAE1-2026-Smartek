import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Contract, BudgetSummary } from '../../../shared/models/sponsor.model';
import { ContractService } from '../../../shared/contract.service';
import { BudgetService } from '../../../shared/budget.service';

@Component({
  selector: 'app-contract-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="p-8 bg-gray-50 min-h-screen">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-2">Contracts Management</h1>
        <p class="text-gray-600">Manage and track all sponsorship agreements and partnerships.</p>
      </div>

      <!-- Statistics Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <!-- Total Contracts -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow card-3d">
          <div class="flex items-center justify-between mb-4">
            <div class="w-14 h-14 bg-gradient-to-br from-blue-400 to-blue-600 rounded-xl flex items-center justify-center shadow-lg transform hover:scale-110 transition-transform icon-3d">
              <svg class="w-7 h-7 text-white" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-sm font-semibold text-gray-600 bg-gray-100 px-2 py-1 rounded-full">{{ getPercentageChange('total') }}</span>
          </div>
          <div class="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Total Contracts</div>
          <div class="text-3xl font-bold text-gray-900">{{ getTotalContracts() | number }}</div>
        </div>

        <!-- Active Contracts -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow card-3d">
          <div class="flex items-center justify-between mb-4">
            <div class="w-14 h-14 bg-gradient-to-br from-green-400 to-green-600 rounded-xl flex items-center justify-center shadow-lg transform hover:scale-110 transition-transform icon-3d">
              <svg class="w-7 h-7 text-white" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-sm font-semibold text-green-600 bg-green-100 px-2 py-1 rounded-full">{{ getPercentageChange('active') }}</span>
          </div>
          <div class="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Active</div>
          <div class="text-3xl font-bold text-gray-900">{{ getActiveContracts() | number }}</div>
        </div>

        <!-- Expired Contracts -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow card-3d">
          <div class="flex items-center justify-between mb-4">
            <div class="w-14 h-14 bg-gradient-to-br from-red-400 to-red-600 rounded-xl flex items-center justify-center shadow-lg transform hover:scale-110 transition-transform icon-3d">
              <svg class="w-7 h-7 text-white" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-sm font-semibold text-red-600 bg-red-100 px-2 py-1 rounded-full">{{ getPercentageChange('expired') }}</span>
          </div>
          <div class="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Expired</div>
          <div class="text-3xl font-bold text-gray-900">{{ getExpiredContracts() | number }}</div>
        </div>

        <!-- Pending Contracts -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow card-3d">
          <div class="flex items-center justify-between mb-4">
            <div class="w-14 h-14 bg-gradient-to-br from-purple-400 to-purple-600 rounded-xl flex items-center justify-center shadow-lg transform hover:scale-110 transition-transform icon-3d">
              <svg class="w-7 h-7 text-white" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-sm font-semibold text-purple-600 bg-purple-100 px-2 py-1 rounded-full">{{ getPercentageChange('pending') }}</span>
          </div>
          <div class="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Pending</div>
          <div class="text-3xl font-bold text-gray-900">{{ getPendingContracts() | number }}</div>
        </div>
      </div>

      <!-- Tabs and Actions Bar -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 mb-6">
        <div class="flex items-center justify-between p-6 border-b border-gray-200">
          <!-- Tabs -->
          <div class="flex items-center space-x-6">
            <button (click)="activeTab = 'all'; filterContracts()" 
                    [class]="activeTab === 'all' ? 'text-blue-600 border-b-2 border-blue-600 font-semibold' : 'text-gray-600 hover:text-gray-900'"
                    class="pb-2 px-1 transition-colors">
              All Contracts
              <span class="ml-2 px-2 py-0.5 bg-gray-100 text-gray-600 rounded-full text-xs font-medium">{{ getTotalCount() }}</span>
            </button>
            <button (click)="activeTab = 'active'; filterContracts()" 
                    [class]="activeTab === 'active' ? 'text-blue-600 border-b-2 border-blue-600 font-semibold' : 'text-gray-600 hover:text-gray-900'"
                    class="pb-2 px-1 transition-colors">
              Active
            </button>
            <button (click)="activeTab = 'expiring'; filterContracts()" 
                    [class]="activeTab === 'expiring' ? 'text-blue-600 border-b-2 border-blue-600 font-semibold' : 'text-gray-600 hover:text-gray-900'"
                    class="pb-2 px-1 transition-colors">
              Expiring Soon
            </button>
            <button (click)="activeTab = 'expired'; filterContracts()" 
                    [class]="activeTab === 'expired' ? 'text-blue-600 border-b-2 border-blue-600 font-semibold' : 'text-gray-600 hover:text-gray-900'"
                    class="pb-2 px-1 transition-colors">
              Expired
            </button>
          </div>

          <!-- Actions -->
          <div class="flex items-center space-x-3">
            <a routerLink="/dashboard/contracts/add"
               class="flex items-center space-x-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg shadow-sm transition-colors">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
              </svg>
              <span class="text-sm font-medium">Add Contract</span>
            </a>
          </div>
        </div>

        <!-- Search Bar -->
        <div class="p-6 bg-gray-50 border-b border-gray-200">
          <div class="flex items-center space-x-4">
            <div class="flex-1 relative">
              <svg class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
              </svg>
              <input type="text" 
                     [(ngModel)]="searchTerm"
                     (input)="filterContracts()"
                     placeholder="Search contracts..." 
                     class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
            </div>
          </div>
        </div>

        <!-- Table Header -->
        <div class="grid grid-cols-12 gap-4 px-6 py-4 bg-gray-50 border-b border-gray-200 text-xs font-semibold text-gray-600 uppercase tracking-wider">
          <div class="col-span-2">Contract Details</div>
          <div class="col-span-2">Sponsor</div>
          <div class="col-span-2">Period</div>
          <div class="col-span-2">Budget</div>
          <div class="col-span-2">Status</div>
          <div class="col-span-1">Type</div>
          <div class="col-span-1 text-right pr-2">Actions</div>
        </div>

        <!-- Contract List -->
        <div class="divide-y divide-gray-200">
          <div *ngFor="let contract of paginatedContracts; let i = index" 
               class="grid grid-cols-12 gap-4 px-6 py-4 hover:bg-gray-50 transition-colors items-center">
            
            <!-- Contract Details -->
            <div class="col-span-2">
              <div class="flex items-center space-x-3">
                <div class="w-10 h-10 bg-gradient-to-br from-purple-500 to-pink-600 rounded-lg flex items-center justify-center text-white font-bold text-sm shadow-md flex-shrink-0">
                  {{ getContractInitials(contract.contractNumber) }}
                </div>
                <div class="min-w-0">
                  <div class="font-semibold text-gray-900 text-sm truncate">{{ contract.contractNumber }}</div>
                </div>
              </div>
            </div>

            <!-- Sponsor -->
            <div class="col-span-2 min-w-0">
              <div class="font-medium text-gray-900 text-sm truncate">{{ contract.sponsor?.name || 'N/A' }}</div>
              <div class="text-xs text-gray-500 truncate">{{ contract.sponsor?.email || '' }}</div>
            </div>

            <!-- Period -->
            <div class="col-span-2">
              <div class="text-sm text-gray-900">{{ contract.startDate }}</div>
              <div class="text-xs text-gray-500">to {{ contract.endDate }}</div>
            </div>

            <!-- Budget -->
            <div class="col-span-2">
              <div class="font-semibold text-gray-900 text-sm">{{ contract.amount | number:'1.2-2' }} {{ contract.currency }}</div>
              <div *ngIf="budgetSummaries[contract.id!]" class="mt-1">
                <div class="text-xs text-gray-600">
                  Available: {{ budgetSummaries[contract.id!].available | number:'1.2-2' }}
                </div>
                <div class="w-full bg-gray-200 rounded-full h-1.5 mt-1">
                  <div class="h-1.5 rounded-full transition-all"
                       [style.width.%]="getBudgetPercentage(contract.id!)"
                       [ngClass]="{
                         'bg-green-500': getBudgetPercentage(contract.id!) < 60,
                         'bg-yellow-500': getBudgetPercentage(contract.id!) >= 60 && getBudgetPercentage(contract.id!) < 85,
                         'bg-red-500': getBudgetPercentage(contract.id!) >= 85
                       }">
                  </div>
                </div>
              </div>
            </div>

            <!-- Status -->
            <div class="col-span-2">
              <span *ngIf="contract.status === 'ACTIVE'" 
                    class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800 whitespace-nowrap">
                <span class="w-1.5 h-1.5 bg-green-500 rounded-full mr-1.5"></span>
                ACTIVE
              </span>
              <span *ngIf="contract.status === 'DRAFT'" 
                    class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800 whitespace-nowrap">
                <span class="w-1.5 h-1.5 bg-yellow-500 rounded-full mr-1.5"></span>
                DRAFT
              </span>
              <span *ngIf="contract.status === 'EXPIRING_SOON'" 
                    class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-orange-100 text-orange-800 whitespace-nowrap">
                <span class="w-1.5 h-1.5 bg-orange-500 rounded-full mr-1.5 animate-pulse"></span>
                EXPIRING
              </span>
              <span *ngIf="contract.status === 'EXPIRED' || contract.status === 'TERMINATED'" 
                    class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800 whitespace-nowrap">
                <span class="w-1.5 h-1.5 bg-red-500 rounded-full mr-1.5"></span>
                {{ contract.status }}
              </span>
            </div>

            <!-- Type -->
            <div class="col-span-1">
              <span class="text-xs text-gray-700 font-medium">{{ contract.type || 'N/A' }}</span>
            </div>

            <!-- Actions -->
            <div class="col-span-1 flex items-center justify-end gap-1 pr-2">
              <button (click)="viewContract(contract)"
                      class="action-icon view-icon p-1.5 text-green-600 hover:text-white bg-green-50 hover:bg-green-600 rounded-lg transition-all duration-300 transform hover:scale-110 flex-shrink-0"
                      title="View Details">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                </svg>
              </button>
              <a [routerLink]="['/dashboard/contracts/edit', contract.id]"
                 class="action-icon edit-icon p-1.5 text-blue-600 hover:text-white bg-blue-50 hover:bg-blue-600 rounded-lg transition-all duration-300 transform hover:scale-110 hover:rotate-12 flex-shrink-0"
                 title="Edit">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                </svg>
              </a>
              <button (click)="confirmDelete(contract)"
                      class="action-icon delete-icon p-1.5 text-red-600 hover:text-white bg-red-50 hover:bg-red-600 rounded-lg transition-all duration-300 transform hover:scale-110 hover:-rotate-12 flex-shrink-0"
                      title="Delete">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                </svg>
              </button>
            </div>
          </div>

          <!-- Empty State -->
          <div *ngIf="filteredContracts.length === 0" class="px-6 py-12 text-center">
            <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
            </svg>
            <h3 class="mt-2 text-sm font-medium text-gray-900">No contracts found</h3>
            <p class="mt-1 text-sm text-gray-500">Get started by creating a new contract.</p>
          </div>
        </div>

        <!-- Pagination -->
        <div class="px-6 py-4 bg-gray-50 border-t border-gray-200 flex items-center justify-between">
          <div class="text-sm text-gray-700">
            Showing <span class="font-medium">{{ getStartIndex() }}</span> to <span class="font-medium">{{ getEndIndex() }}</span> of <span class="font-medium">{{ filteredContracts.length }}</span> contracts
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

    <!-- View Contract Details Modal -->
    <div *ngIf="showViewModal && selectedContract" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl shadow-2xl max-w-3xl w-full mx-4 animate-scale-in max-h-[90vh] overflow-y-auto">
        <!-- Modal Header -->
        <div class="bg-gradient-to-r from-blue-500 to-blue-600 p-6 rounded-t-2xl">
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-4">
              <div class="w-14 h-14 bg-white bg-opacity-20 rounded-xl flex items-center justify-center">
                <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
                </svg>
              </div>
              <div>
                <h2 class="text-2xl font-bold text-white">Contract Details</h2>
                <p class="text-blue-100 text-sm">{{ selectedContract.contractNumber }}</p>
              </div>
            </div>
            <button (click)="closeViewModal()" 
                    class="text-white hover:bg-white hover:bg-opacity-20 rounded-lg p-2 transition-colors">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>
        </div>

        <!-- Modal Body -->
        <div class="p-6 space-y-6">
          <!-- Contract Information -->
          <div class="bg-gray-50 rounded-xl p-5">
            <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
              <svg class="w-5 h-5 mr-2 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
              Contract Information
            </h3>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Contract Number</label>
                <p class="text-sm font-semibold text-gray-900 mt-1">{{ selectedContract.contractNumber }}</p>
              </div>
              <div>
                <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Type</label>
                <p class="text-sm font-semibold text-gray-900 mt-1">{{ selectedContract.type || 'N/A' }}</p>
              </div>
              <div>
                <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Status</label>
                <div class="mt-1">
                  <span *ngIf="selectedContract.status === 'ACTIVE'" 
                        class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                    <span class="w-1.5 h-1.5 bg-green-500 rounded-full mr-2"></span>
                    ACTIVE
                  </span>
                  <span *ngIf="selectedContract.status === 'DRAFT'" 
                        class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                    <span class="w-1.5 h-1.5 bg-yellow-500 rounded-full mr-2"></span>
                    DRAFT
                  </span>
                  <span *ngIf="selectedContract.status === 'EXPIRING_SOON'" 
                        class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-orange-100 text-orange-800">
                    <span class="w-1.5 h-1.5 bg-orange-500 rounded-full mr-2 animate-pulse"></span>
                    EXPIRING SOON
                  </span>
                  <span *ngIf="selectedContract.status === 'EXPIRED' || selectedContract.status === 'TERMINATED'" 
                        class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800">
                    <span class="w-1.5 h-1.5 bg-red-500 rounded-full mr-2"></span>
                    {{ selectedContract.status }}
                  </span>
                </div>
              </div>
              <div>
                <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Currency</label>
                <p class="text-sm font-semibold text-gray-900 mt-1">{{ selectedContract.currency }}</p>
              </div>
            </div>
          </div>

          <!-- Sponsor Information -->
          <div class="bg-blue-50 rounded-xl p-5">
            <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
              <svg class="w-5 h-5 mr-2 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
              </svg>
              Sponsor Information
            </h3>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Sponsor Name</label>
                <p class="text-sm font-semibold text-gray-900 mt-1">{{ selectedContract.sponsor?.name || 'N/A' }}</p>
              </div>
              <div>
                <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Email</label>
                <p class="text-sm font-semibold text-gray-900 mt-1">{{ selectedContract.sponsor?.email || 'N/A' }}</p>
              </div>
              <div>
                <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Company</label>
                <p class="text-sm font-semibold text-gray-900 mt-1">{{ selectedContract.sponsor?.companyName || 'N/A' }}</p>
              </div>
              <div>
                <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Phone</label>
                <p class="text-sm font-semibold text-gray-900 mt-1">{{ selectedContract.sponsor?.phone || 'N/A' }}</p>
              </div>
            </div>
          </div>

          <!-- Period & Budget -->
          <div class="grid grid-cols-2 gap-6">
            <!-- Period -->
            <div class="bg-purple-50 rounded-xl p-5">
              <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                <svg class="w-5 h-5 mr-2 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>
                </svg>
                Contract Period
              </h3>
              <div class="space-y-3">
                <div>
                  <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Start Date</label>
                  <p class="text-sm font-semibold text-gray-900 mt-1">{{ selectedContract.startDate }}</p>
                </div>
                <div>
                  <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">End Date</label>
                  <p class="text-sm font-semibold text-gray-900 mt-1">{{ selectedContract.endDate }}</p>
                </div>
              </div>
            </div>

            <!-- Budget -->
            <div class="bg-green-50 rounded-xl p-5">
              <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                <svg class="w-5 h-5 mr-2 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
                Budget Details
              </h3>
              <div class="space-y-3">
                <div>
                  <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Total Amount</label>
                  <p class="text-xl font-bold text-gray-900 mt-1">{{ selectedContract.amount | number:'1.2-2' }} {{ selectedContract.currency }}</p>
                </div>
                <div *ngIf="budgetSummaries[selectedContract.id!]">
                  <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Available</label>
                  <p class="text-sm font-semibold text-green-600 mt-1">{{ budgetSummaries[selectedContract.id!].available | number:'1.2-2' }} {{ selectedContract.currency }}</p>
                  <div class="w-full bg-gray-200 rounded-full h-2 mt-2">
                    <div class="h-2 rounded-full transition-all"
                         [style.width.%]="getBudgetPercentage(selectedContract.id!)"
                         [ngClass]="{
                           'bg-green-500': getBudgetPercentage(selectedContract.id!) < 60,
                           'bg-yellow-500': getBudgetPercentage(selectedContract.id!) >= 60 && getBudgetPercentage(selectedContract.id!) < 85,
                           'bg-red-500': getBudgetPercentage(selectedContract.id!) >= 85
                         }">
                    </div>
                  </div>
                  <p class="text-xs text-gray-500 mt-1">{{ getBudgetPercentage(selectedContract.id!) | number:'1.0-0' }}% used</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Description -->
          <div *ngIf="selectedContract.description" class="bg-gray-50 rounded-xl p-5">
            <h3 class="text-lg font-semibold text-gray-900 mb-3 flex items-center">
              <svg class="w-5 h-5 mr-2 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h7"/>
              </svg>
              Description
            </h3>
            <p class="text-sm text-gray-700 leading-relaxed">{{ selectedContract.description }}</p>
          </div>
        </div>

        <!-- Modal Footer -->
        <div class="bg-gray-50 px-6 py-4 rounded-b-2xl flex justify-end space-x-3">
          <button (click)="closeViewModal()" 
                  class="px-6 py-2 bg-white hover:bg-gray-100 text-gray-700 font-medium rounded-lg border border-gray-300 transition-colors">
            Close
          </button>
          <a [routerLink]="['/dashboard/contracts/edit', selectedContract.id]"
             (click)="closeViewModal()"
             class="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors">
            Edit Contract
          </a>
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
        <h3 class="text-lg font-semibold text-gray-900 text-center mb-2">Delete Contract</h3>
        <p class="text-sm text-gray-600 text-center mb-6">
          Are you sure you want to delete contract <span class="font-semibold">{{ contractToDelete?.contractNumber }}</span>? This action cannot be undone.
        </p>
        <div class="flex space-x-3">
          <button (click)="showDeleteModal = false" 
                  class="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors font-medium">
            Cancel
          </button>
          <button (click)="deleteContract()" 
                  class="flex-1 px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors font-medium">
            Delete
          </button>
        </div>
      </div>
    </div>
  `
})
export class ContractListComponent implements OnInit {
  contracts: Contract[] = [];
  filteredContracts: Contract[] = [];
  paginatedContracts: Contract[] = [];
  budgetSummaries: { [contractId: number]: BudgetSummary } = {};
  
  activeTab: 'all' | 'active' | 'expiring' | 'expired' = 'all';
  searchTerm: string = '';
  
  currentPage: number = 1;
  itemsPerPage: number = 4;
  
  showDeleteModal: boolean = false;
  contractToDelete: Contract | null = null;
  
  showViewModal: boolean = false;
  selectedContract: Contract | null = null;

  constructor(
    private contractService: ContractService,
    private budgetService: BudgetService
  ) {}

  ngOnInit(): void {
    this.loadContracts();
  }

  loadContracts(): void {
    this.contractService.getAllContracts().subscribe({
      next: (data) => {
        this.contracts = data;
        this.contracts.forEach(contract => {
          if (contract.id) {
            this.loadBudgetSummary(contract.id);
          }
        });
        this.filterContracts();
      },
      error: (err) => { /* Error loading contracts */ }
    });
  }

  loadBudgetSummary(contractId: number): void {
    this.budgetService.getContractBudgetSummary(contractId).subscribe({
      next: (summary) => {
        this.budgetSummaries[contractId] = summary;
      },
      error: (err) => { /* Error loading budget summary */ }
    });
  }

  filterContracts(): void {
    let filtered = [...this.contracts];

    // Filter by tab
    if (this.activeTab === 'active') {
      filtered = filtered.filter(c => c.status === 'ACTIVE');
    } else if (this.activeTab === 'expiring') {
      filtered = filtered.filter(c => c.status === 'EXPIRING_SOON');
    } else if (this.activeTab === 'expired') {
      filtered = filtered.filter(c => c.status === 'EXPIRED' || c.status === 'TERMINATED');
    }

    // Filter by search term
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(c => 
        c.contractNumber?.toLowerCase().includes(term) ||
        c.sponsor?.name?.toLowerCase().includes(term) ||
        c.sponsor?.email?.toLowerCase().includes(term) ||
        c.type?.toLowerCase().includes(term)
      );
    }

    this.filteredContracts = filtered;
    this.currentPage = 1;
    this.updatePagination();
  }

  updatePagination(): void {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    this.paginatedContracts = this.filteredContracts.slice(start, end);
  }

  getTotalPages(): number {
    return Math.ceil(this.filteredContracts.length / this.itemsPerPage);
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
    return this.filteredContracts.length === 0 ? 0 : (this.currentPage - 1) * this.itemsPerPage + 1;
  }

  getEndIndex(): number {
    const end = this.currentPage * this.itemsPerPage;
    return end > this.filteredContracts.length ? this.filteredContracts.length : end;
  }

  getTotalCount(): number {
    return this.contracts.length;
  }

  getContractInitials(contractNumber: string): string {
    if (!contractNumber) return '??';
    const parts = contractNumber.split('-');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return contractNumber.substring(0, 2).toUpperCase();
  }

  getBudgetPercentage(contractId: number): number {
    const summary = this.budgetSummaries[contractId];
    if (!summary || summary.totalBudget === 0) return 0;
    return ((summary.spent + summary.reserved) / summary.totalBudget) * 100;
  }

  confirmDelete(contract: Contract): void {
    this.contractToDelete = contract;
    this.showDeleteModal = true;
  }

  deleteContract(): void {
    if (this.contractToDelete?.id) {
      this.contractService.deleteContract(this.contractToDelete.id).subscribe({
        next: () => {
          this.showDeleteModal = false;
          this.contractToDelete = null;
          this.loadContracts();
        },
        error: (err) => {
          console.error('Error deleting contract', err);
          this.showDeleteModal = false;
        }
      });
    }
  }

  // Statistics Methods
  getTotalContracts(): number {
    return this.contracts.length;
  }

  getActiveContracts(): number {
    return this.contracts.filter(c => c.status === 'ACTIVE').length;
  }

  getExpiredContracts(): number {
    return this.contracts.filter(c => c.status === 'EXPIRED' || c.status === 'TERMINATED').length;
  }

  getPendingContracts(): number {
    return this.contracts.filter(c => c.status === 'DRAFT' || c.status === 'EXPIRING_SOON').length;
  }

  getPercentageChange(type: string): string {
    // This would normally calculate based on historical data
    // For now, returning mock percentages
    const changes: { [key: string]: string } = {
      'total': '+12.5%',
      'active': '+5.2%',
      'expired': '-2.4%',
      'pending': '+8.1%'
    };
    return changes[type] || '+0%';
  }

  // View Modal Methods
  viewContract(contract: Contract): void {
    this.selectedContract = contract;
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedContract = null;
  }
}
