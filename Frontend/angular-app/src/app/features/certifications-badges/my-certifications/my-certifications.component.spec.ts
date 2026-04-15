import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { MyCertificationsComponent } from './my-certifications.component';
import { CertificationService, PageResponse } from '../../../core/services/certification.service';
import { AuthService, AuthResponse } from '../../../core/services/auth.service';
import { EarnedCertification, CertificationTemplate } from '../../../core/models/certification.model';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('MyCertificationsComponent', () => {
  let component: MyCertificationsComponent;
  let fixture: ComponentFixture<MyCertificationsComponent>;
  let certServiceSpy: jasmine.SpyObj<CertificationService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockUser: AuthResponse = {
    token: 'mock-token',
    userId: 42,
    email: 'learner@smartek.com',
    firstName: 'Alice',
    role: 'LEARNER',
    message: 'ok'
  };

  const mockTemplate: CertificationTemplate = {
    id: 1,
    title: 'Spring Boot Certification',
    description: 'For Spring Boot developers'
  };

  const mockCert: EarnedCertification = {
    id: 10,
    certificationTemplate: mockTemplate,
    learnerId: 42,
    issueDate: '2026-01-15',
    expiryDate: '2028-01-15',
    awardedBy: 0
  };

  const mockPageResponse: PageResponse<EarnedCertification> = {
    content: [mockCert],
    totalElements: 1,
    totalPages: 1,
    size: 10,
    number: 0
  };

  const emptyPageResponse: PageResponse<EarnedCertification> = {
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 10,
    number: 0
  };

  beforeEach(async () => {
    certServiceSpy = jasmine.createSpyObj('CertificationService', [
      'getCertificationsByLearnerPaginated',
      'getAllTemplates',
      'awardCertification'
    ]);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUserInfo']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [MyCertificationsComponent],
      providers: [
        { provide: CertificationService, useValue: certServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA] // ignore child components (PaginationComponent)
    }).compileComponents();

    fixture = TestBed.createComponent(MyCertificationsComponent);
    component = fixture.componentInstance;
  });

  // ─── ngOnInit ──────────────────────────────────────────────────────────────

  it('should set error and not load certifications when user is not authenticated', () => {
    authServiceSpy.getUserInfo.and.returnValue(null);

    fixture.detectChanges();

    expect(component.error).toBe('Not authenticated');
    expect(certServiceSpy.getCertificationsByLearnerPaginated).not.toHaveBeenCalled();
  });

  it('should load certifications on init when user is authenticated', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    certServiceSpy.getCertificationsByLearnerPaginated.and.returnValue(of(mockPageResponse));

    fixture.detectChanges();

    expect(certServiceSpy.getCertificationsByLearnerPaginated).toHaveBeenCalledWith(42, 0, 10, 'issueDate', 'DESC');
    expect(component.certifications.length).toBe(1);
    expect(component.certifications[0].id).toBe(10);
  });

  it('should set loading to false after certifications are loaded', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    certServiceSpy.getCertificationsByLearnerPaginated.and.returnValue(of(mockPageResponse));

    fixture.detectChanges();

    expect(component.loading).toBeFalse();
  });

  it('should update pageInfo after loading certifications', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    certServiceSpy.getCertificationsByLearnerPaginated.and.returnValue(of(mockPageResponse));

    fixture.detectChanges();

    expect(component.pageInfo.totalElements).toBe(1);
    expect(component.pageInfo.totalPages).toBe(1);
  });

  it('should set error message when loading certifications fails', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    certServiceSpy.getCertificationsByLearnerPaginated.and.returnValue(throwError(() => new Error('Network error')));

    fixture.detectChanges();

    expect(component.error).toBe('Failed to load certifications');
    expect(component.loading).toBeFalse();
  });

  it('should have empty certifications list when API returns empty page', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    certServiceSpy.getCertificationsByLearnerPaginated.and.returnValue(of(emptyPageResponse));

    fixture.detectChanges();

    expect(component.certifications).toEqual([]);
  });

  // ─── onPageChange ──────────────────────────────────────────────────────────

  it('should reload certifications when page changes', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    certServiceSpy.getCertificationsByLearnerPaginated.and.returnValue(of(mockPageResponse));
    fixture.detectChanges(); // triggers init load (call #1)

    certServiceSpy.getCertificationsByLearnerPaginated.calls.reset();
    component.onPageChange(2);

    expect(certServiceSpy.getCertificationsByLearnerPaginated).toHaveBeenCalledOnceWith(42, 2, 10, 'issueDate', 'DESC');
  });

  // ─── onPageSizeChange ──────────────────────────────────────────────────────

  it('should reset to page 0 and reload when page size changes', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    certServiceSpy.getCertificationsByLearnerPaginated.and.returnValue(of(mockPageResponse));
    fixture.detectChanges(); // init load (call #1)

    certServiceSpy.getCertificationsByLearnerPaginated.calls.reset();
    component.pageInfo.page = 3;
    component.onPageSizeChange(25);

    expect(component.pageInfo.page).toBe(0);
    expect(component.pageInfo.size).toBe(25);
    expect(certServiceSpy.getCertificationsByLearnerPaginated).toHaveBeenCalledOnceWith(42, 0, 25, 'issueDate', 'DESC');
  });

  // ─── viewCertificate ───────────────────────────────────────────────────────

  it('should navigate to certificate-viewer when viewCertificate is called', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    certServiceSpy.getCertificationsByLearnerPaginated.and.returnValue(of(emptyPageResponse));
    fixture.detectChanges();

    component.viewCertificate(10);

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/dashboard/certificate-viewer', 10]);
  });

  // ─── awardSample ───────────────────────────────────────────────────────────

  it('should award sample certification and reload list on success', fakeAsync(() => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    certServiceSpy.getCertificationsByLearnerPaginated.and.returnValue(of(emptyPageResponse));
    certServiceSpy.getAllTemplates.and.returnValue(of([mockTemplate]));
    certServiceSpy.awardCertification.and.returnValue(of(mockCert));
    fixture.detectChanges();

    component.awardSample();
    tick();

    expect(certServiceSpy.awardCertification).toHaveBeenCalled();
    expect(component.awarding).toBeFalse();
  }));

  it('should set error when no templates available for awardSample', fakeAsync(() => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    certServiceSpy.getCertificationsByLearnerPaginated.and.returnValue(of(emptyPageResponse));
    certServiceSpy.getAllTemplates.and.returnValue(of([]));
    fixture.detectChanges();

    component.awardSample();
    tick();

    expect(component.error).toBe('No certification templates available to award');
    expect(component.awarding).toBeFalse();
  }));

  it('should set error when awardCertification fails', fakeAsync(() => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    certServiceSpy.getCertificationsByLearnerPaginated.and.returnValue(of(emptyPageResponse));
    certServiceSpy.getAllTemplates.and.returnValue(of([mockTemplate]));
    certServiceSpy.awardCertification.and.returnValue(throwError(() => new Error('Award failed')));
    fixture.detectChanges();

    component.awardSample();
    tick();

    expect(component.error).toBe('Failed to award sample certification');
    expect(component.awarding).toBeFalse();
  }));

  it('should set error when getAllTemplates fails in awardSample', fakeAsync(() => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    certServiceSpy.getCertificationsByLearnerPaginated.and.returnValue(of(emptyPageResponse));
    certServiceSpy.getAllTemplates.and.returnValue(throwError(() => new Error('Templates error')));
    fixture.detectChanges();

    component.awardSample();
    tick();

    expect(component.error).toBe('Failed to load templates');
    expect(component.awarding).toBeFalse();
  }));
});
