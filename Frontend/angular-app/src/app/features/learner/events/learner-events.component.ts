import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { EventService, EventResponse, EventRegistration } from '../../../core/services/event.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-learner-events',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './learner-events.component.html',
  styleUrl: './learner-events.component.scss'
})
export class LearnerEventsComponent implements OnInit {

  events: EventResponse[] = [];
  filteredEvents: EventResponse[] = [];
  userRegistrations: EventRegistration[] = [];
  loading = false;
  searchTerm = '';
  filterMode = 'ALL';
  filterStatus = 'ALL';
  currentUserId: number | null = null;

  joinedEventIds: number[] = [];
  showRegisterModal = false;
  successMessage: string | null = null;

  canRegister(event: EventResponse): boolean {
    return event.status === 'PUBLISHED' && !this.isRegistered(event.eventId);
  }
  selectedEvent: EventResponse | null = null;
  selectedMode = 'PHYSICAL';
  registering = false;

  constructor(
    private eventService: EventService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const userInfo = this.authService.getUserInfo();
    this.currentUserId = userInfo?.userId || null;
    this.loadEvents();
    if (this.currentUserId) {
      this.loadUserRegistrations();
    }

    // Après retour d'un paiement Stripe réussi, afficher un message de succès
    const paymentSuccess = this.route.snapshot.queryParamMap.get('payment');
    if (paymentSuccess === 'success') {
      this.successMessage = '✅ Paiement confirmé ! Vous êtes maintenant inscrit(e) à l\'événement.';
      setTimeout(() => { this.successMessage = null; }, 5000);
    }
  }

  loadEvents(): void {
    this.loading = true;
    this.eventService.getAllEvents().subscribe({
      next: (events) => {
        this.events = events.filter(e => ['PUBLISHED', 'ONGOING', 'FULL', 'COMPLETED'].includes(e.status));
        this.loading = false;
        this.applyFilters();
      },
      error: (err) => {
        console.error('Error loading events:', err);
        this.loading = false;
      }
    });
  }

  loadUserRegistrations(): void {
    if (!this.currentUserId) return;
    this.eventService.getUserRegistrations(this.currentUserId).subscribe({
      next: (regs) => {
        this.userRegistrations = regs;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error loading registrations:', err)
    });
  }

  isRegistered(eventId: number): boolean {
    return this.userRegistrations.some(
      r => r.eventId === eventId && r.status !== 'CANCELLED'
    );
  }

  getRegistration(eventId: number): EventRegistration | undefined {
    return this.userRegistrations.find(r => r.eventId === eventId && r.status !== 'CANCELLED');
  }

  openRegisterModal(event: EventResponse): void {
    this.selectedEvent = event;
    this.selectedMode = event.mode === 'ONLINE' ? 'ONLINE' : 'PHYSICAL';
    this.showRegisterModal = true;
  }

  closeRegisterModal(): void {
    this.showRegisterModal = false;
    this.selectedEvent = null;
    this.registering = false;
  }

  confirmRegister(): void {
    if (!this.selectedEvent || !this.currentUserId) return;
    this.registering = true;
    this.eventService.registerForEvent(
      this.selectedEvent.eventId, 
      this.currentUserId, 
      this.selectedMode
    ).subscribe({
      next: (response) => {
        // Événement payant → rediriger vers Stripe Checkout
        if (this.selectedEvent!.isPaid && this.selectedEvent!.price > 0) {
          this.eventService.createCheckoutSession(
            response.registrationId,
            this.selectedEvent!.eventId,
            this.currentUserId!,
            this.selectedEvent!.price,
            this.selectedEvent!.title
          ).subscribe({
            next: (checkout) => {
              this.registering = false;
              this.closeRegisterModal();
              // Rediriger vers la page Stripe Checkout
              window.location.href = checkout.checkoutUrl;
            },
            error: (err) => {
              this.registering = false;
              console.error('Error creating checkout session:', err);
              alert('Inscription créée mais erreur lors de la création du paiement. Contactez le support.');
              this.closeRegisterModal();
              this.loadUserRegistrations();
              this.loadEvents();
            }
          });
        } else {
          // Événement gratuit → confirmation directe
          this.registering = false;
          this.closeRegisterModal();
          this.loadUserRegistrations();
          this.loadEvents();
          this.successMessage = '✅ Inscription confirmée !';
          setTimeout(() => { this.successMessage = null; }, 4000);
        }
      },
      error: (err) => {
        this.registering = false;
        console.error('Error registering:', err);
        const msg = err.error?.message || 'Erreur lors de l\'inscription';
        alert('❌ ' + msg);
      }
    });
  }

  cancelRegistration(event: EventResponse): void {
    const reg = this.getRegistration(event.eventId);
    if (!reg || !this.currentUserId) return;
    if (!confirm('Annuler votre inscription à cet événement ?')) return;
    this.eventService.cancelRegistration(reg.registrationId, this.currentUserId).subscribe({
      next: () => {
        this.loadUserRegistrations();
        this.loadEvents();
        alert('Inscription annulée');
      },
      error: (err) => {
        console.error('Error cancelling:', err);
        alert('Erreur lors de l\'annulation');
      }
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
    if (this.filterMode !== 'ALL') result = result.filter(e => e.mode === this.filterMode);
    if (this.filterStatus !== 'ALL') result = result.filter(e => e.status === this.filterStatus);
    this.filteredEvents = result;
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      PUBLISHED: 'Inscriptions ouvertes', FULL: 'Complet',
      ONGOING: 'En cours', COMPLETED: 'Terminé'
    };
    return map[status] || status;
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      PUBLISHED: 'bg-blue-100 text-blue-800',
      FULL: 'bg-orange-100 text-orange-800',
      ONGOING: 'bg-green-100 text-green-800',
      COMPLETED: 'bg-gray-100 text-gray-600'
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

  getUpcomingCount(): number { return this.events.filter(e => e.status === 'PUBLISHED').length; }
  getOngoingCount(): number { return this.events.filter(e => e.status === 'ONGOING').length; }

  joinEvent(event: EventResponse): void {
    this.joinedEventIds = [...this.joinedEventIds, event.eventId];
    this.cdr.markForCheck();
    this.cdr.detectChanges();
  }

  isJoined(eventId: number): boolean {
    return this.joinedEventIds.includes(eventId);
  }
}
