import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { LearnerEventsComponent } from './learner-events.component';
import { EventService, EventResponse, EventRegistration } from '../../../core/services/event.service';
import { AuthService } from '../../../core/services/auth.service';

describe('LearnerEventsComponent', () => {
  let component: LearnerEventsComponent;
  let fixture: ComponentFixture<LearnerEventsComponent>;
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

  const mockRegistrations: EventRegistration[] = [
    { registrationId: 1, eventId: 1, userId: 5, status: 'CONFIRMED', paymentStatus: 'PAID', participationMode: 'PHYSICAL', registeredAt: '2026-04-01T00:00:00' }
  ];

  beforeEach(async () => {
    eventServiceSpy = jasmine.createSpyObj('EventService', [
      'getAllEvents', 'getUserRegistrations', 'registerForEvent', 'cancelRegistration'
    ]);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUserInfo']);

    eventServiceSpy.getAllEvents.and.returnValue(of(mockEvents));
    eventServiceSpy.getUserRegistrations.and.returnValue(of(mockRegistrations));
    authServiceSpy.getUserInfo.and.returnValue({ userId: 5, email: 'learner@test.com', role: 'LEARNER', token: 'tok', firstName: 'Learner', message: '' });

    await TestBed.configureTestingModule({
      imports: [LearnerEventsComponent, HttpClientTestingModule, FormsModule],
      providers: [
        { provide: EventService, useValue: eventServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: { get: () => null } } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LearnerEventsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load events and filter by visible statuses on init', () => {
    expect(eventServiceSpy.getAllEvents).toHaveBeenCalled();
    expect(component.events.length).toBe(1);
  });

  it('should load user registrations on init', () => {
    expect(eventServiceSpy.getUserRegistrations).toHaveBeenCalledWith(5);
    expect(component.userRegistrations.length).toBe(1);
  });

  it('should detect if user is registered for an event', () => {
    expect(component.isRegistered(1)).toBeTrue();
    expect(component.isRegistered(99)).toBeFalse();
  });

  it('should return registration for a specific event', () => {
    const reg = component.getRegistration(1);
    expect(reg).toBeDefined();
    expect(reg!.status).toBe('CONFIRMED');
  });

  it('should open register modal with correct event', () => {
    component.openRegisterModal(mockEvents[0]);
    expect(component.showRegisterModal).toBeTrue();
    expect(component.selectedEvent).toEqual(mockEvents[0]);
    expect(component.selectedMode).toBe('PHYSICAL');
  });

  it('should close register modal and reset state', () => {
    component.showRegisterModal = true;
    component.selectedEvent = mockEvents[0];
    component.closeRegisterModal();
    expect(component.showRegisterModal).toBeFalse();
    expect(component.selectedEvent).toBeNull();
  });

  it('should apply search filter correctly', () => {
    component.events = mockEvents;
    component.searchTerm = 'DevOps';
    component.applyFilters();
    expect(component.filteredEvents.length).toBe(1);
  });

  it('should return empty filteredEvents when search does not match', () => {
    component.events = mockEvents;
    component.searchTerm = 'XYZ_INEXISTANT';
    component.applyFilters();
    expect(component.filteredEvents.length).toBe(0);
  });

  it('should filter by mode', () => {
    component.events = mockEvents;
    component.filterMode = 'PHYSICAL';
    component.applyFilters();
    expect(component.filteredEvents.length).toBe(1);

    component.filterMode = 'ONLINE';
    component.applyFilters();
    expect(component.filteredEvents.length).toBe(0);
  });

  it('canRegister should return true for PUBLISHED events not yet registered', () => {
    component.userRegistrations = [];
    expect(component.canRegister(mockEvents[0])).toBeTrue();
  });

  it('canRegister should return false if already registered', () => {
    expect(component.canRegister(mockEvents[0])).toBeFalse();
  });

  it('canRegister should return false for non-PUBLISHED events', () => {
    component.userRegistrations = [];
    const completedEvent = { ...mockEvents[0], status: 'COMPLETED' };
    expect(component.canRegister(completedEvent)).toBeFalse();
  });

  it('should return correct status label', () => {
    expect(component.getStatusLabel('PUBLISHED')).toBe('Inscriptions ouvertes');
    expect(component.getStatusLabel('FULL')).toBe('Complet');
    expect(component.getStatusLabel('ONGOING')).toBe('En cours');
  });

  it('should return correct mode icon', () => {
    expect(component.getModeIcon('PHYSICAL')).toBe('🏢');
    expect(component.getModeIcon('ONLINE')).toBe('💻');
  });

  it('should calculate participant percent correctly', () => {
    const event = { ...mockEvents[0], currentParticipations: 10, maxParticipations: 50 };
    expect(component.getParticipantPercent(event)).toBe(20);
  });

  it('should count upcoming events correctly', () => {
    component.events = mockEvents;
    expect(component.getUpcomingCount()).toBe(1);
  });

  it('should count ongoing events correctly', () => {
    component.events = [{ ...mockEvents[0], status: 'ONGOING' }];
    expect(component.getOngoingCount()).toBe(1);
  });
});
