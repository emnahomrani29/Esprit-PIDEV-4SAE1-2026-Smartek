import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { EventService, EventResponse, EventRevenueResponse } from '../../../core/services/event.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-trainer-events',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, PageHeaderComponent],
  templateUrl: './trainer-events.component.html',
  styleUrl: './trainer-events.component.scss'
})
export class TrainerEventsComponent implements OnInit {

  events: EventResponse[] = [];
  filteredEvents: EventResponse[] = [];
  showModal = false;
  isEditMode = false;
  selectedEventId: number | null = null;
  searchTerm = '';
  filterStatus = 'ALL';
  filterMode = 'ALL';
  loading = false;
  currentUserId: number | null = null;
  eventForm: FormGroup;

  // Map eventId → revenue
  revenueMap: Map<number, EventRevenueResponse> = new Map();

  eventModes = [
    { value: 'PHYSICAL', label: 'Présentiel', icon: '🏢' },
    { value: 'ONLINE', label: 'En ligne', icon: '💻' },
    { value: 'HYBRID', label: 'Hybride', icon: '🔀' }
  ];

  constructor(
    private fb: FormBuilder,
    private eventService: EventService,
    private authService: AuthService
  ) {
    this.eventForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      description: [''],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      location: ['', Validators.required],
      maxParticipations: [50, [Validators.required, Validators.min(1)]],
      mode: ['PHYSICAL', Validators.required],
      isPaid: [false],
      price: [0]
    });
  }

  ngOnInit(): void {
    const userInfo = this.authService.getUserInfo();
    this.currentUserId = userInfo?.userId || null;
    this.loadEvents();
  }

  loadEvents(): void {
    this.loading = true;
    this.eventService.getAllEvents().subscribe({
      next: (events) => {
        this.events = events;
        this.loading = false;
        this.applyFilters();
        // Charger les revenus pour les events payants
        this.loadRevenueForPaidEvents(events);
      },
      error: (err) => {
        console.error('Error loading events:', err);
        this.loading = false;
      }
    });
  }

  loadRevenueForPaidEvents(events: EventResponse[]): void {
    events.filter(e => e.isPaid).forEach(event => {
      this.eventService.getEventRevenue(event.eventId).subscribe({
        next: (revenue) => {
          this.revenueMap.set(event.eventId, revenue);
        },
        error: (err) => console.error(`Error loading revenue for event ${event.eventId}:`, err)
      });
    });
  }

  getRevenue(eventId: number): EventRevenueResponse | undefined {
    return this.revenueMap.get(eventId);
  }

  getEventsStats() {
    return [
      { label: 'Total', value: this.events.length },
      { label: 'Publiés', value: this.events.filter(e => e.status === 'PUBLISHED').length },
      { label: 'En cours', value: this.events.filter(e => e.status === 'ONGOING').length }
    ];
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.selectedEventId = null;
    this.eventForm.reset({ mode: 'PHYSICAL', maxParticipations: 50, isPaid: false, price: 0 });
    this.showModal = true;
  }

  openEditModal(event: EventResponse): void {
    this.isEditMode = true;
    this.selectedEventId = event.eventId;
    this.eventForm.patchValue({
      title: event.title,
      description: event.description,
      startDate: event.startDate ? event.startDate.substring(0, 16) : '',
      endDate: event.endDate ? event.endDate.substring(0, 16) : '',
      location: event.location,
      maxParticipations: event.maxParticipations,
      mode: event.mode,
      isPaid: event.isPaid,
      price: event.price
    });
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.eventForm.reset();
  }

  onSubmit(): void {
    if (this.eventForm.invalid) return;

    const formValue = this.eventForm.value;
    const request = {
      ...formValue,
      startDate: new Date(formValue.startDate).toISOString().slice(0, 19),
      endDate: new Date(formValue.endDate).toISOString().slice(0, 19),
      createdBy: this.currentUserId
    };

    if (this.isEditMode && this.selectedEventId !== null) {
      this.eventService.updateEvent(this.selectedEventId, request).subscribe({
        next: () => { this.loadEvents(); this.closeModal(); },
        error: (err) => { console.error(err); alert('Erreur lors de la mise à jour'); }
      });
    } else {
      this.eventService.createEvent(request).subscribe({
        next: () => { this.loadEvents(); this.closeModal(); },
        error: (err) => { console.error(err); alert('Erreur lors de la création'); }
      });
    }
  }

  deleteEvent(id: number): void {
    if (!confirm('Supprimer cet événement ?')) return;
    this.eventService.deleteEvent(id).subscribe({
      next: () => this.loadEvents(),
      error: (err) => { console.error(err); alert('Erreur lors de la suppression'); }
    });
  }

  changeStatus(event: EventResponse, newStatus: string): void {
    if (!this.currentUserId) return;
    this.eventService.changeStatus(event.eventId, newStatus, this.currentUserId).subscribe({
      next: () => this.loadEvents(),
      error: (err) => { console.error(err); alert('Erreur lors du changement de statut'); }
    });
  }

  applyFilters(): void {
    let result = [...this.events];
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      result = result.filter(e =>
        e.title?.toLowerCase().includes(term) ||
        e.description?.toLowerCase().includes(term) ||
        e.location?.toLowerCase().includes(term)
      );
    }
    if (this.filterStatus !== 'ALL') result = result.filter(e => e.status === this.filterStatus);
    if (this.filterMode !== 'ALL') result = result.filter(e => e.mode === this.filterMode);
    this.filteredEvents = result;
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      DRAFT: 'Brouillon', PUBLISHED: 'Publié', FULL: 'Complet',
      ONGOING: 'En cours', COMPLETED: 'Terminé', CANCELLED: 'Annulé'
    };
    return map[status] || status;
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      DRAFT: 'bg-gray-100 text-gray-700',
      PUBLISHED: 'bg-blue-100 text-blue-800',
      FULL: 'bg-orange-100 text-orange-800',
      ONGOING: 'bg-green-100 text-green-800',
      COMPLETED: 'bg-purple-100 text-purple-800',
      CANCELLED: 'bg-red-100 text-red-800'
    };
    return map[status] || 'bg-gray-100 text-gray-800';
  }

  getModeIcon(mode: string): string {
    const map: Record<string, string> = { PHYSICAL: '🏢', ONLINE: '💻', HYBRID: '🔀' };
    return map[mode] || '📌';
  }

  getModeLabel(mode: string): string {
    const map: Record<string, string> = { PHYSICAL: 'Présentiel', ONLINE: 'En ligne', HYBRID: 'Hybride' };
    return map[mode] || mode;
  }

  getParticipantPercent(event: EventResponse): number {
    if (!event.maxParticipations) return 0;
    return Math.min(100, Math.round(((event.currentParticipations || 0) / event.maxParticipations) * 100));
  }

  getMinDate(): string {
    return new Date().toISOString().slice(0, 16);
  }

  canPublish(event: EventResponse): boolean { return event.status === 'DRAFT'; }
  canStart(event: EventResponse): boolean { return event.status === 'PUBLISHED' || event.status === 'FULL'; }
  canEnd(event: EventResponse): boolean { return event.status === 'ONGOING'; }
  canCancel(event: EventResponse): boolean { return ['DRAFT', 'PUBLISHED', 'FULL', 'ONGOING'].includes(event.status); }
}
