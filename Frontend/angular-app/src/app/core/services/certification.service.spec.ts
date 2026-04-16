/// <reference types="jasmine" />
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CertificationService } from './certification.service';
import { EarnedCertification, CertificationTemplate } from '../models/certification.model';
import { environment } from '../../../environments/environment';

describe('CertificationService', () => {
  let service: CertificationService;
  let httpMock: HttpTestingController;
  // Le service utilise des URLs hardcodées sur le port 8089
  const baseUrl = 'http://localhost:8089/api/certifications-badges';

  const mockTemplate: CertificationTemplate = { id: 1, title: 'Java Expert', description: 'desc' };
  const mockCert: EarnedCertification = {
    id: 10, certificationTemplate: mockTemplate,
    learnerId: 2, issueDate: new Date('2026-01-01'),
    awardedBy: 5, verificationId: 'uuid-1234'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule], providers: [CertificationService] });
    service = TestBed.inject(CertificationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should GET all templates', () => {
    service.getAllTemplates().subscribe(t => expect(t[0].title).toBe('Java Expert'));
    httpMock.expectOne(`${baseUrl}/certification-templates`).flush([mockTemplate]);
  });

  it('should GET certifications by learner', () => {
    service.getCertificationsByLearner(2).subscribe(c => expect(c[0].learnerId).toBe(2));
    httpMock.expectOne(`${baseUrl}/earned-certifications/learner/2`).flush([mockCert]);
  });

  it('should GET paginated certifications with correct params', () => {
    service.getCertificationsByLearnerPaginated(2, 0, 10, 'issueDate', 'DESC').subscribe(page => {
      expect(page.content.length).toBe(1);
    });
    const req = httpMock.expectOne(r =>
      r.url === `${baseUrl}/earned-certifications/learner/2/paginated` &&
      r.params.get('page') === '0'
    );
    req.flush({ content: [mockCert], totalElements: 1, totalPages: 1, size: 10, number: 0 });
  });

  it('should POST to award a certification', () => {
    const request = { certificationTemplateId: 1, learnerId: 2, issueDate: '2026-01-01' };
    service.awardCertification(request).subscribe(c => expect(c.id).toBe(10));
    const req = httpMock.expectOne(`${baseUrl}/earned-certifications`);
    expect(req.request.method).toBe('POST');
    req.flush(mockCert);
  });

  it('should POST and return LinkedIn URL', () => {
    const url = 'https://linkedin.com/profile/add?name=Java+Expert';
    service.shareOnLinkedIn(10).subscribe(res => expect(res.linkedInUrl).toBe(url));
    httpMock.expectOne(`${baseUrl}/earned-certifications/share/linkedin/10`).flush({ linkedInUrl: url });
  });

  it('should DELETE a certification', () => {
    service.revokeCertification(10).subscribe(() => {});
    const req = httpMock.expectOne(`${baseUrl}/earned-certifications/10`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
