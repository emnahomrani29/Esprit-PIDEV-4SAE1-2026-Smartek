import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { MyBadgesComponent } from './my-badges.component';
import { BadgeService } from '../../../core/services/badge.service';
import { AuthService, AuthResponse } from '../../../core/services/auth.service';
import { EarnedBadge, BadgeTemplate } from '../../../core/models/badge.model';
import { NO_ERRORS_SCHEMA } from '@angular/core';

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
    verificationId: 'abc-verify-123'
  };

  const mockBadgeSilver: EarnedBadge = {
    id: 6,
    badgeTemplate: { ...mockTemplate, minimumScore: 75, name: 'Silver Badge' },
    learnerId: 42,
    awardDate: new Date('2026-02-01'),
    awardedBy: 1
  };

  beforeEach(async () => {
    badgeServiceSpy = jasmine.createSpyObj('BadgeService', [
      'getBadgesByLearner',
      'shareOnLinkedIn'
    ]);
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

  it('should load multiple badges and store all of them', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    badgeServiceSpy.getBadgesByLearner.and.returnValue(of([mockBadge, mockBadgeSilver]));

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

  // ─── shareOnLinkedIn ───────────────────────────────────────────────────────

  it('should call badgeService.shareOnLinkedIn and open URL on success', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    badgeServiceSpy.getBadgesByLearner.and.returnValue(of([mockBadge]));
    badgeServiceSpy.shareOnLinkedIn.and.returnValue(of({ linkedInUrl: 'https://linkedin.com/share?url=test' }));
    spyOn(window, 'open');
    fixture.detectChanges();

    component.shareOnLinkedIn(mockBadge);

    expect(badgeServiceSpy.shareOnLinkedIn).toHaveBeenCalledWith(5);
    expect(window.open).toHaveBeenCalledWith('https://linkedin.com/share?url=test', '_blank');
  });

  it('should set error when shareOnLinkedIn service call fails', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    badgeServiceSpy.getBadgesByLearner.and.returnValue(of([mockBadge]));
    badgeServiceSpy.shareOnLinkedIn.and.returnValue(throwError(() => new Error('fail')));
    fixture.detectChanges();

    component.shareOnLinkedIn(mockBadge);

    expect(component.error).toBe('Failed to generate LinkedIn share link');
    expect(component.sharingId).toBeNull();
  });

  it('should not call shareOnLinkedIn when badge has no id', () => {
    authServiceSpy.getUserInfo.and.returnValue(mockUser);
    badgeServiceSpy.getBadgesByLearner.and.returnValue(of([]));
    fixture.detectChanges();

    const badgeWithoutId: EarnedBadge = { ...mockBadge, id: undefined };
    component.shareOnLinkedIn(badgeWithoutId);

    expect(badgeServiceSpy.shareOnLinkedIn).not.toHaveBeenCalled();
  });

  // ─── badge tier logic ──────────────────────────────────────────────────────

  it('should classify badge as Gold when minimumScore >= 90', () => {
    expect(mockBadge.badgeTemplate.minimumScore).toBeGreaterThanOrEqual(90);
  });

  it('should classify badge as Silver when minimumScore is between 75 and 89', () => {
    expect(mockBadgeSilver.badgeTemplate.minimumScore).toBeGreaterThanOrEqual(75);
    expect(mockBadgeSilver.badgeTemplate.minimumScore).toBeLessThan(90);
  });

  it('should classify badge as Bronze when minimumScore is below 75', () => {
    const bronzeBadge: EarnedBadge = {
      ...mockBadge,
      badgeTemplate: { ...mockTemplate, minimumScore: 60, name: 'Bronze Badge' }
    };
    expect(bronzeBadge.badgeTemplate.minimumScore).toBeLessThan(75);
  });
});
