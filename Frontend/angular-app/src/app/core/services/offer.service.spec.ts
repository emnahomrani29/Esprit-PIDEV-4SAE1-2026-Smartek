/// <reference types="jasmine" />
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { OfferService } from './offer.service';
import { Offer, OfferRequest } from '../models/offer.model';
import { environment } from '../../../environments/environment';

describe('OfferService', () => {
  let service: OfferService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/offers`;

  const mockOffer: Offer = {
    id: 1, title: 'Dev Java', description: 'Poste Java Senior',
    companyName: 'TechCorp', location: 'Paris', contractType: 'CDI',
    companyId: 1, status: 'ACTIVE'
  };

  const mockOffer2: Offer = {
    id: 2, title: 'Data Scientist Python', description: 'Poste Data Science',
    companyName: 'DataCorp', location: 'Lyon', contractType: 'CDI',
    companyId: 2, status: 'ACTIVE'
  };

  const mockOfferRequest: OfferRequest = {
    title: 'Dev Java', description: 'Poste Java Senior',
    companyName: 'TechCorp', location: 'Paris', contractType: 'CDI',
    companyId: 1
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [OfferService]
    });
    service = TestBed.inject(OfferService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // ─── getAllOffers ──────────────────────────────────────────────────────────

  describe('getAllOffers()', () => {
    it('should GET with pagination params and return array response', () => {
      service.getAllOffers().subscribe(offers => {
        expect(offers.length).toBe(2);
        expect(offers[0].title).toBe('Dev Java');
      });

      const req = httpMock.expectOne(`${apiUrl}?page=0&size=100&sortBy=createdAt&sortDir=desc`);
      expect(req.request.method).toBe('GET');
      req.flush([mockOffer, mockOffer2]);
    });

    it('should extract content from paginated response object', () => {
      service.getAllOffers().subscribe(offers => {
        expect(offers.length).toBe(1);
        expect(offers[0].id).toBe(1);
      });

      httpMock.expectOne(`${apiUrl}?page=0&size=100&sortBy=createdAt&sortDir=desc`)
        .flush({ content: [mockOffer], totalElements: 1, totalPages: 1 });
    });

    it('should return empty array when response is empty', () => {
      service.getAllOffers().subscribe(offers => {
        expect(offers).toEqual([]);
      });

      httpMock.expectOne(`${apiUrl}?page=0&size=100&sortBy=createdAt&sortDir=desc`).flush([]);
    });

    it('should return empty array when paginated response has no content', () => {
      service.getAllOffers().subscribe(offers => {
        expect(offers).toEqual([]);
      });

      httpMock.expectOne(`${apiUrl}?page=0&size=100&sortBy=createdAt&sortDir=desc`)
        .flush({ content: [], totalElements: 0 });
    });
  });

  // ─── getOfferById ──────────────────────────────────────────────────────────

  describe('getOfferById()', () => {
    it('should GET /offers/{id} and return the offer', () => {
      service.getOfferById(1).subscribe(offer => {
        expect(offer.id).toBe(1);
        expect(offer.title).toBe('Dev Java');
        expect(offer.status).toBe('ACTIVE');
      });

      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockOffer);
    });

    it('should propagate 404 when offer not found', () => {
      let error: HttpErrorResponse | undefined;
      service.getOfferById(99999).subscribe({
        next: () => fail('Expected error'),
        error: (e: HttpErrorResponse) => { error = e; }
      });

      httpMock.expectOne(`${apiUrl}/99999`).flush(
        { message: 'Offer not found with id: 99999' },
        { status: 404, statusText: 'Not Found' }
      );

      expect(error!.status).toBe(404);
    });
  });

  // ─── getOffersByCompanyId ──────────────────────────────────────────────────

  describe('getOffersByCompanyId()', () => {
    it('should GET /offers/company/{companyId}', () => {
      service.getOffersByCompanyId(1).subscribe(offers => {
        expect(offers.length).toBe(1);
        expect(offers[0].companyId).toBe(1);
      });

      const req = httpMock.expectOne(`${apiUrl}/company/1`);
      expect(req.request.method).toBe('GET');
      req.flush([mockOffer]);
    });

    it('should extract content from paginated response', () => {
      service.getOffersByCompanyId(1).subscribe(offers => {
        expect(offers.length).toBe(1);
      });

      httpMock.expectOne(`${apiUrl}/company/1`)
        .flush({ content: [mockOffer], totalElements: 1 });
    });

    it('should return empty array when company has no offers', () => {
      service.getOffersByCompanyId(999).subscribe(offers => {
        expect(offers).toEqual([]);
      });
      httpMock.expectOne(`${apiUrl}/company/999`).flush([]);
    });
  });

  // ─── getOffersByStatus ─────────────────────────────────────────────────────

  describe('getOffersByStatus()', () => {
    it('should GET /offers/status/ACTIVE', () => {
      service.getOffersByStatus('ACTIVE').subscribe(offers => {
        expect(offers[0].status).toBe('ACTIVE');
      });

      const req = httpMock.expectOne(`${apiUrl}/status/ACTIVE`);
      expect(req.request.method).toBe('GET');
      req.flush([mockOffer]);
    });

    it('should GET /offers/status/CLOSED', () => {
      const closedOffer: Offer = { ...mockOffer, id: 5, status: 'CLOSED' };
      service.getOffersByStatus('CLOSED').subscribe(offers => {
        expect(offers[0].status).toBe('CLOSED');
      });

      httpMock.expectOne(`${apiUrl}/status/CLOSED`).flush([closedOffer]);
    });

    it('should extract content from paginated response', () => {
      service.getOffersByStatus('ACTIVE').subscribe(offers => {
        expect(offers.length).toBe(2);
      });

      httpMock.expectOne(`${apiUrl}/status/ACTIVE`)
        .flush({ content: [mockOffer, mockOffer2], totalElements: 2 });
    });
  });

  // ─── createOffer ───────────────────────────────────────────────────────────

  describe('createOffer()', () => {
    it('should POST to /offers and return the created offer', () => {
      service.createOffer(mockOfferRequest).subscribe(offer => {
        expect(offer.id).toBe(1);
        expect(offer.title).toBe('Dev Java');
        expect(offer.status).toBe('ACTIVE');
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockOfferRequest);
      req.flush(mockOffer);
    });

    it('should propagate 422 when expiration date is in the past', () => {
      const invalidReq: OfferRequest = { ...mockOfferRequest };

      let error: HttpErrorResponse | undefined;
      service.createOffer(invalidReq).subscribe({
        next: () => fail('Expected error'),
        error: (e: HttpErrorResponse) => { error = e; }
      });

      httpMock.expectOne(apiUrl).flush(
        { message: "La date d'expiration ne peut pas être dans le passé" },
        { status: 422, statusText: 'Unprocessable Entity' }
      );

      expect(error!.status).toBe(422);
    });

    it('should propagate 403 when user is not authorized', () => {
      let error: HttpErrorResponse | undefined;
      service.createOffer(mockOfferRequest).subscribe({
        next: () => fail('Expected error'),
        error: (e: HttpErrorResponse) => { error = e; }
      });

      httpMock.expectOne(apiUrl).flush(
        { message: 'Forbidden' },
        { status: 403, statusText: 'Forbidden' }
      );

      expect(error!.status).toBe(403);
    });
  });

  // ─── updateOffer ───────────────────────────────────────────────────────────

  describe('updateOffer()', () => {
    it('should PUT to /offers/{id} and return updated offer', () => {
      const updatedOffer: Offer = { ...mockOffer, title: 'Dev Java Senior' };

      service.updateOffer(1, { ...mockOfferRequest, title: 'Dev Java Senior' }).subscribe(offer => {
        expect(offer.title).toBe('Dev Java Senior');
      });

      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('PUT');
      req.flush(updatedOffer);
    });

    it('should propagate 404 when offer not found', () => {
      let error: HttpErrorResponse | undefined;
      service.updateOffer(99999, mockOfferRequest).subscribe({
        next: () => fail('Expected error'),
        error: (e: HttpErrorResponse) => { error = e; }
      });

      httpMock.expectOne(`${apiUrl}/99999`).flush(
        { message: 'Offer not found with id: 99999' },
        { status: 404, statusText: 'Not Found' }
      );

      expect(error!.status).toBe(404);
    });
  });

  // ─── deleteOffer ───────────────────────────────────────────────────────────

  describe('deleteOffer()', () => {
    it('should DELETE /offers/{id}', () => {
      service.deleteOffer(1).subscribe(() => {});

      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should propagate 422 when offer has accepted applications', () => {
      let error: HttpErrorResponse | undefined;
      service.deleteOffer(1).subscribe({
        next: () => fail('Expected error'),
        error: (e: HttpErrorResponse) => { error = e; }
      });

      httpMock.expectOne(`${apiUrl}/1`).flush(
        { message: 'Impossible de supprimer une offre avec des candidatures acceptées' },
        { status: 422, statusText: 'Unprocessable Entity' }
      );

      expect(error!.status).toBe(422);
    });

    it('should propagate 404 when offer not found', () => {
      let error: HttpErrorResponse | undefined;
      service.deleteOffer(99999).subscribe({
        next: () => fail('Expected error'),
        error: (e: HttpErrorResponse) => { error = e; }
      });

      httpMock.expectOne(`${apiUrl}/99999`).flush(
        { message: 'Offer not found' },
        { status: 404, statusText: 'Not Found' }
      );

      expect(error!.status).toBe(404);
    });
  });
});
