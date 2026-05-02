import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { EventService, EventRequest, EventResponse } from './event.service';
import { environment } from '../../../environments/environment';

/**
 * Tests unitaires pour EventService.
 * Vérifie les appels HTTP : CRUD événements, inscriptions, paiements.
 */
describe('EventService', () => {
  let service: EventService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/events`;

  const mockEvent: EventResponse = {
    eventId: 1,
    title: 'Formation DevOps',
    description: 'Sprint 3',
    startDate: '2026-05-01T09:00:00',
    endDate: '2026-05-01T17:00:00',
    location: 'Tunis',
    physicalCapacity: 50,
    onlineCapacity: 0,
    physicalRegistered: 0,
    onlineRegistered: 0,
    maxParticipations: 50,
    currentParticipations: 0,
    status: 'PUBLISHED',
    mode: 'PHYSICAL',
    price: 0,
    isPaid: false,
    createdBy: 1,
    isAvailable: true,
    createdAt: '2026-04-01T00:00:00',
    updatedAt: '2026-04-01T00:00:00'
  };

  const mockRequest: EventRequest = {
    title: 'Formation DevOps',
    startDate: '2026-05-01T09:00:00',
    endDate: '2026-05-01T17:00:00',
    location: 'Tunis',
    maxParticipations: 50
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [EventService]
    });
    service = TestBed.inject(EventService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // ─── getAllEvents ─────────────────────────────────────────────────────────
  describe('getAllEvents()', () => {
    it('should return all events via GET', () => {
      service.getAllEvents().subscribe(events => {
        expect(events.length).toBe(1);
        expect(events[0].title).toBe('Formation DevOps');
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('GET');
      req.flush([mockEvent]);
    });

    it('should return empty array when no events exist', () => {
      service.getAllEvents().subscribe(events => {
        expect(events).toEqual([]);
      });

      const req = httpMock.expectOne(apiUrl);
      req.flush([]);
    });
  });

  // ─── getUpcomingEvents ────────────────────────────────────────────────────
  describe('getUpcomingEvents()', () => {
    it('should return upcoming events via GET /upcoming', () => {
      service.getUpcomingEvents().subscribe(events => {
        expect(events.length).toBe(1);
        expect(events[0].status).toBe('PUBLISHED');
      });

      const req = httpMock.expectOne(`${apiUrl}/upcoming`);
      expect(req.request.method).toBe('GET');
      req.flush([mockEvent]);
    });
  });

  // ─── getEventById ─────────────────────────────────────────────────────────
  describe('getEventById()', () => {
    it('should return an event by ID via GET', () => {
      service.getEventById(1).subscribe(event => {
        expect(event.eventId).toBe(1);
        expect(event.title).toBe('Formation DevOps');
      });

      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockEvent);
    });
  });

  // ─── createEvent ──────────────────────────────────────────────────────────
  describe('createEvent()', () => {
    it('should create an event via POST', () => {
      service.createEvent(mockRequest).subscribe(event => {
        expect(event.eventId).toBe(1);
        expect(event.title).toBe('Formation DevOps');
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockRequest);
      req.flush(mockEvent);
    });
  });

  // ─── updateEvent ──────────────────────────────────────────────────────────
  describe('updateEvent()', () => {
    it('should update an event via PUT', () => {
      const updated = { ...mockRequest, title: 'Formation DevOps Mis à jour' };

      service.updateEvent(1, updated).subscribe(event => {
        expect(event.eventId).toBe(1);
      });

      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(updated);
      req.flush(mockEvent);
    });
  });

  // ─── deleteEvent ──────────────────────────────────────────────────────────
  describe('deleteEvent()', () => {
    it('should delete an event via DELETE', () => {
      service.deleteEvent(1).subscribe(() => {
        // DELETE réussi
      });

      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });

  // ─── changeStatus ─────────────────────────────────────────────────────────
  describe('changeStatus()', () => {
    it('should change event status via POST', () => {
      service.changeStatus(1, 'PUBLISHED', 10, 'Prêt').subscribe(result => {
        expect(result).toBeTruthy();
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/events/business/1/status`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body.newStatus).toBe('PUBLISHED');
      expect(req.request.body.changedBy).toBe(10);
      req.flush({ status: 'PUBLISHED' });
    });
  });

  // ─── registerForEvent ─────────────────────────────────────────────────────
  describe('registerForEvent()', () => {
    it('should register a user for an event via POST', () => {
      const mockRegistration = {
        registrationId: 1,
        eventId: 1,
        userId: 5,
        status: 'CONFIRMED',
        paymentStatus: 'PAID',
        participationMode: 'PHYSICAL'
      };

      service.registerForEvent(1, 5, 'PHYSICAL').subscribe(reg => {
        expect(reg.status).toBe('CONFIRMED');
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/events/business/register`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body.eventId).toBe(1);
      expect(req.request.body.userId).toBe(5);
      expect(req.request.body.participationMode).toBe('PHYSICAL');
      req.flush(mockRegistration);
    });
  });

  // ─── getUserRegistrations ─────────────────────────────────────────────────
  describe('getUserRegistrations()', () => {
    it('should return user registrations via GET', () => {
      const mockRegs = [{ registrationId: 1, eventId: 1, userId: 5, status: 'CONFIRMED' }];

      service.getUserRegistrations(5).subscribe(regs => {
        expect(regs.length).toBe(1);
        expect(regs[0].userId).toBe(5);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/events/business/user/5/registrations`);
      expect(req.request.method).toBe('GET');
      req.flush(mockRegs);
    });
  });

  // ─── cancelRegistration ───────────────────────────────────────────────────
  describe('cancelRegistration()', () => {
    it('should cancel a registration via DELETE', () => {
      service.cancelRegistration(1, 5).subscribe();

      const req = httpMock.expectOne(`${environment.apiUrl}/events/business/registrations/1?userId=5`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });

  // ─── getEventRevenue ──────────────────────────────────────────────────────
  describe('getEventRevenue()', () => {
    it('should return event revenue via GET', () => {
      const mockRevenue = {
        eventId: 1,
        totalRevenue: 500,
        potentialRevenue: 1000,
        paidRegistrations: 5,
        pendingPayments: 5,
        averageRevenuePerParticipant: 100
      };

      service.getEventRevenue(1).subscribe(revenue => {
        expect(revenue.totalRevenue).toBe(500);
        expect(revenue.paidRegistrations).toBe(5);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/events/business/1/revenue`);
      expect(req.request.method).toBe('GET');
      req.flush(mockRevenue);
    });
  });

  // ─── confirmPayment ───────────────────────────────────────────────────────
  describe('confirmPayment()', () => {
    it('should confirm payment via POST', () => {
      service.confirmPayment(1).subscribe(result => {
        expect(result).toBeTruthy();
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/events/business/registrations/1/confirm-payment`);
      expect(req.request.method).toBe('POST');
      req.flush({ paymentStatus: 'PAID' });
    });
  });
});
