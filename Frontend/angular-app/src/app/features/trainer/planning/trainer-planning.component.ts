import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { PlanningService, PlanningResponse, PlanningRequest, AvailableEvent } from '../../../core/services/planning.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-trainer-planning',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, PageHeaderComponent],
  templateUrl: './trainer-planning.component.html',
  styleUrl: './trainer-planning.component.scss'
})
export class TrainerPlanningComponent implements OnInit {

  plannings: PlanningResponse[] = [];
  loading = false;
  showModal = false;
  isEditMode = false;
  selectedId: number | null = null;
  planningForm: FormGroup;

  // Publication en masse
  publishingAll = false;

  // Événements disponibles depuis le event-service
  availableEvents: AvailableEvent[] = [];
  loadingEvents = false;
  selectedEvent: AvailableEvent | null = null;

  // Vue calendrier : semaine courante
  currentWeekStart: Date = this.getMonday(new Date());
  weekDays: Date[] = [];

  eventTypes = [
    { value: 'COURSE', label: 'Cours', icon: '📚' },
    { value: 'TRAINING', label: 'Formation', icon: '🎓' },
    { value: 'EXAM', label: 'Examen', icon: '📝' },
    { value: 'EVENT', label: 'Événement', icon: '🎉' },
    { value: 'MEETING', label: 'Réunion', icon: '👥' },
    { value: 'OTHER', label: 'Autre', icon: '📌' }
  ];

  colorOptions = [
    { value: '#6366f1', label: 'Indigo' },
    { value: '#ec4899', label: 'Rose' },
    { value: '#10b981', label: 'Vert' },
    { value: '#f59e0b', label: 'Ambre' },
    { value: '#3b82f6', label: 'Bleu' },
    { value: '#ef4444', label: 'Rouge' },
    { value: '#8b5cf6', label: 'Violet' },
    { value: '#14b8a6', label: 'Teal' }
  ];

  constructor(
    private fb: FormBuilder,
    private planningService: PlanningService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {
    this.planningForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      description: [''],
      date: ['', Validators.required],
      startTime: ['', Validators.required],
      endTime: ['', Validators.required],
      eventType: ['COURSE', Validators.required],
      location: [''],
      color: ['#6366f1', Validators.required]
    });
  }

  ngOnInit(): void {
    this.buildWeekDays();
    this.loadPlannings();
    this.loadAvailableEvents();
  }

  loadPlannings(): void {
    this.loading = true;
    this.planningService.getAll().subscribe({
      next: (data) => {
        this.plannings = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading plannings:', err);
        this.loading = false;
      }
    });
  }

  loadAvailableEvents(): void {
    this.loadingEvents = true;
    this.planningService.getAvailableEvents().subscribe({
      next: (data) => {
        // Garder uniquement les événements publiés ou en cours
        this.availableEvents = data.filter(e =>
          ['PUBLISHED', 'ONGOING', 'DRAFT'].includes(e.status)
        );
        this.loadingEvents = false;
      },
      error: (err) => {
        console.error('Error loading events:', err);
        this.loadingEvents = false;
      }
    });
  }

  /** Appelé quand le trainer sélectionne un événement dans le dropdown */
  onEventSelected(eventId: string): void {
    if (!eventId) {
      this.selectedEvent = null;
      return;
    }
    const ev = this.availableEvents.find(e => e.eventId === +eventId);
    if (!ev) return;
    this.selectedEvent = ev;

    // Pré-remplir le formulaire avec les données de l'événement
    const startDate = ev.startDate.split('T')[0];
    const startTime = ev.startDate.includes('T') ? ev.startDate.split('T')[1].substring(0, 5) : '09:00';
    const endTime   = ev.endDate.includes('T')   ? ev.endDate.split('T')[1].substring(0, 5)   : '10:00';

    this.planningForm.patchValue({
      title:       ev.title,
      description: ev.description,
      date:        startDate,
      startTime:   startTime,
      endTime:     endTime,
      location:    ev.location,
      color:       '#ec4899'   // couleur dédiée aux événements
    });
  }

  /** Retourne le badge de statut d'un événement */
  getEventStatusBadge(status: string): string {
    const map: Record<string, string> = {
      PUBLISHED: '🟢 Publié',
      DRAFT:     '⚪ Brouillon',
      ONGOING:   '🔵 En cours',
      FULL:      '🔴 Complet',
      COMPLETED: '✅ Terminé',
      CANCELLED: '❌ Annulé'
    };
    return map[status] ?? status;
  }

  /** Retourne le mode d'un événement en français */
  getEventModeLabel(mode: string): string {
    const map: Record<string, string> = {
      PHYSICAL: '🏢 Présentiel',
      ONLINE:   '💻 En ligne',
      HYBRID:   '🔀 Hybride'
    };
    return map[mode] ?? mode;
  }

  /** Calcule les places restantes d'un événement */
  getEventRemainingPlaces(ev: AvailableEvent): number {
    const total = (ev.physicalCapacity || 0) + (ev.onlineCapacity || 0);
    const taken = (ev.physicalRegistered || 0) + (ev.onlineRegistered || 0);
    return Math.max(0, total - taken);
  }

  // ─── Calendrier semaine ───────────────────────────────────────────────────

  getMonday(d: Date): Date {
    const date = new Date(d);
    const day = date.getDay();
    const diff = date.getDate() - day + (day === 0 ? -6 : 1);
    date.setDate(diff);
    date.setHours(0, 0, 0, 0);
    return date;
  }

  buildWeekDays(): void {
    this.weekDays = [];
    for (let i = 0; i < 7; i++) {
      const d = new Date(this.currentWeekStart);
      d.setDate(d.getDate() + i);
      this.weekDays.push(d);
    }
  }

  prevWeek(): void {
    this.currentWeekStart.setDate(this.currentWeekStart.getDate() - 7);
    this.currentWeekStart = new Date(this.currentWeekStart);
    this.buildWeekDays();
  }

  nextWeek(): void {
    this.currentWeekStart.setDate(this.currentWeekStart.getDate() + 7);
    this.currentWeekStart = new Date(this.currentWeekStart);
    this.buildWeekDays();
  }

  goToToday(): void {
    this.currentWeekStart = this.getMonday(new Date());
    this.buildWeekDays();
  }

  getPlanningsForDay(day: Date): PlanningResponse[] {
    const dateStr = this.toDateString(day);
    return this.plannings
      .filter(p => p.date === dateStr)
      .sort((a, b) => a.startTime.localeCompare(b.startTime));
  }

  isToday(day: Date): boolean {
    const today = new Date();
    return day.toDateString() === today.toDateString();
  }

  toDateString(d: Date): string {
    return d.toISOString().split('T')[0];
  }

  getWeekLabel(): string {
    const end = new Date(this.currentWeekStart);
    end.setDate(end.getDate() + 6);
    const opts: Intl.DateTimeFormatOptions = { day: 'numeric', month: 'short' };
    return `${this.currentWeekStart.toLocaleDateString('fr-FR', opts)} – ${end.toLocaleDateString('fr-FR', opts)} ${end.getFullYear()}`;
  }

  // ─── CRUD ─────────────────────────────────────────────────────────────────

  openCreateModal(day?: Date): void {
    this.isEditMode = false;
    this.selectedId = null;
    this.selectedEvent = null;
    this.planningForm.reset({
      eventType: 'COURSE',
      color: '#6366f1',
      date: day ? this.toDateString(day) : this.toDateString(new Date())
    });
    this.showModal = true;
  }

  openEditModal(planning: PlanningResponse): void {
    this.isEditMode = true;
    this.selectedId = planning.planningId;
    this.selectedEvent = null;
    this.planningForm.patchValue({
      title: planning.title,
      description: planning.description,
      date: planning.date,
      startTime: planning.startTime?.substring(0, 5),
      endTime: planning.endTime?.substring(0, 5),
      eventType: planning.eventType,
      location: planning.location,
      color: planning.color
    });
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.selectedEvent = null;
    this.planningForm.reset();
  }

  onSubmit(): void {
    if (this.planningForm.invalid) return;
    const v = this.planningForm.value;
    const request: PlanningRequest = {
      ...v,
      startTime: v.startTime + ':00',
      endTime: v.endTime + ':00'
    };

    if (this.isEditMode && this.selectedId !== null) {
      this.planningService.update(this.selectedId, request).subscribe({
        next: () => { this.loadPlannings(); this.closeModal(); },
        error: (err) => {
          const msg = err?.error?.error || err?.error?.message || JSON.stringify(err?.error) || 'Erreur lors de la mise à jour';
          alert(msg);
        }
      });
    } else {
      this.planningService.create(request).subscribe({
        next: () => { this.loadPlannings(); this.closeModal(); },
        error: (err) => {
          const msg = err?.error?.error || err?.error?.message || JSON.stringify(err?.error) || 'Erreur lors de la création';
          alert(msg);
        }
      });
    }
  }

  deletePlanning(id: number): void {
    if (!confirm('Supprimer cette session ?')) return;
    this.planningService.delete(id).subscribe({
      next: () => this.loadPlannings(),
      error: (err) => { console.error(err); alert('Erreur lors de la suppression'); }
    });
  }

  togglePublish(planning: PlanningResponse): void {
    const isPublished = planning.status === 'PUBLISHED';
    const action$ = isPublished
      ? this.planningService.unpublish(planning.planningId)
      : this.planningService.publish(planning.planningId);

    action$.subscribe({
      next: (updated) => {
        const idx = this.plannings.findIndex(p => p.planningId === updated.planningId);
        if (idx !== -1) {
          this.plannings[idx] = updated;
          this.cdr.detectChanges();
        }
      },
      error: (err) => { console.error(err); alert('Erreur lors de la publication'); }
    });
  }

  isPublished(planning: PlanningResponse): boolean {
    return planning.status === 'PUBLISHED';
  }

  // ─── Publication en masse ─────────────────────────────────────────────────

  /** Nombre de sessions de la semaine visible */
  get weekPlannings(): PlanningResponse[] {
    const start = this.toDateString(this.currentWeekStart);
    const end = new Date(this.currentWeekStart);
    end.setDate(end.getDate() + 6);
    const endStr = this.toDateString(end);
    return this.plannings.filter(p => p.date >= start && p.date <= endStr);
  }

  /** Toutes les sessions de la semaine sont-elles publiées ? */
  get allWeekPublished(): boolean {
    const week = this.weekPlannings;
    return week.length > 0 && week.every(p => p.status === 'PUBLISHED');
  }

  /** Y a-t-il au moins une session non publiée cette semaine ? */
  get hasUnpublishedThisWeek(): boolean {
    return this.weekPlannings.some(p => p.status !== 'PUBLISHED');
  }

  /** Nombre de sessions publiées cette semaine */
  getPublishedCount(): number {
    return this.weekPlannings.filter(p => p.status === 'PUBLISHED').length;
  }

  /** Publie toutes les sessions de la semaine visible */
  publishAllWeek(): void {
    const drafts = this.weekPlannings.filter(p => p.status !== 'PUBLISHED');
    if (drafts.length === 0) { alert('Toutes les sessions de cette semaine sont déjà publiées.'); return; }
    if (!confirm(`Publier les ${drafts.length} session(s) en brouillon de cette semaine ?`)) return;
    this.publishingAll = true;
    this.publishAllFallback(drafts);
  }

  /** Dépublie toutes les sessions de la semaine visible */
  unpublishAllWeek(): void {
    const published = this.weekPlannings.filter(p => p.status === 'PUBLISHED');
    if (published.length === 0) { alert('Aucune session publiée cette semaine.'); return; }
    if (!confirm(`Dépublier les ${published.length} session(s) publiée(s) de cette semaine ?`)) return;
    this.publishingAll = true;
    this.unpublishAllFallback(published);
  }

  /** Fallback : publie les sessions une par une */
  private publishAllFallback(drafts: PlanningResponse[]): void {
    let done = 0;
    drafts.forEach(p => {
      this.planningService.publish(p.planningId).subscribe({
        next: (updated) => {
          const idx = this.plannings.findIndex(x => x.planningId === updated.planningId);
          if (idx !== -1) this.plannings[idx] = updated;
          if (++done === drafts.length) {
            this.publishingAll = false;
            this.loadPlannings();
          }
        },
        error: () => {
          if (++done === drafts.length) {
            this.publishingAll = false;
            this.loadPlannings();
          }
        }
      });
    });
  }

  /** Fallback : dépublie les sessions une par une */
  private unpublishAllFallback(published: PlanningResponse[]): void {
    let done = 0;
    published.forEach(p => {
      this.planningService.unpublish(p.planningId).subscribe({
        next: (updated) => {
          const idx = this.plannings.findIndex(x => x.planningId === updated.planningId);
          if (idx !== -1) this.plannings[idx] = updated;
          if (++done === published.length) {
            this.publishingAll = false;
            this.loadPlannings();
          }
        },
        error: () => {
          if (++done === published.length) {
            this.publishingAll = false;
            this.loadPlannings();
          }
        }
      });
    });
  }

  // ─── Helpers ──────────────────────────────────────────────────────────────

  getEventTypeIcon(type: string): string {
    return this.eventTypes.find(e => e.value === type)?.icon || '📌';
  }

  getEventTypeLabel(type: string): string {
    return this.eventTypes.find(e => e.value === type)?.label || type;
  }

  formatTime(time: string): string {
    return time ? time.substring(0, 5) : '';
  }

  getPlanningStats() {
    const today = this.toDateString(new Date());
    const upcoming = this.plannings.filter(p => p.date >= today).length;
    const thisMonth = new Date().toISOString().substring(0, 7);
    const monthly = this.plannings.filter(p => p.date?.startsWith(thisMonth)).length;
    return [
      { label: 'Sessions à venir', value: upcoming },
      { label: 'Sessions ce mois', value: monthly }
    ];
  }

  getDayName(day: Date): string {
    return day.toLocaleDateString('fr-FR', { weekday: 'short' });
  }
}
