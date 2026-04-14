import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EventService } from '../../../core/services/event.service';
import { AuthService } from '../../../core/services/auth.service';
import type { Event as EventModel } from '../../../core/models/event.model';

@Component({
  selector: 'app-learner-events',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './learner-events.component.html',
  styleUrl: './learner-events.component.scss'
})
export class LearnerEventsComponent implements OnInit {
  events: EventModel[] = [];
  filteredEvents: EventModel[] = [];
  loading = false;
  searchTerm = '';
  selectedMode: string = 'ALL';
  sortBy = 'date';
  sortOrder = 'asc';
  showRegistrationModal = false;
  showDetailsModal = false;
  selectedEvent?: EventModel;
  registrationMode: string = 'PHYSICAL';
  userRegistrations: Set<number> = new Set();
  currentUserId: number = 0;
  activeTab: 'available' | 'registered' = 'available';

  constructor(
    private eventService: EventService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const userInfo = this.authService.getUserInfo();
    if (userInfo) {
      this.currentUserId = userInfo.userId;
    }
    // Load user registrations first, then events
    this.loadUserRegistrations();
  }

  // Update/Refresh functionality
  refreshEvents(): void {
    this.loading = true;
    this.loadUserRegistrations();
  }

  loadPublishedEvents(): void {
    this.loading = true;
    this.eventService.getAllEvents().subscribe({
      next: (data) => {
        console.log('All events:', data);
        console.log('Event statuses:', data.map(e => ({ id: e.eventId, status: e.status })));
        
        // For "Available" tab: show published or full events not registered
        // For "My Registrations" tab: show all events where user is registered
        this.events = data;
        
        console.log('All events loaded:', this.events.length);
        console.log('User registrations set:', Array.from(this.userRegistrations));
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading events:', error);
        this.loading = false;
      }
    });
  }

  loadUserRegistrations(): void {
    if (!this.currentUserId) {
      // Fallback to localStorage if no connected user
      const registrations = localStorage.getItem('userEventRegistrations');
      if (registrations) {
        this.userRegistrations = new Set(JSON.parse(registrations));
      }
      this.loadPublishedEvents();
      return;
    }

    // Get real registrations from backend
    this.eventService.getUserRegistrations(this.currentUserId).subscribe({
      next: (registrations) => {
        console.log('User registrations from backend:', registrations);
        // Extract eventId from active registrations (not cancelled)
        const eventIds = registrations
          .filter(r => r.status !== 'CANCELLED')
          .map(r => r.eventId);
        this.userRegistrations = new Set(eventIds);
        console.log('Registered event IDs:', Array.from(this.userRegistrations));
        
        // Also save in localStorage for consistency
        localStorage.setItem('userEventRegistrations', JSON.stringify(eventIds));
        
        // Load events after loading registrations
        this.loadPublishedEvents();
      },
      error: (error) => {
        console.error('Error loading user registrations:', error);
        // Fallback to localStorage on error
        const registrations = localStorage.getItem('userEventRegistrations');
        if (registrations) {
          this.userRegistrations = new Set(JSON.parse(registrations));
        }
        // Load events even on error
        this.loadPublishedEvents();
      }
    });
  }

  saveUserRegistration(eventId: number): void {
    this.userRegistrations.add(eventId);
    localStorage.setItem('userEventRegistrations', JSON.stringify(Array.from(this.userRegistrations)));
  }

  searchEvents(): void {
    let filtered = this.events;

    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(event =>
        event.title.toLowerCase().includes(term) ||
        event.location.toLowerCase().includes(term) ||
        event.description?.toLowerCase().includes(term)
      );
    }

    if (this.selectedMode !== 'ALL') {
      filtered = filtered.filter(event => event.mode === this.selectedMode);
    }

    this.filteredEvents = filtered;
  }

  switchTab(tab: 'available' | 'registered'): void {
    this.activeTab = tab;
    this.searchTerm = '';
    this.selectedMode = 'ALL';
    this.applyFilters();
  }

  applyFilters(): void {
    let filtered = this.events;

    // Filter by active tab
    if (this.activeTab === 'registered') {
      // "My Registrations" tab: show all events where user is registered
      filtered = filtered.filter(event => this.isUserRegistered(event.eventId!));
    } else {
      // "Available" tab: show only published/full events not registered
      filtered = filtered.filter(event => 
        (event.status === 'PUBLISHED' || event.status === 'FULL') && 
        !this.isUserRegistered(event.eventId!)
      );
    }

    // Apply search filter
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(event =>
        event.title.toLowerCase().includes(term) ||
        event.location.toLowerCase().includes(term) ||
        event.description?.toLowerCase().includes(term) ||
        event.mode?.toLowerCase().includes(term) ||
        event.status?.toLowerCase().includes(term)
      );
    }

    // Apply mode filter
    if (this.selectedMode !== 'ALL') {
      filtered = filtered.filter(event => event.mode === this.selectedMode);
    }

    // Apply sorting
    filtered = this.applySorting(filtered);

    console.log('Filtered events for tab', this.activeTab, ':', filtered.length);
    this.filteredEvents = filtered;
  }

  // Advanced sorting functionality
  applySorting(events: EventModel[]): EventModel[] {
    return events.sort((a, b) => {
      let comparison = 0;

      switch (this.sortBy) {
        case 'date':
          comparison = new Date(a.startDate).getTime() - new Date(b.startDate).getTime();
          break;
        case 'title':
          comparison = a.title.localeCompare(b.title);
          break;
        case 'location':
          comparison = (a.location || '').localeCompare(b.location || '');
          break;
        case 'mode':
          comparison = (a.mode || '').localeCompare(b.mode || '');
          break;
        case 'price':
          const priceA = a.isPaid ? (a.price || 0) : 0;
          const priceB = b.isPaid ? (b.price || 0) : 0;
          comparison = priceA - priceB;
          break;
        case 'availability':
          const availableA = a.maxParticipations - (a.currentParticipations || 0);
          const availableB = b.maxParticipations - (b.currentParticipations || 0);
          comparison = availableA - availableB;
          break;
        default:
          comparison = 0;
      }

      return this.sortOrder === 'asc' ? comparison : -comparison;
    });
  }

  // Sort control methods
  setSortBy(sortBy: string): void {
    if (this.sortBy === sortBy) {
      this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = sortBy;
      this.sortOrder = 'asc';
    }
    this.applyFilters();
  }

  getSortIcon(sortBy: string): string {
    if (this.sortBy !== sortBy) return '↕️';
    return this.sortOrder === 'asc' ? '↑' : '↓';
  }

  openRegistrationModal(event: EventModel): void {
    if (this.isUserRegistered(event.eventId!)) {
      alert('❌ You are already registered for this event!');
      return;
    }

    this.selectedEvent = event;
    this.registrationMode = event.mode === 'PHYSICAL' ? 'PHYSICAL' : 'ONLINE';
    this.showRegistrationModal = true;
  }

  closeRegistrationModal(): void {
    this.showRegistrationModal = false;
    this.selectedEvent = undefined;
  }

  openDetailsModal(event: EventModel): void {
    this.selectedEvent = event;
    this.showDetailsModal = true;
  }

  closeDetailsModal(): void {
    this.showDetailsModal = false;
    this.selectedEvent = undefined;
  }

  registerForEvent(): void {
    if (!this.selectedEvent) return;

    if (!this.currentUserId) {
      alert('❌ You must be logged in to register');
      return;
    }

    console.log('Registering with:', {
      eventId: this.selectedEvent.eventId,
      userId: this.currentUserId,
      mode: this.registrationMode
    });

    this.eventService.registerForEventAdvanced(
      this.selectedEvent.eventId!,
      this.currentUserId,
      this.registrationMode
    ).subscribe({
      next: (response) => {
        console.log('Registration response:', response);
        
        this.saveUserRegistration(this.selectedEvent!.eventId!);
        
        if (response.status === 'CONFIRMED') {
          alert(`✅ ${response.message}\n\nYour registration is confirmed!`);
        } else if (response.status === 'WAITING') {
          alert(`⏳ ${response.message}\n\nYou will be notified if a spot becomes available.`);
        }
        
        this.closeRegistrationModal();
        this.loadUserRegistrations();
      },
      error: (error) => {
        console.error('Error registering:', error);
        console.error('Error details:', error.error);
        const errorMessage = error.error?.message || error.message || 'Registration error';
        alert(`❌ ${errorMessage}`);
      }
    });
  }

  isUserRegistered(eventId: number): boolean {
    return this.userRegistrations.has(eventId);
  }

  canRegister(event: EventModel): boolean {
    return event.status === 'PUBLISHED' && !this.isFull(event) && !this.isUserRegistered(event.eventId!);
  }

  isFull(event: EventModel): boolean {
    return (event.currentParticipations || 0) >= event.maxParticipations;
  }

  getAvailableSpots(event: EventModel): number {
    return event.maxParticipations - (event.currentParticipations || 0);
  }

  getStatusBadgeClass(status?: string): string {
    switch (status) {
      case 'PUBLISHED': return 'bg-green-100 text-green-800';
      case 'FULL': return 'bg-orange-100 text-orange-800';
      case 'ONGOING': return 'bg-blue-100 text-blue-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  getModeBadgeClass(mode: string): string {
    switch (mode) {
      case 'PHYSICAL': return 'bg-blue-100 text-blue-800';
      case 'ONLINE': return 'bg-green-100 text-green-800';
      case 'HYBRID': return 'bg-purple-100 text-purple-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
