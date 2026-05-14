/// <reference types="jasmine" />
import { TestBed } from '@angular/core/testing';
import { PermissionService } from './permission.service';
import { AuthService } from './auth.service';
import { Role } from '../enums/role.enum';

describe('PermissionService', () => {
  let service: PermissionService;
  let authSpy: jasmine.SpyObj<AuthService>;

  const learner = { userId: 1, role: 'LEARNER', email: 'l@t.com', firstName: 'L', token: 't', message: '' };
  const admin   = { userId: 2, role: 'ADMIN',   email: 'a@t.com', firstName: 'A', token: 't', message: '' };
  const trainer = { userId: 3, role: 'TRAINER', email: 'tr@t.com', firstName: 'T', token: 't', message: '' };

  beforeEach(() => {
    authSpy = jasmine.createSpyObj('AuthService', ['getUserInfo']);
    TestBed.configureTestingModule({
      providers: [PermissionService, { provide: AuthService, useValue: authSpy }]
    });
    service = TestBed.inject(PermissionService);
  });

  it('LEARNER should be learner', () => {
    authSpy.getUserInfo.and.returnValue(learner as any);
    expect(service.isLearner()).toBeTrue();
  });

  it('ADMIN should have USERS_VIEW via hasRole', () => {
    authSpy.getUserInfo.and.returnValue(admin as any);
    expect(service.hasRole(Role.ADMIN)).toBeTrue();
  });

  it('LEARNER should NOT have ADMIN role', () => {
    authSpy.getUserInfo.and.returnValue(learner as any);
    expect(service.hasRole(Role.ADMIN)).toBeFalse();
  });

  it('should return false when not authenticated', () => {
    authSpy.getUserInfo.and.returnValue(null);
    expect(service.hasRole(Role.LEARNER)).toBeFalse();
  });

  it('hasAnyPermission — true if role matches', () => {
    authSpy.getUserInfo.and.returnValue(admin as any);
    expect(service.hasAnyPermission([Role.ADMIN, Role.TRAINER])).toBeTrue();
  });

  it('hasAnyPermission — false if role does not match', () => {
    authSpy.getUserInfo.and.returnValue(learner as any);
    expect(service.hasAnyPermission([Role.ADMIN, Role.TRAINER])).toBeFalse();
  });

  it('hasRole — true for correct role', () => {
    authSpy.getUserInfo.and.returnValue(trainer as any);
    expect(service.hasRole(Role.TRAINER)).toBeTrue();
  });

  it('hasRole — false for wrong role', () => {
    authSpy.getUserInfo.and.returnValue(learner as any);
    expect(service.hasRole(Role.ADMIN)).toBeFalse();
  });

  it('isAdmin — true for ADMIN', () => {
    authSpy.getUserInfo.and.returnValue(admin as any);
    expect(service.isAdmin()).toBeTrue();
  });

  it('isAdmin — false for LEARNER', () => {
    authSpy.getUserInfo.and.returnValue(learner as any);
    expect(service.isAdmin()).toBeFalse();
  });

  it('isTrainer — true for TRAINER', () => {
    authSpy.getUserInfo.and.returnValue(trainer as any);
    expect(service.isTrainer()).toBeTrue();
  });

  it('canCreate — true for TRAINER', () => {
    authSpy.getUserInfo.and.returnValue(trainer as any);
    expect(service.canCreate()).toBeTrue();
  });

  it('canCreate — false for LEARNER', () => {
    authSpy.getUserInfo.and.returnValue(learner as any);
    expect(service.canCreate()).toBeFalse();
  });

  it('canApplyToJobs — true for LEARNER', () => {
    authSpy.getUserInfo.and.returnValue(learner as any);
    expect(service.canApplyToJobs()).toBeTrue();
  });

  it('isRH — false for LEARNER', () => {
    authSpy.getUserInfo.and.returnValue(learner as any);
    expect(service.isRH()).toBeFalse();
  });
});
