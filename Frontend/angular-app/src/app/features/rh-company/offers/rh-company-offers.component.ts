import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';
import { environment } from '../../../../environments/environment';
import { map } from 'rxjs';

@Component({
  selector: 'app-rh-company-offers',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './rh-company-offers.component.html'
})
export class RhCompanyOffersComponent implements OnInit {
  // Tabs
  activeTab: 'offers' | 'applications' | 'interviews' | 'stats' = 'offers';

  // Auth
  companyId = 0;
  userId = 0;

  // Offers
  offers: any[] = [];
  filteredOffers: any[] = [];
  filterStatus = 'ALL';
  showOfferModal = false;
  isEditMode = false;
  selectedOffer: any = null;
  offerForm: any = this.emptyOfferForm();

  // Applications
  applications: any[] = [];
  selectedOfferForApps: any = null;
  showApplicationsModal = false;

  // Interviews
  interviews: any[] = [];
  showInterviewModal = false;
  selectedApplication: any = null;
  interviewForm: any = this.emptyInterviewForm();

  // Feedback
  showFeedbackModal = false;
  selectedInterview: any = null;
  feedbackForm: any = this.emptyFeedbackForm();

  // Stats
  stats: any = null;

  loading = false;
  error = '';

  contractTypes = ['CDI', 'CDD', 'Stage', 'Alternance', 'Freelance'];
  statusTypes = ['ACTIVE', 'CLOSED', 'DRAFT'];
  domains = ['Informatique', 'Finance', 'Marketing', 'RH', 'Commercial', 'Ingénierie', 'Santé', 'Éducation', 'Autre'];
  experienceLevels = ['JUNIOR', 'MID', 'SENIOR'];

  constructor(private http: HttpClient, private authService: AuthService) {}

  ngOnInit() {
    const user = this.authService.getUserInfo();
    this.companyId = user?.userId || 0;
    this.userId = user?.userId || 0;
    this.loadOffers();
    this.loadCompanyInterviews();
  }

  // ─── OFFERS ───────────────────────────────────────────────────────────────

  loadOffers() {
    this.loading = true;
    this.http.get<any>(`${environment.apiUrl}/offers/company/${this.companyId}`).pipe(
      map(res => Array.isArray(res) ? res : (res?.content ?? []))
    ).subscribe({
      next: data => { this.offers = data; this.applyFilter(); this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  applyFilter() {
    this.filteredOffers = this.filterStatus === 'ALL'
      ? this.offers
      : this.offers.filter(o => o.status === this.filterStatus);
  }

  emptyOfferForm() {
    return {
      title: '', description: '', companyName: '', location: '',
      contractType: 'CDI', salary: '', salaryMin: null, salaryMax: null,
      domain: '', experienceLevel: 'MID', remote: false, positions: 1,
      status: 'ACTIVE', expiresAt: null
    };
  }

  openCreateOffer() {
    this.isEditMode = false;
    this.offerForm = this.emptyOfferForm();
    this.showOfferModal = true;
  }

  openEditOffer(offer: any) {
    this.isEditMode = true;
    this.selectedOffer = offer;
    this.offerForm = {
      title: offer.title, description: offer.description, companyName: offer.companyName,
      location: offer.location, contractType: offer.contractType, salary: offer.salary || '',
      salaryMin: offer.salaryMin, salaryMax: offer.salaryMax, domain: offer.domain || '',
      experienceLevel: offer.experienceLevel || 'MID', remote: offer.remote || false,
      positions: offer.positions || 1, status: offer.status,
      expiresAt: offer.expiresAt ? offer.expiresAt.substring(0, 16) : null
    };
    this.showOfferModal = true;
  }

  saveOffer() {
    // Validation des champs obligatoires
    if (!this.offerForm.title?.trim()) { this.error = 'Le titre est obligatoire'; return; }
    if (!this.offerForm.description?.trim()) { this.error = 'La description est obligatoire'; return; }
    if (!this.offerForm.companyName?.trim()) { this.error = 'Le nom de l\'entreprise est obligatoire'; return; }
    if (!this.offerForm.location?.trim()) { this.error = 'La localisation est obligatoire'; return; }
    if (!this.offerForm.contractType?.trim()) { this.error = 'Le type de contrat est obligatoire'; return; }

    this.error = '';
    const payload = {
      ...this.offerForm,
      companyId: this.companyId,
      expiresAt: this.offerForm.expiresAt ? new Date(this.offerForm.expiresAt).toISOString() : null
    };
    const req = this.isEditMode
      ? this.http.put<any>(`${environment.apiUrl}/offers/${this.selectedOffer.id}`, payload)
      : this.http.post<any>(`${environment.apiUrl}/offers`, payload);
    req.subscribe({
      next: () => { this.showOfferModal = false; this.loadOffers(); },
      error: (err) => {
        const msg = err?.error?.errors ? Object.values(err.error.errors).join(', ') : 'Erreur lors de la sauvegarde';
        this.error = msg;
      }
    });
  }

  duplicateOffer(offer: any) {
    const payload = {
      title: offer.title + ' (copie)', description: offer.description,
      companyName: offer.companyName, location: offer.location,
      contractType: offer.contractType, salary: offer.salary,
      salaryMin: offer.salaryMin, salaryMax: offer.salaryMax,
      domain: offer.domain, experienceLevel: offer.experienceLevel,
      remote: offer.remote, positions: offer.positions,
      companyId: this.companyId, status: 'DRAFT'
    };
    this.http.post<any>(`${environment.apiUrl}/offers`, payload).subscribe({
      next: () => this.loadOffers(),
      error: () => { this.error = 'Erreur lors de la duplication'; }
    });
  }

  toggleOfferStatus(offer: any) {
    const newStatus = offer.status === 'ACTIVE' ? 'CLOSED' : 'ACTIVE';
    this.http.put<any>(`${environment.apiUrl}/offers/${offer.id}`, { ...offer, status: newStatus, companyId: this.companyId }).subscribe({
      next: () => this.loadOffers()
    });
  }

  deleteOffer(id: number) {
    if (!confirm('Supprimer cette offre ?')) return;
    this.http.delete(`${environment.apiUrl}/offers/${id}`).subscribe({ next: () => this.loadOffers() });
  }

  // ─── APPLICATIONS ─────────────────────────────────────────────────────────

  viewApplications(offer: any) {
    this.selectedOfferForApps = offer;
    this.http.get<any[]>(`${environment.apiUrl}/applications/offer/${offer.id}`).subscribe({
      next: data => { this.applications = data; this.showApplicationsModal = true; }
    });
  }

  updateAppStatus(appId: number, status: string) {
    this.http.put<any>(`${environment.apiUrl}/applications/${appId}/status?status=${status}`, {}).subscribe({
      next: updated => {
        const i = this.applications.findIndex(a => a.id === appId);
        if (i !== -1) this.applications[i] = updated;
        // Rafraîchir les offres pour mettre à jour les compteurs
        this.loadOffers();
      }
    });
  }

  downloadCV(app: any) {
    if (!app.cvBase64 || !app.cvFileName) return;
    const bytes = atob(app.cvBase64);
    const arr = new Uint8Array(bytes.length);
    for (let i = 0; i < bytes.length; i++) arr[i] = bytes.charCodeAt(i);
    const blob = new Blob([arr], { type: 'application/pdf' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = app.cvFileName;
    link.click();
  }

  openScheduleInterview(app: any) {
    this.selectedApplication = app;
    this.interviewForm = this.emptyInterviewForm();
    this.interviewForm.applicationId = app.id;
    this.showInterviewModal = true;
  }

  // ─── INTERVIEWS ───────────────────────────────────────────────────────────

  emptyInterviewForm() {
    return { applicationId: 0, interviewDate: '', location: 'En ligne', meetingLink: '', notes: '' };
  }

  loadCompanyInterviews() {
    this.http.get<any[]>(`${environment.apiUrl}/interviews/company/${this.companyId}`).subscribe({
      next: data => { this.interviews = data || []; },
      error: () => {
        // Fallback sur tous les entretiens si l'endpoint n'est pas encore disponible
        this.http.get<any[]>(`${environment.apiUrl}/interviews`).subscribe({
          next: data => { this.interviews = data || []; }
        });
      }
    });
  }

  saveInterview() {
    const payload = { ...this.interviewForm, createdBy: this.userId };
    this.http.post<any>(`${environment.apiUrl}/interviews`, payload).subscribe({
      next: () => { this.showInterviewModal = false; this.loadCompanyInterviews(); },
      error: () => { this.error = 'Erreur lors de la planification'; }
    });
  }

  updateInterviewStatus(id: number, status: string) {
    this.http.put<any>(`${environment.apiUrl}/interviews/${id}/status?status=${status}`, {}).subscribe({
      next: updated => {
        const i = this.interviews.findIndex(iv => iv.id === id);
        if (i !== -1) this.interviews[i] = updated;
      }
    });
  }

  deleteInterview(id: number) {
    if (!confirm('Supprimer cet entretien ?')) return;
    this.http.delete(`${environment.apiUrl}/interviews/${id}`).subscribe({ next: () => this.loadCompanyInterviews() });
  }

  // ─── FEEDBACK ─────────────────────────────────────────────────────────────

  emptyFeedbackForm() {
    return { interviewId: 0, applicationId: 0, rating: 3, strengths: '', weaknesses: '', generalComment: '', decision: 'PENDING', submittedBy: this.userId };
  }

  openFeedback(interview: any) {
    this.selectedInterview = interview;
    this.feedbackForm = {
      interviewId: interview.id,
      applicationId: interview.applicationId,
      rating: 3, strengths: '', weaknesses: '', generalComment: '',
      decision: 'PENDING', submittedBy: this.userId
    };
    this.showFeedbackModal = true;
  }

  saveFeedback() {
    this.http.post<any>(`${environment.apiUrl}/interview-feedbacks`, this.feedbackForm).subscribe({
      next: () => {
        this.showFeedbackModal = false;
        this.loadCompanyInterviews();
        // Marquer l'entretien comme terminé automatiquement
        this.updateInterviewStatus(this.feedbackForm.interviewId, 'COMPLETED');
      },
      error: () => { this.error = 'Erreur lors de la soumission du feedback'; }
    });
  }

  // ─── STATS ────────────────────────────────────────────────────────────────

  loadStats() {
    this.http.get<any>(`${environment.apiUrl}/offers/stats/company/${this.companyId}`).subscribe({
      next: data => { this.stats = data; }
    });
  }

  onTabChange(tab: 'offers' | 'applications' | 'interviews' | 'stats') {
    this.activeTab = tab;
    if (tab === 'stats' && !this.stats) this.loadStats();
  }

  // ─── HELPERS ──────────────────────────────────────────────────────────────

  statusColor(status: string): string {
    const map: any = {
      ACTIVE: 'bg-green-100 text-green-700', CLOSED: 'bg-red-100 text-red-700',
      DRAFT: 'bg-gray-100 text-gray-600', EXPIRED: 'bg-orange-100 text-orange-700',
      PENDING: 'bg-yellow-100 text-yellow-700', ACCEPTED: 'bg-green-100 text-green-700',
      REJECTED: 'bg-red-100 text-red-700', SCHEDULED: 'bg-blue-100 text-blue-700',
      COMPLETED: 'bg-green-100 text-green-700', CANCELLED: 'bg-red-100 text-red-700',
      RESCHEDULED: 'bg-purple-100 text-purple-700'
    };
    return map[status] || 'bg-gray-100 text-gray-600';
  }

  get interviewsByStatus() {
    return {
      scheduled: this.interviews.filter(i => i.status === 'SCHEDULED').length,
      completed: this.interviews.filter(i => i.status === 'COMPLETED').length,
      cancelled: this.interviews.filter(i => i.status === 'CANCELLED').length,
    };
  }
}
