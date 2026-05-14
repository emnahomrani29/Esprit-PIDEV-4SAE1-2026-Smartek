import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SkillEvidenceService } from '../../../core/services/skill-evidence.service';
import { SkillEvidenceResponse } from '../../../core/models/skill-evidence.model';

@Component({
  selector: 'app-skill-evidence-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './skill-evidence-management.component.html',
  styleUrl: './skill-evidence-management.component.scss'
})
export class SkillEvidenceManagementComponent implements OnInit {
  evidences: SkillEvidenceResponse[] = [];
  filteredEvidences: SkillEvidenceResponse[] = [];
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  // Filters
  searchTerm = '';
  selectedStatus = '';
  selectedCategory = '';

  // Validation modal
  showValidationModal = false;
  selectedEvidence: SkillEvidenceResponse | null = null;
  modalDefaultAction: string = 'approve';
  reviewScore = 80;
  reviewComment = '';
  modalError = '';

  constructor(private skillEvidenceService: SkillEvidenceService) {}

  ngOnInit(): void {
    this.loadEvidences();
  }

  loadEvidences(): void {
    this.isLoading = true;
    this.skillEvidenceService.getAll().subscribe({
      next: (data) => {
        this.evidences = data;
        this.filteredEvidences = [...data];
        this.isLoading = false;
      },
      error: () => { this.errorMessage = 'Erreur lors du chargement'; this.isLoading = false; }
    });
  }

  // ---- Filters ----
  filterEvidences(): void {
    this.filteredEvidences = this.evidences.filter(e => {
      const matchSearch = !this.searchTerm ||
        e.learnerName?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        e.title?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        e.description?.toLowerCase().includes(this.searchTerm.toLowerCase());

      const matchStatus = !this.selectedStatus || e.status === this.selectedStatus;
      const matchCategory = !this.selectedCategory || e.category === this.selectedCategory;

      return matchSearch && matchStatus && matchCategory;
    });
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedStatus = '';
    this.selectedCategory = '';
    this.filteredEvidences = [...this.evidences];
  }

  // ---- Stats ----
  getTotalEvidences(): number { return this.evidences.length; }

  getUniqueLearners(): number {
    return new Set(this.evidences.map(e => e.learnerId)).size;
  }

  // ---- Validation Modal ----
  openValidationModal(ev: SkillEvidenceResponse, action: string): void {
    this.selectedEvidence = ev;
    this.modalDefaultAction = action;
    this.reviewScore = 80;
    this.reviewComment = '';
    this.modalError = '';
    this.showValidationModal = true;
  }

  closeValidationModal(): void {
    this.showValidationModal = false;
    this.selectedEvidence = null;
    this.modalError = '';
  }

  submitValidation(): void {
    if (!this.selectedEvidence) return;

    if (this.modalDefaultAction === 'approve') {
      if (this.reviewScore < 0 || this.reviewScore > 100) {
        this.modalError = 'Le score doit être entre 0 et 100';
        return;
      }
      this.isLoading = true;
      this.skillEvidenceService.approve(this.selectedEvidence.evidenceId, {
        score: this.reviewScore,
        adminComment: this.reviewComment || undefined
      }).subscribe({
        next: () => {
          this.successMessage = 'Preuve approuvée avec succès';
          this.closeValidationModal();
          this.loadEvidences();
        },
        error: (err) => {
          this.modalError = err.error?.message || 'Erreur lors de l\'approbation';
          this.isLoading = false;
        }
      });
    } else {
      if (!this.reviewComment.trim()) {
        this.modalError = 'Un commentaire est requis pour le rejet';
        return;
      }
      this.isLoading = true;
      this.skillEvidenceService.reject(this.selectedEvidence.evidenceId, {
        adminComment: this.reviewComment
      }).subscribe({
        next: () => {
          this.successMessage = 'Preuve rejetée';
          this.closeValidationModal();
          this.loadEvidences();
        },
        error: (err) => {
          this.modalError = err.error?.message || 'Erreur lors du rejet';
          this.isLoading = false;
        }
      });
    }
  }

  submitApprove(): void {
    if (!this.selectedEvidence) return;
    if (this.reviewScore < 0 || this.reviewScore > 100) {
      this.modalError = 'Le score doit être entre 0 et 100';
      return;
    }
    this.isLoading = true;
    this.modalError = '';
    this.skillEvidenceService.approve(this.selectedEvidence.evidenceId, {
      score: this.reviewScore,
      adminComment: this.reviewComment || undefined
    }).subscribe({
      next: () => {
        this.successMessage = 'Preuve approuvée avec succès';
        this.closeValidationModal();
        this.loadEvidences();
      },
      error: (err) => {
        this.modalError = err.error?.message || 'Erreur lors de l\'approbation';
        this.isLoading = false;
      }
    });
  }

  submitReject(): void {
    if (!this.selectedEvidence) return;
    if (!this.reviewComment.trim()) {
      this.modalError = 'Un commentaire est requis pour le rejet';
      return;
    }
    this.isLoading = true;
    this.modalError = '';
    this.skillEvidenceService.reject(this.selectedEvidence.evidenceId, {
      adminComment: this.reviewComment
    }).subscribe({
      next: () => {
        this.successMessage = 'Preuve rejetée';
        this.closeValidationModal();
        this.loadEvidences();
      },
      error: (err) => {
        this.modalError = err.error?.message || 'Erreur lors du rejet';
        this.isLoading = false;
      }
    });
  }

  // ---- Helpers ----
  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'APPROVED': return 'badge-success';
      case 'REJECTED': return 'badge-danger';
      default:         return 'badge-warning';
    }
  }

  getCategoryLabel(cat?: string): string {
    const map: Record<string, string> = {
      PROGRAMMING: 'Programming', DESIGN: 'Design',
      MANAGEMENT: 'Management', COMMUNICATION: 'Communication', OTHER: 'Other'
    };
    return cat ? (map[cat] || cat) : '—';
  }

  formatDate(date: string): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
