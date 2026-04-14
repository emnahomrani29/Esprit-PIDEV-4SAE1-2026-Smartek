import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { EventService } from '../../../core/services/event.service';
import { Event, EventMode, EventRequest } from '../../../core/models/event.model';

@Component({
  selector: 'app-trainer-events',
  standalone: true,
  imports: [CommonModule, PageHeaderComponent, FormsModule],
  templateUrl: './trainer-events.component.html',
  styleUrl: './trainer-events.component.scss'
})
export class TrainerEventsComponent implements OnInit {
  events: Event[] = [];
  filteredEvents: Event[] = [];
  loading = false;
  showModal = false;
  isEditMode = false;
  searchTerm = '';
  selectedStatus = 'ALL';
  selectedMode = 'ALL';
  sortBy = 'date';
  sortOrder = 'asc';
  
  eventForm: EventRequest = {
    title: '',
    description: '',
    startDate: '',
    endDate: '',
    location: '',
    maxParticipations: 50,
    physicalCapacity: 50,
    onlineCapacity: 0,
    mode: 'PHYSICAL',
    price: 0,
    isPaid: false
  };

  selectedEventId?: number;
  EventMode = EventMode;

  constructor(private eventService: EventService) {}

  ngOnInit(): void {
    this.loadEvents();
  }

  // Update/Refresh functionality
  refreshEvents(): void {
    this.loading = true;
    this.loadEvents();
  }

  loadEvents(): void {
    this.loading = true;
    this.eventService.getAllEvents().subscribe({
      next: (data) => {
        this.events = data;
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading events:', error);
        this.loading = false;
      }
    });
  }

  // Advanced filtering and sorting
  applyFilters(): void {
    let filtered = [...this.events];

    // Filter by status
    if (this.selectedStatus !== 'ALL') {
      filtered = filtered.filter(event => event.status === this.selectedStatus);
    }

    // Filter by mode
    if (this.selectedMode !== 'ALL') {
      filtered = filtered.filter(event => event.mode === this.selectedMode);
    }

    // Dynamic search filter
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(event =>
        event.title.toLowerCase().includes(term) ||
        event.location.toLowerCase().includes(term) ||
        event.description?.toLowerCase().includes(term) ||
        event.status?.toLowerCase().includes(term) ||
        event.mode?.toLowerCase().includes(term)
      );
    }

    // Apply sorting
    filtered = this.applySorting(filtered);

    this.filteredEvents = filtered;
  }

  // Advanced sorting functionality
  applySorting(events: Event[]): Event[] {
    return events.sort((a, b) => {
      let comparison = 0;

      switch (this.sortBy) {
        case 'date':
          comparison = new Date(a.startDate).getTime() - new Date(b.startDate).getTime();
          break;
        case 'title':
          comparison = a.title.localeCompare(b.title);
          break;
        case 'status':
          comparison = (a.status || '').localeCompare(b.status || '');
          break;
        case 'mode':
          comparison = (a.mode || '').localeCompare(b.mode || '');
          break;
        case 'location':
          comparison = (a.location || '').localeCompare(b.location || '');
          break;
        case 'participants':
          comparison = (a.currentParticipations || 0) - (b.currentParticipations || 0);
          break;
        case 'capacity':
          comparison = a.maxParticipations - b.maxParticipations;
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

  getEventsStats() {
    const activeEvents = this.events.filter(e => e.status === 'PUBLISHED' || e.status === 'ONGOING').length;
    const totalParticipants = this.events.reduce((sum, e) => sum + (e.currentParticipations || 0), 0);
    const draftEvents = this.events.filter(e => e.status === 'DRAFT').length;
    const completedEvents = this.events.filter(e => e.status === 'COMPLETED').length;
    
    return [
      { label: 'Active Events', value: activeEvents },
      { label: 'Total Participants', value: totalParticipants },
      { label: 'Draft Events', value: draftEvents },
      { label: 'Completed Events', value: completedEvents }
    ];
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.resetForm();
    this.showModal = true;
  }

  openEditModal(event: Event): void {
    this.isEditMode = true;
    this.selectedEventId = event.eventId;
    this.eventForm = {
      title: event.title,
      description: event.description || '',
      startDate: this.formatDateForInput(event.startDate),
      endDate: this.formatDateForInput(event.endDate),
      location: event.location,
      maxParticipations: event.maxParticipations,
      physicalCapacity: event.physicalCapacity,
      onlineCapacity: event.onlineCapacity,
      mode: event.mode,
      price: event.price || 0,
      isPaid: event.isPaid || false
    };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.resetForm();
  }

  resetForm(): void {
    this.eventForm = {
      title: '',
      description: '',
      startDate: '',
      endDate: '',
      location: '',
      maxParticipations: 50,
      physicalCapacity: 50,
      onlineCapacity: 0,
      mode: 'PHYSICAL',
      price: 0,
      isPaid: false
    };
    this.selectedEventId = undefined;
  }

  saveEvent(): void {
    if (this.isEditMode && this.selectedEventId) {
      this.eventService.updateEvent(this.selectedEventId, this.eventForm).subscribe({
        next: () => {
          this.loadEvents();
          this.closeModal();
          alert('Event updated successfully!');
        },
        error: (error) => {
          console.error('Error updating event:', error);
          alert('Error updating event');
        }
      });
    } else {
      this.eventService.createEvent(this.eventForm).subscribe({
        next: () => {
          this.loadEvents();
          this.closeModal();
          alert('Event created successfully!');
        },
        error: (error) => {
          console.error('Error creating event:', error);
          alert('Error creating event');
        }
      });
    }
  }

  deleteEvent(id: number): void {
    if (confirm('Are you sure you want to delete this event?')) {
      this.eventService.deleteEvent(id).subscribe({
        next: () => {
          this.loadEvents();
          alert('Event deleted successfully!');
        },
        error: (error) => {
          console.error('Error deleting event:', error);
          alert('Error deleting event');
        }
      });
    }
  }

  onModeChange(): void {
    if (this.eventForm.mode === 'PHYSICAL') {
      this.eventForm.physicalCapacity = this.eventForm.maxParticipations;
      this.eventForm.onlineCapacity = 0;
    } else if (this.eventForm.mode === 'ONLINE') {
      this.eventForm.physicalCapacity = 0;
      this.eventForm.onlineCapacity = this.eventForm.maxParticipations;
    } else {
      this.eventForm.physicalCapacity = Math.floor(this.eventForm.maxParticipations / 2);
      this.eventForm.onlineCapacity = Math.ceil(this.eventForm.maxParticipations / 2);
    }
  }

  formatDateForInput(dateString: string): string {
    const date = new Date(dateString);
    return date.toISOString().slice(0, 16);
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

  getStatusBadgeClass(status?: string): string {
    switch (status) {
      case 'PUBLISHED': return 'bg-green-100 text-green-800';
      case 'DRAFT': return 'bg-gray-100 text-gray-800';
      case 'ONGOING': return 'bg-blue-100 text-blue-800';
      case 'COMPLETED': return 'bg-purple-100 text-purple-800';
      case 'CANCELLED': return 'bg-red-100 text-red-800';
      case 'FULL': return 'bg-orange-100 text-orange-800';
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

  publishEvent(eventId: number): void {
    if (confirm('Are you sure you want to publish this event?')) {
      this.eventService.changeEventStatus(eventId, 'PUBLISHED', 'Event published by trainer').subscribe({
        next: () => {
          this.loadEvents();
          alert('Event published successfully!');
        },
        error: (error) => {
          console.error('Error publishing event:', error);
          alert('Error publishing event');
        }
      });
    }
  }

  cancelEvent(eventId: number): void {
    if (confirm('Are you sure you want to cancel this event?')) {
      this.eventService.changeEventStatus(eventId, 'CANCELLED', 'Event cancelled by trainer').subscribe({
        next: () => {
          this.loadEvents();
          alert('Event cancelled successfully!');
        },
        error: (error) => {
          console.error('Error cancelling event:', error);
          alert('Error cancelling event');
        }
      });
    }
  }

  getAvailableSpots(event: Event): number {
    return event.maxParticipations - (event.currentParticipations || 0);
  }

  getParticipationRate(event: Event): number {
    return Math.round(((event.currentParticipations || 0) / event.maxParticipations) * 100);
  }
}
