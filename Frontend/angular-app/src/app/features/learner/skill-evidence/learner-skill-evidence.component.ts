import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SkillEvidenceService } from '../../../core/services/skill-evidence.service';
import { AuthService } from '../../../core/services/auth.service';
import { SkillEvidenceResponse, EvidenceCategory } from '../../../core/models/skill-evidence.model';

@Component({
  selector: 'app-learner-skill-evidence',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './learner-skill-evidence.component.html',
  styleUrl: './learner-skill-evidence.component.scss'
})
export class LearnerSkillEvidenceComponent implements OnInit {
  evidences: SkillEvidenceResponse[] = [];
  loading = false;
  showModal = false;
  showDetailModal = false;
  isEditMode = false;
  selectedEvidence: SkillEvidenceResponse | null = null;
  successMessage = '';
  errorMessage = '';

  currentEvidence: {
    evidenceId?: number;
    title: string;
    fileUrl: string;
    description: string;
    category: EvidenceCategory | '';
  } = { title: '', fileUrl: '', description: '', category: '' };

  private currentUser: any = null;

  constructor(
    private skillEvidenceService: SkillEvidenceService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUserInfo();
    this.loadEvidences();
  }

  loadEvidences(): void {
    if (!this.currentUser?.userId) return;
    this.loading = true;
    this.skillEvidenceService.getByLearner(this.currentUser.userId).subscribe({
      next: (data) => { this.evidences = data; this.loading = false; },
      error: () => { this.errorMessage = 'Erreur lors du chargement'; this.loading = false; }
    });
  }

  // ---- Stats ----
  getPendingCount(): number  { return this.evidences.filter(e => e.status === 'PENDING').length; }
  getApprovedCount(): number { return this.evidences.filter(e => e.status === 'APPROVED').length; }
  getRejectedCount(): number { return this.evidences.filter(e => e.status === 'REJECTED').length; }

  // ---- Add/Edit Modal ----
  openAddModal(): void {
    this.isEditMode = false;
    this.currentEvidence = { title: '', fileUrl: '', description: '', category: '' };
    this.successMessage = '';
    this.errorMessage = '';
    this.showModal = true;
  }

  openEditModal(ev: SkillEvidenceResponse): void {
    this.isEditMode = true;
    this.currentEvidence = {
      evidenceId: ev.evidenceId,
      title: ev.title,
      fileUrl: ev.fileUrl || '',
      description: ev.description || '',
      category: ev.category || ''
    };
    this.successMessage = '';
    this.errorMessage = '';
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.successMessage = '';
    this.errorMessage = '';
  }

  // ---- Detail Modal ----
  openDetailModal(ev: SkillEvidenceResponse): void {
    this.selectedEvidence = ev;
    this.showDetailModal = true;
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.selectedEvidence = null;
  }

  // ---- Save ----
  saveEvidence(): void {
    if (!this.currentEvidence.title.trim()) { this.errorMessage = 'Le titre est obligatoire'; return; }
    if (!this.currentEvidence.category)     { this.errorMessage = 'La catégorie est obligatoire'; return; }

    this.loading = true;
    this.errorMessage = '';

    const payload = {
      title: this.currentEvidence.title,
      fileUrl: this.currentEvidence.fileUrl || undefined,
      description: this.currentEvidence.description || undefined,
      learnerId: this.currentUser.userId,
      learnerName: this.currentUser.firstName,
      learnerEmail: this.currentUser.email,
      category: this.currentEvidence.category as EvidenceCategory
    };

    const obs = this.isEditMode && this.currentEvidence.evidenceId
      ? this.skillEvidenceService.update(this.currentEvidence.evidenceId, payload)
      : this.skillEvidenceService.create(payload);

    obs.subscribe({
      next: () => {
        this.successMessage = this.isEditMode ? 'Preuve modifiée avec succès' : 'Preuve ajoutée avec succès';
        this.loading = false;
        this.showModal = false;
        this.loadEvidences();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors de la sauvegarde';
        this.loading = false;
      }
    });
  }

  // ---- Delete ----
  deleteEvidence(id: number): void {
    if (!confirm('Supprimer cette preuve de compétence ?')) return;
    this.skillEvidenceService.delete(id).subscribe({
      next: () => { this.successMessage = 'Preuve supprimée'; this.loadEvidences(); },
      error: () => { this.errorMessage = 'Erreur lors de la suppression'; }
    });
  }

  // ---- Helpers ----
  getStatusColor(status: string): string {
    switch (status) {
      case 'APPROVED': return 'bg-green-100 text-green-800';
      case 'REJECTED': return 'bg-red-100 text-red-800';
      default:         return 'bg-yellow-100 text-yellow-800';
    }
  }

  getStatusTextClass(status: string): string {
    switch (status) {
      case 'APPROVED': return 'text-green-600';
      case 'REJECTED': return 'text-red-600';
      default:         return 'text-yellow-600';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'APPROVED': return 'Approuvé';
      case 'REJECTED': return 'Rejeté';
      default:         return 'En attente';
    }
  }

  getCategoryLabel(cat?: string): string {
    const map: Record<string, string> = {
      PROGRAMMING: 'Programming', DESIGN: 'Design',
      MANAGEMENT: 'Management', COMMUNICATION: 'Communication', OTHER: 'Other'
    };
    return cat ? (map[cat] || cat) : 'Other';
  }

  shouldShowScore(ev: SkillEvidenceResponse): boolean {
    return ev.status === 'APPROVED' && ev.score !== null && ev.score !== undefined;
  }

  formatDate(date: string): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString('fr-FR', { day: '2-digit', month: 'long', year: 'numeric' });
  }

  // backward compat (used in old HTML)
  getStatusBadgeClass(status: string): string { return this.getStatusColor(status); }
}
