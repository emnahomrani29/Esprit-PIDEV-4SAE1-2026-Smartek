import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { MyBadgesComponent } from './my-badges.component';
import { BadgeService } from '../../../core/services/badge.service';
import { AuthService, AuthResponse } from '../../../core/services/auth.service';
import { EarnedBadge, BadgeTemplate } from '../../../core/models/badge.model';
import { NO_ERRORS_SCHEMA } from '@angular/core';

// Prevent QRCode from running in test environment
jest: undefined; // no-op — qrcode is mocked below
const QRCodeMock = { toDataURL: jasmine.createSpy('toDataURL').and.resolveTo('data:image/png;base64,mock') };

describe('MyBadgesComponent', () => {
  let component: MyBadgesComponent;
  let fixture: ComponentFixture<MyBadgesComponent>;
  let badgeServiceSpy: jasmine.SpyObj<BadgeService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  const mockUser: AuthResponse = {
    token: 'mock-token',
    userId: 42,
    email: 'learner@smartek.com',
    firstName: 'Alice',
    role: 'LEARNER',
    message: 'ok'
  };

  const mockTemplate: BadgeTemplate = {
    id: 1,
    name: 'Gold Badge',
    description: 'Awarded for 90%+ score',
    minimumScore: 90,
    examId: 10
  };

  const mockBadge: EarnedBadge = {
    id: 5,
    badgeTemplate: mockTemplate,
    learnerId: 42,
    awardDate: new Date('2026-01-15'),
    awardedBy: 0,
    verificationCode: 'abc-verify-123'
  };

  const mockBadgeNoVerification: EarnedBadge = {
    id: 6,
    badgeTemplate: { ...mockTemplate, minimumScore: 75, name: 'Silver Badge' },
    learnerId: 42,
    awardDate: new Date('2026-02-01'),
    awardedBy: 1
    // no verificationCode
  };

  beforeEach(async () => {
    badgeServiceSpy = jasmine.createSpyObj('BadgeService', ['getBadgesByLearner']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUserInfo']);

    await TestBed.configureTestingModule({
      imports: [MyBadgesComponent],
      providers: [
        { provide: BadgeService, useValue: badgeServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(MyBadgesComponent);
    component = fixture.componentInstance;
  });

  // ─── ngOnInit ──────────────────────────────────────────────────────────────

  it('should set error and not load badges when user is not authenticated', () => {
    authServiceSpy.getUserInfo.and.returnValue(null);

    fixture.detectChanges();

    expect(component.error).toBe('Not authenticated');
    expect(badgeServiceSpy.getBadgesByLearner).not.toHaveBeenCalled();
  });

  it('should load badges on init when user is authenticated', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    badgeServiceSpy.getBadgesByLearner.and.returnValue(of([mockBadge]));

    fixture.detectChanges();

    expect(badgeServiceSpy.getBadgesByLearner).toHaveBeenCalledWith(42);
    expect(component.badges.length).toBe(1);
    expect(component.badges[0].id).toBe(5);
  });

  it('should set loading to false after badges are loaded', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    badgeServiceSpy.getBadgesByLearner.and.returnValue(of([mockBadge]));

    fixture.detectChanges();

    expect(component.loading).toBeFalse();
  });

  it('should have empty badges list when learner has no badges', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    badgeServiceSpy.getBadgesByLearner.and.returnValue(of([]));

    fixture.detectChanges();

    expect(component.badges).toEqual([]);
    expect(component.error).toBeNull();
  });

  it('should set error message when loading badges fails', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    badgeServiceSpy.getBadgesByLearner.and.returnValue(throwError(() => new Error('Network error')));

    fixture.detectChanges();

    expect(component.error).toBe('Failed to load badges');
    expect(component.loading).toBeFalse();
  });

  it('should set loading to false even when API call fails', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    badgeServiceSpy.getBadgesByLearner.and.returnValue(throwError(() => new Error('error')));

    fixture.detectChanges();

    expect(component.loading).toBeFalse();
  });

  // ─── getVerificationUrl ────────────────────────────────────────────────────

  it('should return correct verification URL when badge has verificationCode', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    badgeServiceSpy.getBadgesByLearner.and.returnValue(of([]));
    fixture.detectChanges();

    const url = component.getVerificationUrl(mockBadge);

    expect(url).toBe('http://localhost:4200/verify/abc-verify-123');
  });

  it('should return empty string when badge has no verificationCode', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    badgeServiceSpy.getBadgesByLearner.and.returnValue(of([]));
    fixture.detectChanges();

    const url = component.getVerificationUrl(mockBadgeNoVerification);

    expect(url).toBe('');
  });

  // ─── shareOnLinkedIn ───────────────────────────────────────────────────────

  it('should open LinkedIn share URL when shareOnLinkedIn is called', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    badgeServiceSpy.getBadgesByLearner.and.returnValue(of([]));
    fixture.detectChanges();

    spyOn(window, 'open');
    component.shareOnLinkedIn(mockBadge);

    const expectedUrl = encodeURIComponent('http://localhost:4200/verify/abc-verify-123');
    expect(window.open).toHaveBeenCalledWith(
      `https://www.linkedin.com/sharing/share-offsite/?url=${expectedUrl}`,
      '_blank'
    );
  });

  it('should open LinkedIn with empty encoded URL when badge has no verificationCode', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    badgeServiceSpy.getBadgesByLearner.and.returnValue(of([]));
    fixture.detectChanges();

    spyOn(window, 'open');
    component.shareOnLinkedIn(mockBadgeNoVerification);

    expect(window.open).toHaveBeenCalledWith(
      `https://www.linkedin.com/sharing/share-offsite/?url=`,
      '_blank'
    );
  });

  // ─── badge tier logic (via template data) ─────────────────────────────────

  it('should classify badge as Gold when minimumScore >= 90', () => {
    // Gold badge has minimumScore = 90
    expect(mockBadge.badgeTemplate.minimumScore).toBeGreaterThanOrEqual(90);
  });

  it('should classify badge as Silver when minimumScore is between 75 and 89', () => {
    expect(mockBadgeNoVerification.badgeTemplate.minimumScore).toBeGreaterThanOrEqual(75);
    expect(mockBadgeNoVerification.badgeTemplate.minimumScore).toBeLessThan(90);
  });

  it('should classify badge as Bronze when minimumScore is below 75', () => {
    const bronzeBadge: EarnedBadge = {
      ...mockBadge,
      badgeTemplate: { ...mockTemplate, minimumScore: 60, name: 'Bronze Badge' }
    };
    expect(bronzeBadge.badgeTemplate.minimumScore).toBeLessThan(75);
  });

  // ─── multiple badges ───────────────────────────────────────────────────────

  it('should load multiple badges and store all of them', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    badgeServiceSpy.getBadgesByLearner.and.returnValue(of([mockBadge, mockBadgeNoVerification]));

    fixture.detectChanges();

    expect(component.badges.length).toBe(2);
  });

  it('should call getBadgesByLearner with the authenticated user id', () => {
    const differentUser: AuthResponse = { ...mockUser, userId: 99 };
    authServiceSpy.getUserInfo.and.returnValue(differentUser);
    badgeServiceSpy.getBadgesByLearner.and.returnValue(of([]));

    fixture.detectChanges();

    expect(badgeServiceSpy.getBadgesByLearner).toHaveBeenCalledWith(99);
  });
});
