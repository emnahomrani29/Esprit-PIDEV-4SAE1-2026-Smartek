/// <reference types="jasmine" />
import { TestBed } from '@angular/core/testing';
import { PermissionService } from './permission.service';
import { AuthService } from './auth.service';
import { Permission } from '../enums/permission.enum';
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

  it('LEARNER should have CERTIFICATIONS_VIEW', () => {
    authSpy.getUserInfo.and.returnValue(learner as any);
    expect(service.hasPermission(Permission.CERTIFICATIONS_VIEW)).toBeTrue();
  });

  it('ADMIN should have USERS_VIEW', () => {
    authSpy.getUserInfo.and.returnValue(admin as any);
    expect(service.hasPermission(Permission.USERS_VIEW)).toBeTrue();
  });

  it('LEARNER should NOT have USERS_VIEW', () => {
    authSpy.getUserInfo.and.returnValue(learner as any);
    expect(service.hasPermission(Permission.USERS_VIEW)).toBeFalse();
  });

  it('should return false when not authenticated', () => {
    authSpy.getUserInfo.and.returnValue(null);
    expect(service.hasPermission(Permission.CERTIFICATIONS_VIEW)).toBeFalse();
  });

  it('hasAnyPermission — true if has at least one', () => {
    authSpy.getUserInfo.and.returnValue(admin as any);
    expect(service.hasAnyPermission([Permission.USERS_VIEW, Permission.CERTIFICATIONS_VIEW])).toBeTrue();
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

  it('getUserPermissions — returns empty array when not authenticated', () => {
    authSpy.getUserInfo.and.returnValue(null);
    expect(service.getUserPermissions()).toEqual([]);
  });
});
