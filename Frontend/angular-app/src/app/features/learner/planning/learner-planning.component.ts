import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PlanningService, PlanningResponse } from '../../../core/services/planning.service';

@Component({
  selector: 'app-learner-planning',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './learner-planning.component.html',
  styleUrl: './learner-planning.component.scss'
})
export class LearnerPlanningComponent implements OnInit {

  plannings: PlanningResponse[] = [];
  filteredPlannings: PlanningResponse[] = [];
  loading = false;
  filterType = 'ALL';
  currentWeekStart: Date = this.getMonday(new Date());
  weekDays: Date[] = [];

  eventTypes = [
    { value: 'COURSE', label: 'Cours', icon: '📚' },
    { value: 'TRAINING', label: 'Formation', icon: '🎓' },
    { value: 'EXAM', label: 'Examen', icon: '📝' },
    { value: 'MEETING', label: 'Réunion', icon: '👥' },
    { value: 'OTHER', label: 'Autre', icon: '📌' }
  ];

  constructor(private planningService: PlanningService) {}

  ngOnInit(): void {
    this.buildWeekDays();
    this.loadPublishedPlannings();
  }

  loadPublishedPlannings(): void {
    this.loading = true;
    this.planningService.getPublished().subscribe({
      next: (data) => {
        this.plannings = data;
        this.loading = false;
        this.applyFilter();
      },
      error: (err) => {
        console.error('Error loading plannings:', err);
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    this.filteredPlannings = this.filterType === 'ALL'
      ? [...this.plannings]
      : this.plannings.filter(p => p.eventType === this.filterType);
  }

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
    return this.filteredPlannings
      .filter(p => p.date === dateStr)
      .sort((a, b) => a.startTime.localeCompare(b.startTime));
  }

  isToday(day: Date): boolean {
    return day.toDateString() === new Date().toDateString();
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

  getDayName(day: Date): string {
    return day.toLocaleDateString('fr-FR', { weekday: 'short' });
  }

  formatTime(time: string): string {
    return time ? time.substring(0, 5) : '';
  }

  getEventTypeIcon(type: string): string {
    return this.eventTypes.find(e => e.value === type)?.icon || '📌';
  }

  getEventTypeLabel(type: string): string {
    return this.eventTypes.find(e => e.value === type)?.label || type;
  }

  getUpcomingCount(): number {
    const today = this.toDateString(new Date());
    return this.plannings.filter(p => p.date >= today).length;
  }
}
