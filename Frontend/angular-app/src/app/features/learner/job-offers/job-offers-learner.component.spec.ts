import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { of, throwError } from 'rxjs';
import { JobOffersLearnerComponent } from './job-offers-learner.component';
import { OfferService } from '../../../core/services/offer.service';
import { ApplicationService } from '../../../core/services/application.service';
import { AuthService } from '../../../core/services/auth.service';
import { Offer } from '../../../core/models/offer.model';
import { Application } from '../../../core/models/application.model';

describe('JobOffersLearnerComponent', () => {
  let component: JobOffersLearnerComponent;
  let fixture: ComponentFixture<JobOffersLearnerComponent>;
  let offerServiceSpy: jasmine.SpyObj<OfferService>;
  let applicationServiceSpy: jasmine.SpyObj<ApplicationService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  const mockOffers: Offer[] = [
    {
      id: 1, title: 'Dev Java Senior', description: 'Poste Java',
      companyName: 'TechCorp', location: 'Paris', contractType: 'CDI',
      companyId: 10, status: 'ACTIVE'
    },
    {
      id: 2, title: 'Data Scientist', description: 'ML/AI',
      companyName: 'DataCo', location: 'Lyon', contractType: 'CDD',
      companyId: 11, status: 'ACTIVE'
    }
  ];

  const mockApplications: Application[] = [
    {
      id: 1, offerId: 1,
      learnerId: 5, learnerName: 'Alice', learnerEmail: 'alice@test.com',
      status: 'PENDING', appliedAt: '2026-01-01T10:00:00'
    }
  ];

  const mockUserInfo = {
    token: 'jwt-token', userId: 5, email: 'alice@test.com',
    firstName: 'Alice', role: 'LEARNER', experience: 3, message: 'OK'
  };

  beforeEach(async () => {
    offerServiceSpy = jasmine.createSpyObj('OfferService', ['getAllOffers', 'getOffersByStatus']);
    applicationServiceSpy = jasmine.createSpyObj('ApplicationService', [
      'getApplicationsByLearner', 'applyToOffer', 'updateApplicationStatus'
    ]);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUserInfo', 'isAuthenticated', 'fetchUserData']);

    offerServiceSpy.getAllOffers.and.returnValue(of(mockOffers));
    applicationServiceSpy.getApplicationsByLearner.and.returnValue(of(mockApplications));
    authServiceSpy.getUserInfo.and.returnValue(mockUserInfo);
    authServiceSpy.isAuthenticated.and.returnValue(true);

    await TestBed.configureTestingModule({
      imports: [JobOffersLearnerComponent, CommonModule, FormsModule, RouterModule.forRoot([])],
      providers: [
        { provide: OfferService, useValue: offerServiceSpy },
        { provide: ApplicationService, useValue: applicationServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(JobOffersLearnerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // ─── Initialisation ────────────────────────────────────────────────────────

  describe('ngOnInit()', () => {
    it('should load user info from AuthService', () => {
      expect(component.currentUserId).toBe(5);
    });

    it('should load offers on init', () => {
      expect(offerServiceSpy.getAllOffers).toHaveBeenCalled();
      expect(component.offers.length).toBe(2);
    });

    it('should load user applications and build applicationStatusMap', () => {
      expect(applicationServiceSpy.getApplicationsByLearner).toHaveBeenCalledWith(5);
      expect(component.applicationStatusMap.size).toBe(1);
      expect(component.applicationStatusMap.get(1)).toBe('PENDING');
    });

    it('should apply filters after loading offers', () => {
      expect(component.filteredOffers.length).toBe(2);
    });
  });

  // ─── Filtres ───────────────────────────────────────────────────────────────

  describe('applyFilters()', () => {
    it('should filter by contract type CDI', () => {
      component.filterContractType = 'CDI';
      component.applyFilters();
      expect(component.filteredOffers.length).toBe(1);
      expect(component.filteredOffers[0].contractType).toBe('CDI');
    });

    it('should filter by search term (title)', () => {
      component.searchTerm = 'java';
      component.applyFilters();
      expect(component.filteredOffers.length).toBe(1);
      expect(component.filteredOffers[0].title).toContain('Java');
    });

    it('should filter by search term (company name)', () => {
      component.searchTerm = 'dataco';
      component.applyFilters();
      expect(component.filteredOffers.length).toBe(1);
      expect(component.filteredOffers[0].companyName).toBe('DataCo');
    });

    it('should return all offers when no filter applied', () => {
      component.filterContractType = 'ALL';
      component.searchTerm = '';
      component.applyFilters();
      expect(component.filteredOffers.length).toBe(2);
    });

    it('should return empty array when no match', () => {
      component.searchTerm = 'xyz-inexistant';
      component.applyFilters();
      expect(component.filteredOffers.length).toBe(0);
    });
  });

  // ─── hasApplied / getApplicationStatus ────────────────────────────────────

  describe('hasApplied() / getApplicationStatus()', () => {
    it('should return true for offer already applied', () => {
      expect(component.hasApplied(1)).toBeTrue();
    });

    it('should return false for offer not applied', () => {
      expect(component.hasApplied(999)).toBeFalse();
    });

    it('should return the status for a given offerId', () => {
      expect(component.getApplicationStatus(1)).toBe('PENDING');
    });

    it('should return null for unknown offerId', () => {
      expect(component.getApplicationStatus(999)).toBeNull();
    });
  });

  // ─── Modals ────────────────────────────────────────────────────────────────

  describe('viewDetails() / closeDetailModal()', () => {
    it('should open detail modal with selected offer', () => {
      component.viewDetails(mockOffers[0]);
      expect(component.showDetailModal).toBeTrue();
      expect(component.selectedOffer?.id).toBe(1);
    });

    it('should close detail modal and clear selection', () => {
      component.viewDetails(mockOffers[0]);
      component.closeDetailModal();
      expect(component.showDetailModal).toBeFalse();
      expect(component.selectedOffer).toBeNull();
    });
  });

  describe('applyToOffer() / closeApplicationModal()', () => {
    it('should open application modal and reset form', () => {
      component.applyToOffer(mockOffers[0]);
      expect(component.showApplicationModal).toBeTrue();
      expect(component.selectedOffer?.id).toBe(1);
      expect(component.coverLetter).toBe('');
    });

    it('should close detail modal when opening apply modal', () => {
      component.showDetailModal = true;
      component.applyToOffer(mockOffers[0]);
      expect(component.showDetailModal).toBeFalse();
    });

    it('should close application modal and clear state', () => {
      component.applyToOffer(mockOffers[0]);
      component.closeApplicationModal();
      expect(component.showApplicationModal).toBeFalse();
      expect(component.selectedOffer).toBeNull();
    });
  });

  // ─── submitApplication ─────────────────────────────────────────────────────

  describe('submitApplication()', () => {
    it('should show error if no CV file selected', () => {
      component.selectedOffer = mockOffers[0];
      component.cvFile = null;
      component.submitApplication();
      expect(component.errorMessage).toContain('CV');
    });

    it('should not call applyToOffer if no CV', () => {
      component.selectedOffer = mockOffers[0];
      component.cvFile = null;
      component.submitApplication();
      expect(applicationServiceSpy.applyToOffer).not.toHaveBeenCalled();
    });
  });
});
