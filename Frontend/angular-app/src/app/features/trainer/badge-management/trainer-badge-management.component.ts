import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { BadgeService } from '../../../core/services/badge.service';
import { CertificationService } from '../../../core/services/certification.service';
import { SkillEvidenceService } from '../../../core/services/skill-evidence.service';
import { AuthService } from '../../../core/services/auth.service';
import { BadgeTemplate, EarnedBadge } from '../../../core/models/badge.model';
import { CertificationTemplate, EarnedCertification } from '../../../core/models/certification.model';
import { SkillEvidenceResponse } from '../../../core/models/skill-evidence.model';

@Component({
  selector: 'app-trainer-badge-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './trainer-badge-management.component.html',
  styleUrl: './trainer-badge-management.component.scss'
})
export class TrainerBadgeManagementComponent implements OnInit {

  activeTab: 'badges' | 'certifications' | 'evidence' = 'badges';
  trainerId = 0;

  // ── BADGES ──────────────────────────────────────────────────────────────
  badgeTemplates: BadgeTemplate[] = [];
  earnedBadges: EarnedBadge[] = [];
  loadingBadges = false;
  showBadgeForm = false;
  editingBadge: BadgeTemplate | null = null;
  showAwardBadgeModal = false;
  selectedBadgeTemplate: BadgeTemplate | null = null;
  awardLearnerId = '';
  badgeForm: FormGroup;

  // ── CERTIFICATIONS ───────────────────────────────────────────────────────
  certTemplates: CertificationTemplate[] = [];
  earnedCerts: EarnedCertification[] = [];
  loadingCerts = false;
  showCertForm = false;
  editingCert: CertificationTemplate | null = null;
  showAwardCertModal = false;
  selectedCertTemplate: CertificationTemplate | null = null;
  awardCertLearnerId = '';
  certForm: FormGroup;

  // ── SKILL EVIDENCE ───────────────────────────────────────────────────────
  evidences: SkillEvidenceResponse[] = [];
  filteredEvidences: SkillEvidenceResponse[] = [];
  loadingEvidence = false;
  evidenceFilter: 'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED' = 'ALL';
  showReviewModal = false;
  selectedEvidence: SkillEvidenceResponse | null = null;
  reviewScore = 0;
  reviewComment = '';
  reviewAction: 'approve' | 'reject' = 'approve';

  // ── GENERAL ──────────────────────────────────────────────────────────────
  successMsg = '';
  errorMsg = '';

  constructor(
    private badgeService: BadgeService,
    private certService: CertificationService,
    private evidenceService: SkillEvidenceService,
    private authService: AuthService,
    private fb: FormBuilder
  ) {
    this.badgeForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: ['', Validators.required],
      minimumScore: [null],
      examId: [null]
    });
    this.certForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(2)]],
      description: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    const user = this.authService.getUserInfo();
    this.trainerId = user?.userId || 0;
    this.loadBadges();
    this.loadCertifications();
    this.loadEvidence();
  }

  // ── TABS ─────────────────────────────────────────────────────────────────
  setTab(tab: 'badges' | 'certifications' | 'evidence') {
    this.activeTab = tab;
    this.clearMessages();
  }

  clearMessages() { this.successMsg = ''; this.errorMsg = ''; }

  // ── BADGES ───────────────────────────────────────────────────────────────
  loadBadges() {
    this.loadingBadges = true;
    this.badgeService.getAllTemplates().subscribe({
      next: t => { this.badgeTemplates = t; this.loadingBadges = false; },
      error: () => { this.errorMsg = 'Erreur chargement badges'; this.loadingBadges = false; }
    });
    this.badgeService.getAllEarnedBadges().subscribe({
      next: b => this.earnedBadges = b,
      error: () => {}
    });
  }

  openBadgeForm(badge?: BadgeTemplate) {
    this.editingBadge = badge || null;
    this.badgeForm.reset();
    if (badge) this.badgeForm.patchValue(badge);
    this.showBadgeForm = true;
  }

  saveBadge() {
    if (this.badgeForm.invalid) return;
    const data: BadgeTemplate = this.badgeForm.value;
    const req = this.editingBadge
      ? this.badgeService.updateTemplate(this.editingBadge.id!, data)
      : this.badgeService.createTemplate(data);
    req.subscribe({
      next: () => { this.successMsg = 'Badge sauvegardé !'; this.showBadgeForm = false; this.loadBadges(); },
      error: () => this.errorMsg = 'Erreur sauvegarde badge'
    });
  }

  deleteBadge(id: number) {
    if (!confirm('Supprimer ce template de badge ?')) return;
    this.badgeService.deleteTemplate(id).subscribe({
      next: () => { this.successMsg = 'Badge supprimé'; this.loadBadges(); },
      error: () => this.errorMsg = 'Erreur suppression'
    });
  }

  openAwardBadge(template: BadgeTemplate) {
    this.selectedBadgeTemplate = template;
    this.awardLearnerId = '';
    this.showAwardBadgeModal = true;
  }

  awardBadge() {
    if (!this.selectedBadgeTemplate || !this.awardLearnerId) return;
    this.badgeService.awardBadge({ badgeTemplateId: this.selectedBadgeTemplate.id!, learnerId: +this.awardLearnerId }).subscribe({
      next: () => { this.successMsg = 'Badge attribué !'; this.showAwardBadgeModal = false; this.loadBadges(); },
      error: (e) => this.errorMsg = e?.error?.message || 'Erreur attribution badge'
    });
  }

  revokeBadge(id: number) {
    if (!confirm('Révoquer ce badge ?')) return;
    this.badgeService.revokeBadge(id).subscribe({
      next: () => { this.successMsg = 'Badge révoqué'; this.loadBadges(); },
      error: () => this.errorMsg = 'Erreur révocation'
    });
  }

  // ── CERTIFICATIONS ───────────────────────────────────────────────────────
  loadCertifications() {
    this.loadingCerts = true;
    this.certService.getAllTemplates().subscribe({
      next: t => { this.certTemplates = t; this.loadingCerts = false; },
      error: () => { this.errorMsg = 'Erreur chargement certifications'; this.loadingCerts = false; }
    });
    this.certService.getAllEarnedCertifications().subscribe({
      next: c => this.earnedCerts = c,
      error: () => {}
    });
  }

  openCertForm(cert?: CertificationTemplate) {
    this.editingCert = cert || null;
    this.certForm.reset();
    if (cert) this.certForm.patchValue(cert);
    this.showCertForm = true;
  }

  saveCert() {
    if (this.certForm.invalid) return;
    const data: CertificationTemplate = this.certForm.value;
    const req = this.editingCert
      ? this.certService.updateTemplate(this.editingCert.id!, data)
      : this.certService.createTemplate(data);
    req.subscribe({
      next: () => { this.successMsg = 'Certification sauvegardée !'; this.showCertForm = false; this.loadCertifications(); },
      error: () => this.errorMsg = 'Erreur sauvegarde certification'
    });
  }

  deleteCert(id: number) {
    if (!confirm('Supprimer ce template de certification ?')) return;
    this.certService.deleteTemplate(id).subscribe({
      next: () => { this.successMsg = 'Certification supprimée'; this.loadCertifications(); },
      error: () => this.errorMsg = 'Erreur suppression'
    });
  }

  openAwardCert(template: CertificationTemplate) {
    this.selectedCertTemplate = template;
    this.awardCertLearnerId = '';
    this.showAwardCertModal = true;
  }

  awardCert() {
    if (!this.selectedCertTemplate || !this.awardCertLearnerId) return;
    const today = new Date();
    const issueDate = `${today.getFullYear()}-${String(today.getMonth()+1).padStart(2,'0')}-${String(today.getDate()).padStart(2,'0')}`;
    this.certService.awardCertification({
      certificationTemplateId: this.selectedCertTemplate.id!,
      learnerId: +this.awardCertLearnerId,
      issueDate
    }).subscribe({
      next: () => { this.successMsg = 'Certification attribuée !'; this.showAwardCertModal = false; this.loadCertifications(); },
      error: (e) => this.errorMsg = e?.error?.message || 'Erreur attribution certification'
    });
  }

  revokeCert(id: number) {
    if (!confirm('Révoquer cette certification ?')) return;
    this.certService.revokeCertification(id).subscribe({
      next: () => { this.successMsg = 'Certification révoquée'; this.loadCertifications(); },
      error: () => this.errorMsg = 'Erreur révocation'
    });
  }

  // ── SKILL EVIDENCE ───────────────────────────────────────────────────────
  loadEvidence() {
    this.loadingEvidence = true;
    this.evidenceService.getAll().subscribe({
      next: e => { this.evidences = e; this.applyEvidenceFilter(); this.loadingEvidence = false; },
      error: () => { this.loadingEvidence = false; }
    });
  }

  applyEvidenceFilter() {
    this.filteredEvidences = this.evidenceFilter === 'ALL'
      ? this.evidences
      : this.evidences.filter(e => e.status === this.evidenceFilter);
  }

  openReview(evidence: SkillEvidenceResponse, action: 'approve' | 'reject') {
    this.selectedEvidence = evidence;
    this.reviewAction = action;
    this.reviewScore = 0;
    this.reviewComment = '';
    this.showReviewModal = true;
  }

  submitReview() {
    if (!this.selectedEvidence) return;
    const id = this.selectedEvidence.evidenceId;
    if (this.reviewAction === 'approve') {
      this.evidenceService.approve(id, { score: this.reviewScore, adminComment: this.reviewComment, reviewerId: this.trainerId }).subscribe({
        next: () => { this.successMsg = 'Preuve approuvée !'; this.showReviewModal = false; this.loadEvidence(); },
        error: () => this.errorMsg = 'Erreur approbation'
      });
    } else {
      this.evidenceService.reject(id, { adminComment: this.reviewComment, reviewerId: this.trainerId }).subscribe({
        next: () => { this.successMsg = 'Preuve rejetée'; this.showReviewModal = false; this.loadEvidence(); },
        error: () => this.errorMsg = 'Erreur rejet'
      });
    }
  }

  statusColor(status: string): string {
    const m: any = { PENDING: 'bg-yellow-100 text-yellow-700', APPROVED: 'bg-green-100 text-green-700', REJECTED: 'bg-red-100 text-red-700' };
    return m[status] || 'bg-gray-100 text-gray-600';
  }

  categoryLabel(cat: string): string {
    const m: any = { PROGRAMMING: '💻 Programming', DESIGN: '🎨 Design', MANAGEMENT: '📋 Management', COMMUNICATION: '💬 Communication', OTHER: '📦 Other' };
    return m[cat] || cat;
  }

  get pendingCount() { return this.evidences.filter(e => e.status === 'PENDING').length; }
  get approvedCount() { return this.evidences.filter(e => e.status === 'APPROVED').length; }
}
