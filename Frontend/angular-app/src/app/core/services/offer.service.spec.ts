import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { OfferService } from './offer.service';
import { environment } from '../../../environments/environment';

describe('OfferService', () => {
  let service: OfferService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/offers`;

  const mockOffer = {
    id: 1, title: 'Dev Java', description: 'Poste Java',
    companyName: 'TechCorp', location: 'Paris', contractType: 'CDI',
    companyId: 1, status: 'ACTIVE'
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

  it('should GET all offers', () => {
    service.getAllOffers().subscribe(offers => {
      expect(offers.length).toBe(1);
      expect(offers[0].title).toBe('Dev Java');
    });
    httpMock.expectOne(`${apiUrl}?page=0&size=100&sortBy=createdAt&sortDir=desc`).flush([mockOffer]);
  });

  it('should GET offer by id', () => {
    service.getOfferById(1).subscribe(offer => {
      expect(offer.id).toBe(1);
    });
    httpMock.expectOne(`${apiUrl}/1`).flush(mockOffer);
  });

  it('should GET offers by company', () => {
    service.getOffersByCompanyId(1).subscribe(offers => {
      expect(offers.length).toBe(1);
    });
    httpMock.expectOne(`${apiUrl}/company/1`).flush([mockOffer]);
  });

  it('should GET offers by status', () => {
    service.getOffersByStatus('ACTIVE').subscribe(offers => {
      expect(offers[0].status).toBe('ACTIVE');
    });
    httpMock.expectOne(`${apiUrl}/status/ACTIVE`).flush([mockOffer]);
  });

  it('should DELETE offer', () => {
    service.deleteOffer(1).subscribe(() => {});
    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
