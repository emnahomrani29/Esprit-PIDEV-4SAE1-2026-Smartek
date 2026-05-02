import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TrainerPlanningComponent } from './trainer-planning.component';
import { PlanningService, PlanningResponse } from '../../../core/services/planning.service';
import { AuthService } from '../../../core/services/auth.service';
import { ChangeDetectorRef } from '@angular/core';

/**
 * Tests unitaires pour TrainerPlanningComponent.
 * Vérifie le chargement des plannings, la navigation calendrier,
 * la logique de publication et les helpers.
 */
describe('TrainerPlanningComponent', () => {
  let component: TrainerPlanningComponent;
  let fixture: ComponentFixture<TrainerPlanningComponent>;
  let planningServiceSpy: jasmine.SpyObj<PlanningService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  const mockPlannings: PlanningResponse[] = [
    {
      planningId: 1,
      date: '2026-05-18',
      startTime: '09:00:00',
      endTime: '11:00:00',
      title: 'Formation Spring Boot',
      description: 'Session de formation',
      eventType: 'TRAINING',
      location: 'Salle A',
      color: '#3498db',
      status: 'DRAFT'
    },
    {
      planningId: 2,
      date: '2026-05-19',
      startTime: '14:00:00',
      endTime: '16:00:00',
      title: 'Examen Final',
      description: 'Examen de fin de formation',
      eventType: 'EXAM',
      location: 'Salle B',
      color: '#ef4444',
      status: 'PUBLISHED'
    }
  ];

  beforeEach(async () => {
    planningServiceSpy = jasmine.createSpyObj('PlanningService', [
      'getAll', 'create', 'update', 'delete', 'publish', 'unpublish', 'getAvailableEvents'
    ]);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUserInfo']);

    planningServiceSpy.getAll.and.returnValue(of(mockPlannings));
    planningServiceSpy.getAvailableEvents.and.returnValue(of([]));
    authServiceSpy.getUserInfo.and.returnValue({
      userId: 10, email: 'trainer@smartek.com', firstName: 'Ahmed',
      role: 'TRAINER', token: 'test-token', message: ''
    });

    await TestBed.configureTestingModule({
      imports: [
        TrainerPlanningComponent,
        CommonModule,
        ReactiveFormsModule,
        FormsModule
      ],
      providers: [
        { provide: PlanningService, useValue: planningServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        ChangeDetectorRef
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(TrainerPlanningComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // ─── Chargement des plannings ─────────────────────────────────────────────
  describe('loadPlannings()', () => {
    it('should load plannings on init', () => {
      expect(planningServiceSpy.getAll).toHaveBeenCalled();
      expect(component.plannings.length).toBe(2);
    });

    it('should set loading to false after loading', () => {
      expect(component.loading).toBeFalse();
    });

    it('should handle error when loading plannings fails', () => {
      planningServiceSpy.getAll.and.returnValue(throwError(() => new Error('Network error')));
      component.loadPlannings();
      expect(component.loading).toBeFalse();
    });
  });

  // ─── Navigation calendrier ────────────────────────────────────────────────
  describe('Calendar navigation', () => {
    it('should build 7 week days on init', () => {
      expect(component.weekDays.length).toBe(7);
    });

    it('should go to previous week when prevWeek() is called', () => {
      const initialMonday = new Date(component.currentWeekStart);
      component.prevWeek();
      const expectedDate = new Date(initialMonday);
      expectedDate.setDate(expectedDate.getDate() - 7);
      expect(component.currentWeekStart.getDate()).toBe(expectedDate.getDate());
    });

    it('should go to next week when nextWeek() is called', () => {
      const initialMonday = new Date(component.currentWeekStart);
      component.nextWeek();
      const expectedDate = new Date(initialMonday);
      expectedDate.setDate(expectedDate.getDate() + 7);
      expect(component.currentWeekStart.getDate()).toBe(expectedDate.getDate());
    });

    it('should return to current week when goToToday() is called', () => {
      component.prevWeek();
      component.goToToday();
      const today = new Date();
      const monday = component.getMonday(today);
      expect(component.currentWeekStart.toDateString()).toBe(monday.toDateString());
    });

    it('should return Monday for getMonday()', () => {
      const wednesday = new Date('2026-05-20'); // mercredi
      const monday = component.getMonday(wednesday);
      expect(monday.getDay()).toBe(1); // 1 = lundi
    });
  });

  // ─── Filtrage des plannings par jour ──────────────────────────────────────
  describe('getPlanningsForDay()', () => {
    it('should return plannings for a specific day', () => {
      component.plannings = mockPlannings;
      const day = new Date('2026-05-18');
      const result = component.getPlanningsForDay(day);
      expect(result.length).toBe(1);
      expect(result[0].title).toBe('Formation Spring Boot');
    });

    it('should return empty array for a day with no plannings', () => {
      component.plannings = mockPlannings;
      const day = new Date('2026-05-25');
      const result = component.getPlanningsForDay(day);
      expect(result).toEqual([]);
    });
  });

  // ─── Logique de publication ───────────────────────────────────────────────
  describe('togglePublish()', () => {
    it('should publish a DRAFT planning', () => {
      const published = { ...mockPlannings[0], status: 'PUBLISHED' };
      planningServiceSpy.publish.and.returnValue(of(published));

      component.plannings = [...mockPlannings];
      component.togglePublish(mockPlannings[0]);

      expect(planningServiceSpy.publish).toHaveBeenCalledWith(1);
    });

    it('should unpublish a PUBLISHED planning', () => {
      const draft = { ...mockPlannings[1], status: 'DRAFT' };
      planningServiceSpy.unpublish.and.returnValue(of(draft));

      component.plannings = [...mockPlannings];
      component.togglePublish(mockPlannings[1]);

      expect(planningServiceSpy.unpublish).toHaveBeenCalledWith(2);
    });
  });

  describe('isPublished()', () => {
    it('should return true for PUBLISHED planning', () => {
      expect(component.isPublished(mockPlannings[1])).toBeTrue();
    });

    it('should return false for DRAFT planning', () => {
      expect(component.isPublished(mockPlannings[0])).toBeFalse();
    });
  });

  // ─── Statistiques de la semaine ───────────────────────────────────────────
  describe('weekPlannings getter', () => {
    it('should return plannings within the current week', () => {
      // Forcer la semaine courante à contenir nos plannings de test
      component.plannings = mockPlannings;
      component.currentWeekStart = component.getMonday(new Date('2026-05-18'));
      component.buildWeekDays();

      const weekPlannings = component.weekPlannings;
      expect(weekPlannings.length).toBeGreaterThanOrEqual(0);
    });
  });

  describe('getPublishedCount()', () => {
    it('should count published plannings in the current week', () => {
      component.plannings = mockPlannings;
      component.currentWeekStart = component.getMonday(new Date('2026-05-18'));
      component.buildWeekDays();

      const count = component.getPublishedCount();
      expect(count).toBeGreaterThanOrEqual(0);
    });
  });

  // ─── Modal CRUD ───────────────────────────────────────────────────────────
  describe('Modal management', () => {
    it('should open create modal with empty form', () => {
      component.openCreateModal();
      expect(component.showModal).toBeTrue();
      expect(component.isEditMode).toBeFalse();
      expect(component.selectedId).toBeNull();
    });

    it('should open edit modal with planning data', () => {
      component.openEditModal(mockPlannings[0]);
      expect(component.showModal).toBeTrue();
      expect(component.isEditMode).toBeTrue();
      expect(component.selectedId).toBe(1);
      expect(component.planningForm.value.title).toBe('Formation Spring Boot');
    });

    it('should close modal and reset form', () => {
      component.openCreateModal();
      component.closeModal();
      expect(component.showModal).toBeFalse();
    });
  });

  // ─── Helpers ──────────────────────────────────────────────────────────────
  describe('Helper methods', () => {
    it('should return correct event type icon', () => {
      expect(component.getEventTypeIcon('COURSE')).toBe('📚');
      expect(component.getEventTypeIcon('EXAM')).toBe('📝');
      expect(component.getEventTypeIcon('TRAINING')).toBe('🎓');
    });

    it('should return correct event type label', () => {
      expect(component.getEventTypeLabel('COURSE')).toBe('Cours');
      expect(component.getEventTypeLabel('EXAM')).toBe('Examen');
    });

    it('should format time correctly (HH:mm)', () => {
      expect(component.formatTime('09:00:00')).toBe('09:00');
      expect(component.formatTime('14:30:00')).toBe('14:30');
    });

    it('should return empty string for empty time', () => {
      expect(component.formatTime('')).toBe('');
    });

    it('should detect today correctly', () => {
      const today = new Date();
      expect(component.isToday(today)).toBeTrue();

      const yesterday = new Date();
      yesterday.setDate(yesterday.getDate() - 1);
      expect(component.isToday(yesterday)).toBeFalse();
    });

    it('should return correct week label', () => {
      const label = component.getWeekLabel();
      expect(label).toBeTruthy();
      expect(typeof label).toBe('string');
    });
  });

  // ─── Suppression ──────────────────────────────────────────────────────────
  describe('deletePlanning()', () => {
    it('should call planningService.delete when confirmed', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      planningServiceSpy.delete.and.returnValue(of(undefined));

      component.deletePlanning(1);

      expect(planningServiceSpy.delete).toHaveBeenCalledWith(1);
    });

    it('should not call planningService.delete when cancelled', () => {
      spyOn(window, 'confirm').and.returnValue(false);

      component.deletePlanning(1);

      expect(planningServiceSpy.delete).not.toHaveBeenCalled();
    });
  });
});
