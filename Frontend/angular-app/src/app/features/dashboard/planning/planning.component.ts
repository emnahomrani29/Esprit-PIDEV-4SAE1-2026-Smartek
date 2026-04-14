import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InterviewService } from '../../../core/services/interview.service';
import { AuthService } from '../../../core/services/auth.service';
import { Interview } from '../../../core/models/interview.model';

@Component({
  selector: 'app-planning',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './planning.component.html',
  styleUrls: ['./planning.component.css']
})
export class PlanningComponent implements OnInit {
  interviews: Interview[] = [];
  filteredInterviews: Interview[] = [];
  selectedInterview: Interview | null = null;
  showDetailModal = false;
  
  filterStatus = 'ALL';
  searchTerm = '';
  
  statusTypes = ['ALL', 'SCHEDULED', 'COMPLETED', 'CANCELLED', 'RESCHEDULED'];
  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private interviewService: InterviewService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.loadInterviews();
  }

  loadInterviews() {
    this.loading = true;
    this.errorMessage = '';
    
    console.log('Loading interviews...');
    
    this.interviewService.getAllInterviews().subscribe({
      next: (data) => {
        console.log('Interviews loaded:', data);
        this.interviews = data;
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading interviews:', error);
        this.errorMessage = 'Erreur lors du chargement des entretiens';
        this.loading = false;
      }
    });
  }

  applyFilters() {
    this.filteredInterviews = this.interviews.filter(interview => {
      const statusMatch = this.filterStatus === 'ALL' || interview.status === this.filterStatus;
      const searchMatch = this.searchTerm === '' || 
        interview.learnerName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        interview.learnerEmail.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        interview.location.toLowerCase().includes(this.searchTerm.toLowerCase());
      
      return statusMatch && searchMatch;
    });
    
    // Trier par date (les plus proches en premier)
    this.filteredInterviews.sort((a, b) => {
      return new Date(a.interviewDate).getTime() - new Date(b.interviewDate).getTime();
    });
  }

  onFilterChange() {
    this.applyFilters();
  }

  onSearchChange() {
    this.applyFilters();
  }

  viewDetails(interview: Interview) {
    this.selectedInterview = interview;
    this.showDetailModal = true;
  }

  closeDetailModal() {
    this.showDetailModal = false;
    this.selectedInterview = null;
  }

  updateInterviewStatus(interviewId: number, status: string) {
    this.loading = true;
    this.errorMessage = '';
    
    this.interviewService.updateInterviewStatus(interviewId, status).subscribe({
      next: (updatedInterview) => {
        console.log('Interview status updated:', updatedInterview);
        const index = this.interviews.findIndex(i => i.id === interviewId);
        if (index !== -1) {
          this.interviews[index] = updatedInterview;
        }
        this.applyFilters();
        this.successMessage = 'Statut mis à jour avec succès';
        this.loading = false;
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        console.error('Error updating interview status:', error);
        this.errorMessage = 'Erreur lors de la mise à jour du statut';
        this.loading = false;
      }
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'SCHEDULED': return 'bg-blue-100 text-blue-800';
      case 'COMPLETED': return 'bg-green-100 text-green-800';
      case 'CANCELLED': return 'bg-red-100 text-red-800';
      case 'RESCHEDULED': return 'bg-yellow-100 text-yellow-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'SCHEDULED': return '📅 Planifié';
      case 'COMPLETED': return '✓ Terminé';
      case 'CANCELLED': return '✗ Annulé';
      case 'RESCHEDULED': return '↻ Reporté';
      default: return status;
    }
  }

  isUpcoming(interviewDate: string): boolean {
    return new Date(interviewDate) > new Date();
  }

  isPast(interviewDate: string): boolean {
    return new Date(interviewDate) < new Date();
  }
}
