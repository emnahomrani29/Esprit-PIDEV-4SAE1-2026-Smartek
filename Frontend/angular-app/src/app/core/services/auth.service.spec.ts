/// <reference types="jasmine" />
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AuthService, AuthResponse, LoginRequest, RegisterRequest } from './auth.service';
import { environment } from '../../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const mockAuthResponse: AuthResponse = {
    token: 'mock-jwt-token',
    userId: 1,
    email: 'test@smartek.com',
    firstName: 'Alice',
    role: 'LEARNER',
    message: 'Login successful'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => { httpMock.verify(); localStorage.clear(); });

  describe('login()', () => {
    it('should POST to /auth/login and save token', () => {
      const request: LoginRequest = { email: 'test@smartek.com', password: 'password123' };
      service.login(request).subscribe(response => {
        expect(response.token).toBe('mock-jwt-token');
      });
      const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
      expect(req.request.method).toBe('POST');
      req.flush(mockAuthResponse);
      expect(localStorage.getItem('token')).toBe('mock-jwt-token');
    });

    it('should save userInfo after login', () => {
      service.login({ email: 'test@smartek.com', password: 'pass' }).subscribe();
      httpMock.expectOne(`${environment.apiUrl}/auth/login`).flush(mockAuthResponse);
      const saved = JSON.parse(localStorage.getItem('userInfo')!);
      expect(saved.role).toBe('LEARNER');
    });
  });

  describe('logout()', () => {
    it('should remove token and userInfo', () => {
      localStorage.setItem('token', 'tok');
      localStorage.setItem('userInfo', JSON.stringify(mockAuthResponse));
      service.logout();
      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('userInfo')).toBeNull();
    });
  });

  describe('isAuthenticated()', () => {
    it('should return true when token exists', () => {
      localStorage.setItem('token', 'valid');
      expect(service.isAuthenticated()).toBeTrue();
    });

    it('should return false when no token', () => {
      expect(service.isAuthenticated()).toBeFalse();
    });
  });

  describe('getUserInfo()', () => {
    it('should return parsed user info', () => {
      localStorage.setItem('userInfo', JSON.stringify(mockAuthResponse));
      expect(service.getUserInfo()?.email).toBe('test@smartek.com');
    });

    it('should return null when empty', () => {
      expect(service.getUserInfo()).toBeNull();
    });

    it('should default experience to 0 when undefined', () => {
      const noExp = { ...mockAuthResponse };
      delete (noExp as any).experience;
      localStorage.setItem('userInfo', JSON.stringify(noExp));
      expect(service.getUserInfo()?.experience).toBe(0);
    });

    it('should return null on invalid JSON', () => {
      localStorage.setItem('userInfo', 'invalid{{{');
      expect(service.getUserInfo()).toBeNull();
    });
  });
});
