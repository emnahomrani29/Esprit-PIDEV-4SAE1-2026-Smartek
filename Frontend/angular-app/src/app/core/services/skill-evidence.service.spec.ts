import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { SkillEvidenceService } from './skill-evidence.service';
import {
  SkillEvidenceRequest, SkillEvidenceResponse,
  ApprovalRequest, RejectionRequest, EvidenceStatus, EvidenceCategory
} from '../models/skill-evidence.model';
import { environment } from '../../../environments/environment';

describe('SkillEvidenceService', () => {
  let service: SkillEvidenceService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/skill-evidence`;

  const mockEvidence: SkillEvidenceResponse = {
    evidenceId: 1, title: 'Java Cert', fileUrl: 'http://file.pdf',
    description: 'Oracle Java', uploadDate: '2026-01-01',
    learnerId: 10, learnerName: 'Alice', learnerEmail: 'alice@test.com',
    status: 'PENDING', category: 'PROGRAMMING'
  };

  const mockRequest: SkillEvidenceRequest = {
    title: 'Java Cert', learnerId: 10,
    learnerName: 'Alice', learnerEmail: 'alice@test.com', category: 'PROGRAMMING'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SkillEvidenceService]
    });
    service = TestBed.inject(SkillEvidenceService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // ===== CRUD =====

  it('should create evidence via POST', () => {
    service.create(mockRequest).subscribe(res => {
      expect(res.evidenceId).toBe(1);
      expect(res.status).toBe('PENDING');
    });
    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockRequest);
    req.flush(mockEvidence);
  });

  it('should get all evidences via GET', () => {
    service.getAll().subscribe(res => {
      expect(res.length).toBe(1);
      expect(res[0].title).toBe('Java Cert');
    });
    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush([mockEvidence]);
  });

  it('should get evidences by learner via GET', () => {
    service.getByLearner(10).subscribe(res => {
      expect(res.length).toBe(1);
    });
    const req = httpMock.expectOne(`${apiUrl}/learner/10`);
    expect(req.request.method).toBe('GET');
    req.flush([mockEvidence]);
  });

  it('should get evidence by id via GET', () => {
    service.getById(1).subscribe(res => {
      expect(res.evidenceId).toBe(1);
    });
    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockEvidence);
  });

  it('should update evidence via PUT', () => {
    const updated = { ...mockEvidence, title: 'Updated' };
    service.update(1, mockRequest).subscribe(res => {
      expect(res.title).toBe('Updated');
    });
    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(updated);
  });

  it('should delete evidence via DELETE', () => {
    service.delete(1).subscribe(res => expect(res).toBeNull());
    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  // ===== LOGIQUE MÉTIER : Validation =====

  it('should approve evidence via POST with score', () => {
    const approvalReq: ApprovalRequest = { score: 85, adminComment: 'Bon travail', reviewerId: 1 };
    const approved = { ...mockEvidence, status: 'APPROVED' as EvidenceStatus, score: 85 };

    service.approve(1, approvalReq).subscribe(res => {
      expect(res.status).toBe('APPROVED');
      expect(res.score).toBe(85);
    });
    const req = httpMock.expectOne(`${apiUrl}/1/approve`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body.score).toBe(85);
    req.flush(approved);
  });

  it('should reject evidence via POST with comment', () => {
    const rejectionReq: RejectionRequest = { adminComment: 'Preuve insuffisante', reviewerId: 1 };
    const rejected = { ...mockEvidence, status: 'REJECTED' as EvidenceStatus };

    service.reject(1, rejectionReq).subscribe(res => {
      expect(res.status).toBe('REJECTED');
    });
    const req = httpMock.expectOne(`${apiUrl}/1/reject`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body.adminComment).toBe('Preuve insuffisante');
    req.flush(rejected);
  });

  // ===== LOGIQUE MÉTIER : Analytics =====

  it('should get learner analytics via GET', () => {
    const analytics = {
      totalCount: 5, approvedCount: 3, pendingCount: 1, rejectedCount: 1,
      averageScore: 80, categoryDistribution: { PROGRAMMING: 3 }, scoreTrend: []
    };
    service.getLearnerAnalytics(10).subscribe(res => {
      expect(res.totalCount).toBe(5);
      expect(res.approvedCount).toBe(3);
      expect(res.averageScore).toBe(80);
    });
    const req = httpMock.expectOne(`${apiUrl}/analytics/learner/10`);
    expect(req.request.method).toBe('GET');
    req.flush(analytics);
  });

  it('should get global analytics via GET', () => {
    const analytics = {
      totalCount: 20, approvedCount: 15, pendingCount: 3, rejectedCount: 2,
      approvalRate: 0.88, averageScore: 75,
      categoryDistribution: {}, submissionTrend: {}, statusDistribution: {}
    };
    service.getGlobalAnalytics().subscribe(res => {
      expect(res.approvalRate).toBe(0.88);
    });
    const req = httpMock.expectOne(`${apiUrl}/analytics/global`);
    req.flush(analytics);
  });

  // ===== LOGIQUE MÉTIER : Filtres =====

  it('should get evidences by status via GET', () => {
    service.getByStatus('PENDING').subscribe(res => {
      expect(res.every(e => e.status === 'PENDING')).toBeTrue();
    });
    const req = httpMock.expectOne(`${apiUrl}/status/PENDING`);
    expect(req.request.method).toBe('GET');
    req.flush([mockEvidence]);
  });

  it('should get evidences by category via GET', () => {
    service.getByCategory('PROGRAMMING').subscribe(res => {
      expect(res[0].category).toBe('PROGRAMMING');
    });
    const req = httpMock.expectOne(`${apiUrl}/category/PROGRAMMING`);
    req.flush([mockEvidence]);
  });

  // ===== Gestion d'erreurs =====

  it('should handle 404 error on getById', () => {
    let errorReceived = false;
    service.getById(999).subscribe({
      next: () => fail('should have failed'),
      error: (err) => { errorReceived = true; expect(err.status).toBe(404); }
    });
    const req = httpMock.expectOne(`${apiUrl}/999`);
    req.flush('Not found', { status: 404, statusText: 'Not Found' });
    expect(errorReceived).toBeTrue();
  });

  it('should handle 500 error on create', () => {
    let errorReceived = false;
    service.create(mockRequest).subscribe({
      next: () => fail('should have failed'),
      error: (err) => { errorReceived = true; expect(err.status).toBe(500); }
    });
    const req = httpMock.expectOne(apiUrl);
    req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });
    expect(errorReceived).toBeTrue();
  });
});
