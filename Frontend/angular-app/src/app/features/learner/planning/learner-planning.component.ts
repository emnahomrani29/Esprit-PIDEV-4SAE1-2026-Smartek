import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WeeklyPlanningService } from '../../../core/services/weekly-planning.service';
import { AuthService } from '../../../core/services/auth.service';
import { WeeklyPlanningItem } from '../../../core/models/weekly-planning.model';

@Component({
  selector: 'app-learner-planning',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './learner-planning.component.html',
  styleUrl: './learner-planning.component.scss'
})
export class LearnerPlanningComponent implements OnInit {
  currentWeekStart: Date = new Date();
  publishedItems: WeeklyPlanningItem[] = [];
  filteredItems: WeeklyPlanningItem[] = [];
  registeredItems: Set<number> = new Set();
  loading = false;
  selectedType: string = 'ALL';
  searchTerm = '';
  currentUserId = 0;
  sortBy: string = 'time';
  sortOrder: string = 'asc';

  // Week days in English
  weekDays = [
    { key: 'monday', label: 'Monday', date: '' },
    { key: 'tuesday', label: 'Tuesday', date: '' },
    { key: 'wednesday', label: 'Wednesday', date: '' },
    { key: 'thursday', label: 'Thursday', date: '' },
    { key: 'friday', label: 'Friday', date: '' },
    { key: 'saturday', label: 'Saturday', date: '' },
    { key: 'sunday', label: 'Sunday', date: '' }
  ];

  constructor(
    private weeklyPlanningService: WeeklyPlanningService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const userInfo = this.authService.getUserInfo();
    if (userInfo) {
      this.currentUserId = userInfo.userId;
    }
    
    this.setCurrentWeek();
    this.loadPublishedPlannings();
  }

  setCurrentWeek(): void {
    const today = new Date();
    const monday = new Date(today);
    monday.setDate(today.getDate() - today.getDay() + 1);
    this.currentWeekStart = monday;
    this.updateWeekDays();
  }

  updateWeekDays(): void {
    this.weekDays.forEach((day, index) => {
      const date = new Date(this.currentWeekStart);
      date.setDate(this.currentWeekStart.getDate() + index);
      day.date = date.toISOString().split('T')[0];
    });
  }

  previousWeek(): void {
    this.currentWeekStart.setDate(this.currentWeekStart.getDate() - 7);
    this.updateWeekDays();
    this.loadPublishedPlannings();
  }

  nextWeek(): void {
    this.currentWeekStart.setDate(this.currentWeekStart.getDate() + 7);
    this.updateWeekDays();
    this.loadPublishedPlannings();
  }

  // Update/Refresh button functionality
  refreshPlanning(): void {
    this.loading = true;
    this.loadPublishedPlannings();
  }

  loadPublishedPlannings(): void {
    this.loading = true;
    const weekStartDate = this.currentWeekStart.toISOString().split('T')[0];
    
    this.weeklyPlanningService.getPublishedPlannings(weekStartDate).subscribe({
      next: (items) => {
        this.publishedItems = items.map(item => ({
          ...item,
          published: true
        }));
        this.applyFilters();
        this.checkRegistrationStatus();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading published plannings:', error);
        this.publishedItems = [];
        this.applyFilters();
        this.loading = false;
      }
    });
  }

  checkRegistrationStatus(): void {
    if (!this.currentUserId) return;
    
    this.registeredItems.clear();
    this.publishedItems.forEach(item => {
      if (item.planningId) {
        this.weeklyPlanningService.isRegistered(item.planningId, this.currentUserId).subscribe({
          next: (isRegistered) => {
            if (isRegistered) {
              this.registeredItems.add(item.planningId!);
            }
          },
          error: (error) => {
            console.error('Error checking registration status:', error);
          }
        });
      }
    });
  }

  // Dynamic search and filtering
  applyFilters(): void {
    let filtered = [...this.publishedItems];

    // Filter by type
    if (this.selectedType !== 'ALL') {
      filtered = filtered.filter(item => item.type === this.selectedType);
    }

    // Dynamic search filter
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(item =>
        item.title.toLowerCase().includes(term) ||
        item.description?.toLowerCase().includes(term) ||
        item.location?.toLowerCase().includes(term) ||
        item.type.toLowerCase().includes(term)
      );
    }

    // Apply sorting
    filtered = this.applySorting(filtered);

    this.filteredItems = filtered;
  }

  // Advanced sorting functionality
  applySorting(items: WeeklyPlanningItem[]): WeeklyPlanningItem[] {
    return items.sort((a, b) => {
      let comparison = 0;

      switch (this.sortBy) {
        case 'time':
          comparison = a.startTime.localeCompare(b.startTime);
          break;
        case 'title':
          comparison = a.title.localeCompare(b.title);
          break;
        case 'type':
          comparison = a.type.localeCompare(b.type);
          break;
        case 'location':
          comparison = (a.location || '').localeCompare(b.location || '');
          break;
        case 'availability':
          const availableA = (a.maxParticipants || 0) - (a.currentParticipants || 0);
          const availableB = (b.maxParticipants || 0) - (b.currentParticipants || 0);
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

  getItemsForDay(date: string): WeeklyPlanningItem[] {
    return this.filteredItems.filter(item => item.date === date);
  }

  getTypeColor(type: string): string {
    switch (type) {
      case 'TRAINING': return '#3B82F6';
      case 'EXAM': return '#EF4444';
      case 'EVENT': return '#10B981';
      default: return '#6B7280';
    }
  }

  getTypeIcon(type: string): string {
    switch (type) {
      case 'TRAINING': return '📚';
      case 'EXAM': return '📝';
      case 'EVENT': return '🎯';
      default: return '📅';
    }
  }

  getTypeBadgeClass(type: string): string {
    switch (type) {
      case 'TRAINING': return 'bg-blue-100 text-blue-800';
      case 'EXAM': return 'bg-red-100 text-red-800';
      case 'EVENT': return 'bg-green-100 text-green-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  formatTime(time: string): string {
    return time.substring(0, 5);
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('en-US', {
      weekday: 'long',
      day: 'numeric',
      month: 'long'
    });
  }

  getAvailableSpots(item: WeeklyPlanningItem): number {
    return (item.maxParticipants || 0) - (item.currentParticipants || 0);
  }

  canRegister(item: WeeklyPlanningItem): boolean {
    return this.getAvailableSpots(item) > 0 && !this.isRegistered(item);
  }

  isRegistered(item: WeeklyPlanningItem): boolean {
    return item.planningId ? this.registeredItems.has(item.planningId) : false;
  }

  register(item: WeeklyPlanningItem): void {
    if (!this.currentUserId || !item.planningId) {
      alert('Error: User not connected or invalid session');
      return;
    }

    if (this.isRegistered(item)) {
      alert('You are already registered for this session');
      return;
    }

    if (!this.canRegister(item)) {
      alert('No more spots available for this session');
      return;
    }

    if (confirm(`Do you want to register for "${item.title}"?`)) {
      this.weeklyPlanningService.registerToSession(item.planningId, this.currentUserId).subscribe({
        next: (message) => {
          alert(message);
          this.registeredItems.add(item.planningId!);
          this.loadPublishedPlannings();
        },
        error: (error) => {
          console.error('Registration error:', error);
          alert('Registration error: ' + (error.error || error.message));
        }
      });
    }
  }

  unregister(item: WeeklyPlanningItem): void {
    if (!this.currentUserId || !item.planningId) {
      alert('Error: User not connected or invalid session');
      return;
    }

    if (!this.isRegistered(item)) {
      alert('You are not registered for this session');
      return;
    }

    if (confirm(`Do you want to unregister from "${item.title}"?`)) {
      this.weeklyPlanningService.unregisterFromSession(item.planningId, this.currentUserId).subscribe({
        next: (message) => {
          alert(message);
          this.registeredItems.delete(item.planningId!);
          this.loadPublishedPlannings();
        },
        error: (error) => {
          console.error('Unregistration error:', error);
          alert('Unregistration error: ' + (error.error || error.message));
        }
      });
    }
  }
}