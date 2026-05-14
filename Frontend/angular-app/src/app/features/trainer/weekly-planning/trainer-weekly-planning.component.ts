import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WeeklyPlanningService } from '../../../core/services/weekly-planning.service';
import { AuthService } from '../../../core/services/auth.service';
import { 
  WeeklyPlanningItem, 
  WeeklyPlanningResponse, 
  TrainingItem, 
  ExamItem, 
  EventItem 
} from '../../../core/models/weekly-planning.model';

@Component({
  selector: 'app-trainer-weekly-planning',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './trainer-weekly-planning.component.html',
  styleUrl: './trainer-weekly-planning.component.scss'
})
export class TrainerWeeklyPlanningComponent implements OnInit {
  currentWeekStart: Date = new Date();
  weeklyPlanning: WeeklyPlanningResponse | null = null;
  loading = false;
  currentUserId = 0;
  searchTerm = '';
  selectedType = 'ALL';
  sortBy = 'time';
  sortOrder = 'asc';

  // Available data
  trainings: TrainingItem[] = [];
  exams: ExamItem[] = [];
  events: EventItem[] = [];

  // Modal
  showAddModal = false;
  selectedDay = '';
  selectedItemType: 'TRAINING' | 'EXAM' | 'EVENT' = 'TRAINING';
  selectedItem: any = null;
  newItem: WeeklyPlanningItem = this.createEmptyItem();

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
    this.loadAvailableItems();
    this.loadWeeklyPlanning();
  }

  // Update/Refresh functionality
  refreshPlanning(): void {
    this.loading = true;
    this.loadAvailableItems();
    this.loadWeeklyPlanning();
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
    this.loadWeeklyPlanning();
  }

  nextWeek(): void {
    this.currentWeekStart.setDate(this.currentWeekStart.getDate() + 7);
    this.updateWeekDays();
    this.loadWeeklyPlanning();
  }

  loadAvailableItems(): void {
    this.weeklyPlanningService.getTrainings().subscribe({
      next: (trainings) => this.trainings = trainings,
      error: (error) => console.error('Error loading trainings:', error)
    });

    this.weeklyPlanningService.getExams().subscribe({
      next: (exams) => this.exams = exams,
      error: (error) => console.error('Error loading exams:', error)
    });

    this.weeklyPlanningService.getEvents().subscribe({
      next: (events) => this.events = events,
      error: (error) => console.error('Error loading events:', error)
    });
  }

  loadWeeklyPlanning(): void {
    if (!this.currentUserId) return;
    
    this.loading = true;
    const weekStartDate = this.currentWeekStart.toISOString().split('T')[0];
    
    this.weeklyPlanningService.getWeeklyPlanning(this.currentUserId, weekStartDate).subscribe({
      next: (response) => {
        this.weeklyPlanning = response;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading weekly planning:', error);
        this.loading = false;
      }
    });
  }

  // Advanced filtering and sorting
  getItemsForDay(date: string): WeeklyPlanningItem[] {
    if (!this.weeklyPlanning) return [];
    
    let items = this.weeklyPlanning.items.filter(item => item.date === date);

    // Apply type filter
    if (this.selectedType !== 'ALL') {
      items = items.filter(item => item.type === this.selectedType);
    }

    // Apply search filter
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      items = items.filter(item =>
        item.title.toLowerCase().includes(term) ||
        item.description?.toLowerCase().includes(term) ||
        item.location?.toLowerCase().includes(term) ||
        item.type.toLowerCase().includes(term)
      );
    }

    // Apply sorting
    return this.applySorting(items);
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
        case 'status':
          comparison = (a.status || '').localeCompare(b.status || '');
          break;
        case 'participants':
          comparison = (a.currentParticipants || 0) - (b.currentParticipants || 0);
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
  }

  getSortIcon(sortBy: string): string {
    if (this.sortBy !== sortBy) return '↕️';
    return this.sortOrder === 'asc' ? '↑' : '↓';
  }

  // Get filtered count for display
  getFilteredItemsCount(): number {
    if (!this.weeklyPlanning) return 0;
    
    let items = this.weeklyPlanning.items;

    if (this.selectedType !== 'ALL') {
      items = items.filter(item => item.type === this.selectedType);
    }

    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      items = items.filter(item =>
        item.title.toLowerCase().includes(term) ||
        item.description?.toLowerCase().includes(term) ||
        item.location?.toLowerCase().includes(term) ||
        item.type.toLowerCase().includes(term)
      );
    }

    return items.length;
  }

  openAddModal(dayDate: string): void {
    this.selectedDay = dayDate;
    this.newItem = this.createEmptyItem();
    this.newItem.date = dayDate;
    this.showAddModal = true;
  }

  closeAddModal(): void {
    this.showAddModal = false;
    this.selectedItem = null;
    this.newItem = this.createEmptyItem();
  }

  onTypeChange(): void {
    this.selectedItem = null;
    this.newItem.title = '';
    this.newItem.description = '';
    this.newItem.location = '';
    this.newItem.maxParticipants = undefined;
  }

  onItemSelect(): void {
    if (!this.selectedItem) return;

    switch (this.selectedItemType) {
      case 'TRAINING':
        this.newItem.title = this.selectedItem.title;
        this.newItem.description = this.selectedItem.description;
        this.newItem.itemId = this.selectedItem.trainingId;
        this.newItem.color = '#3B82F6';
        break;
      case 'EXAM':
        this.newItem.title = this.selectedItem.title;
        this.newItem.description = this.selectedItem.description;
        this.newItem.itemId = this.selectedItem.id;
        this.newItem.color = '#EF4444';
        break;
      case 'EVENT':
        this.newItem.title = this.selectedItem.title;
        this.newItem.description = this.selectedItem.description;
        this.newItem.location = this.selectedItem.location;
        this.newItem.maxParticipants = this.selectedItem.maxParticipations;
        this.newItem.itemId = this.selectedItem.eventId;
        this.newItem.color = '#10B981';
        break;
    }
  }

  addItem(): void {
    if (!this.newItem.title || !this.newItem.startTime || !this.newItem.endTime) {
      alert('Please fill in all required fields');
      return;
    }

    const currentItems = this.weeklyPlanning?.items || [];
    const updatedItems = [...currentItems, { ...this.newItem }];

    this.saveWeeklyPlanning(updatedItems);
    this.closeAddModal();
  }

  removeItem(item: WeeklyPlanningItem): void {
    if (!confirm('Are you sure you want to delete this item?')) return;

    const currentItems = this.weeklyPlanning?.items || [];
    const updatedItems = currentItems.filter(i => 
      !(i.date === item.date && i.startTime === item.startTime && i.title === item.title)
    );

    this.saveWeeklyPlanning(updatedItems);
  }

  saveWeeklyPlanning(items: WeeklyPlanningItem[]): void {
    const request = {
      weekStartDate: this.currentWeekStart.toISOString().split('T')[0],
      trainerId: this.currentUserId,
      items: items
    };

    this.weeklyPlanningService.createOrUpdateWeeklyPlanning(request).subscribe({
      next: (response) => {
        this.weeklyPlanning = response;
        alert('Planning saved successfully!');
      },
      error: (error) => {
        console.error('Error saving weekly planning:', error);
        alert('Error saving planning');
      }
    });
  }

  publishWeeklyPlanning(): void {
    if (!confirm('Are you sure you want to publish the entire planning for this week?')) return;

    const weekStartDate = this.currentWeekStart.toISOString().split('T')[0];
    
    this.weeklyPlanningService.publishWeeklyPlanning(this.currentUserId, weekStartDate).subscribe({
      next: (response) => {
        this.weeklyPlanning = response;
        alert('Planning published successfully! Learners can now see the sessions.');
      },
      error: (error) => {
        console.error('Error publishing weekly planning:', error);
        alert('Error publishing planning');
      }
    });
  }

  unpublishWeeklyPlanning(): void {
    if (!confirm('Are you sure you want to unpublish the entire planning for this week?')) return;

    const weekStartDate = this.currentWeekStart.toISOString().split('T')[0];
    
    this.weeklyPlanningService.unpublishWeeklyPlanning(this.currentUserId, weekStartDate).subscribe({
      next: (response) => {
        this.weeklyPlanning = response;
        alert('Planning unpublished successfully!');
      },
      error: (error) => {
        console.error('Error unpublishing weekly planning:', error);
        alert('Error unpublishing planning');
      }
    });
  }

  private createEmptyItem(): WeeklyPlanningItem {
    return {
      type: 'TRAINING',
      title: '',
      description: '',
      date: '',
      startTime: '09:00',
      endTime: '11:00',
      location: '',
      color: '#3B82F6',
      maxParticipants: 20,
      status: 'DRAFT'
    };
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

  formatTime(time: string): string {
    return time.substring(0, 5);
  }

  formatDate(date: string): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString('en-US', {
      weekday: 'long',
      day: 'numeric',
      month: 'long'
    });
  }

  getStats() {
    return this.weeklyPlanning?.stats || {
      totalSessions: 0,
      trainingSessions: 0,
      examSessions: 0,
      eventSessions: 0,
      publishedSessions: 0,
      draftSessions: 0,
      totalHours: 0
    };
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'PUBLISHED': return 'bg-green-100 text-green-800';
      case 'DRAFT': return 'bg-gray-100 text-gray-800';
      case 'SCHEDULED': return 'bg-blue-100 text-blue-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  getAvailableSpots(item: WeeklyPlanningItem): number {
    return (item.maxParticipants || 0) - (item.currentParticipants || 0);
  }

  getParticipationRate(item: WeeklyPlanningItem): number {
    if (!item.maxParticipants) return 0;
    return Math.round(((item.currentParticipants || 0) / item.maxParticipants) * 100);
  }
}