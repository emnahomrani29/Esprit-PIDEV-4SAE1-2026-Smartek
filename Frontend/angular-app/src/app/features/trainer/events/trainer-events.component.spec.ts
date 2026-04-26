import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { TrainerEventsComponent } from './trainer-events.component';
import { EventService, EventResponse } from '../../../core/services/event.service';
import { AuthService } from '../../../core/services/auth.service';

describe('TrainerEventsComponent', () => {
  let component: TrainerEventsComponent;
  let fixture: ComponentFixture<TrainerEventsComponent>;
  let eventServiceSpy: jasmine.SpyObj<EventService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  const mockEvents: EventResponse[] = [
    {
      eventId: 1, title: 'Formation DevOps', description: 'Sprint 3',
      startDate: '2026-05-01T09:00:00', endDate: '2026-05-01T17:00:00',
      location: 'Tunis', physicalCapacity: 50, onlineCapacity: 0,
      physicalRegistered: 5, onlineRegistered: 0, maxParticipations: 50,
      currentParticipations: 5, status: 'PUBLISHED', mode: 'PHYSICAL',
      price: 0, isPaid: false, createdBy: 1, isAvailable: true,
      createdAt: '2026-01-01T00:00:00', updatedAt: '2026-01-01T00:00:00'
    }
  ];

  beforeEach(async () => {
    eventServiceSpy = jasmine.createSpyObj('EventService', [
      'getAllEvents', 'createEvent', 'updateEvent', 'deleteEvent',
      'changeStatus', 'getEventRevenue'
    ]);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUserInfo']);

    eventServiceSpy.getAllEvents.and.returnValue(of(mockEvents));
    eventServiceSpy.getEventRevenue.and.returnValue(of({
      eventId: 1, totalRevenue: 0, potentialRevenue: 0,
      paidRegistrations: 0, pendingPayments: 0, averageRevenuePerParticipant: 0
    }));
    authServiceSpy.getUserInfo.and.returnValue({ userId: 1, email: 'trainer@test.com', role: 'TRAINER', token: 'tok', firstName: 'Trainer', message: '' });

    await TestBed.configureTestingModule({
      imports: [TrainerEventsComponent, HttpClientTestingModule, ReactiveFormsModule, FormsModule],
      providers: [
        { provide: EventService, useValue: eventServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TrainerEventsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load events on init', () => {
    expect(eventServiceSpy.getAllEvents).toHaveBeenCalled();
    expect(component.events.length).toBe(1);
    expect(component.events[0].title).toBe('Formation DevOps');
  });

  it('should set currentUserId from authService', () => {
    expect(component.currentUserId).toBe(1);
  });

  it('should open create modal with empty form', () => {
    component.openCreateModal();
    expect(component.showModal).toBeTrue();
    expect(component.isEditMode).toBeFalse();
    expect(component.selectedEventId).toBeNull();
  });

  it('should open edit modal with event data', () => {
    component.openEditModal(mockEvents[0]);
    expect(component.showModal).toBeTrue();
    expect(component.isEditMode).toBeTrue();
    expect(component.selectedEventId).toBe(1);
    expect(component.eventForm.value.title).toBe('Formation DevOps');
  });

  it('should close modal and reset form', () => {
    component.showModal = true;
    component.closeModal();
    expect(component.showModal).toBeFalse();
  });

  it('should apply search filter correctly', () => {
    component.events = mockEvents;
    component.searchTerm = 'DevOps';
    component.applyFilters();
    expect(component.filteredEvents.length).toBe(1);
  });

  it('should return empty filteredEvents when search term does not match', () => {
    component.events = mockEvents;
    component.searchTerm = 'XYZ_INEXISTANT';
    component.applyFilters();
    expect(component.filteredEvents.length).toBe(0);
  });

  it('should filter by status', () => {
    component.events = mockEvents;
    component.filterStatus = 'PUBLISHED';
    component.applyFilters();
    expect(component.filteredEvents.length).toBe(1);

    component.filterStatus = 'DRAFT';
    component.applyFilters();
    expect(component.filteredEvents.length).toBe(0);
  });

  it('should return correct status label', () => {
    expect(component.getStatusLabel('PUBLISHED')).toBe('Publié');
    expect(component.getStatusLabel('DRAFT')).toBe('Brouillon');
    expect(component.getStatusLabel('ONGOING')).toBe('En cours');
    expect(component.getStatusLabel('COMPLETED')).toBe('Terminé');
  });

  it('should return correct mode icon', () => {
    expect(component.getModeIcon('PHYSICAL')).toBe('🏢');
    expect(component.getModeIcon('ONLINE')).toBe('💻');
    expect(component.getModeIcon('HYBRID')).toBe('🔀');
  });

  it('should calculate participant percent correctly', () => {
    const event = { ...mockEvents[0], currentParticipations: 25, maxParticipations: 50 };
    expect(component.getParticipantPercent(event)).toBe(50);
  });

  it('should cap participant percent at 100', () => {
    const event = { ...mockEvents[0], currentParticipations: 60, maxParticipations: 50 };
    expect(component.getParticipantPercent(event)).toBe(100);
  });

  it('canPublish should return true only for DRAFT events', () => {
    expect(component.canPublish({ ...mockEvents[0], status: 'DRAFT' })).toBeTrue();
    expect(component.canPublish({ ...mockEvents[0], status: 'PUBLISHED' })).toBeFalse();
  });

  it('canCancel should return true for active statuses', () => {
    expect(component.canCancel({ ...mockEvents[0], status: 'PUBLISHED' })).toBeTrue();
    expect(component.canCancel({ ...mockEvents[0], status: 'COMPLETED' })).toBeFalse();
  });

  it('should call deleteEvent and reload on delete', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    eventServiceSpy.deleteEvent.and.returnValue(of(undefined));

    component.deleteEvent(1);

    expect(eventServiceSpy.deleteEvent).toHaveBeenCalledWith(1);
    expect(eventServiceSpy.getAllEvents).toHaveBeenCalledTimes(2);
  });

  it('should not delete if user cancels confirm', () => {
    spyOn(window, 'confirm').and.returnValue(false);

    component.deleteEvent(1);

    expect(eventServiceSpy.deleteEvent).not.toHaveBeenCalled();
  });

  it('should call changeStatus with correct params', () => {
    eventServiceSpy.changeStatus.and.returnValue(of({}));

    component.changeStatus(mockEvents[0], 'PUBLISHED');

    expect(eventServiceSpy.changeStatus).toHaveBeenCalledWith(1, 'PUBLISHED', 1);
  });

  it('should compute events stats correctly', () => {
    component.events = mockEvents;
    const stats = component.getEventsStats();
    expect(stats[0].value).toBe(1); // Total
    expect(stats[1].value).toBe(1); // Publiés
  });

  it('should not submit if form is invalid', () => {
    component.eventForm.reset();
    component.onSubmit();
    expect(eventServiceSpy.createEvent).not.toHaveBeenCalled();
  });
});
