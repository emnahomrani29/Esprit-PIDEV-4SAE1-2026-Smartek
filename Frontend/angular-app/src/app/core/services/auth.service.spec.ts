import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService, LoginRequest, RegisterRequest, AuthResponse } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let routerSpy: jasmine.SpyObj<Router>;

  const BASE = 'http://localhost:8081/api/auth';

  const mockAuthResponse: AuthResponse = {
    token: 'mock-jwt-token',
    userId: 42,
    email: 'learner@smartek.com',
    firstName: 'Alice',
    role: 'LEARNER',
    message: 'Login successful'
  };

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        { provide: Router, useValue: routerSpy }
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  // ─── login ─────────────────────────────────────────────────────────────────

  it('should login and store token in localStorage', () => {
    const request: LoginRequest = { email: 'learner@smartek.com', password: 'pass123' };

    service.login(request).subscribe(result => {
      expect(result.token).toBe('mock-jwt-token');
      expect(localStorage.getItem('token')).toBe('mock-jwt-token');
    });

    const req = httpMock.expectOne(`${BASE}/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockAuthResponse);
  });

  it('should store userInfo in localStorage after login', () => {
    service.login({ email: 'learner@smartek.com', password: 'pass123' }).subscribe(() => {
      const stored = JSON.parse(localStorage.getItem('userInfo')!);
      expect(stored.userId).toBe(42);
      expect(stored.role).toBe('LEARNER');
    });

    httpMock.expectOne(`${BASE}/login`).flush(mockAuthResponse);
  });

  it('should propagate 401 error on invalid credentials', () => {
    service.login({ email: 'wrong@test.com', password: 'wrong' }).subscribe({
      error: err => expect(err.status).toBe(401)
    });

    httpMock.expectOne(`${BASE}/login`).flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
  });

  // ─── register ──────────────────────────────────────────────────────────────

  it('should register and store token in localStorage', () => {
    const request: RegisterRequest = {
      firstName: 'Alice',
      email: 'alice@smartek.com',
      password: 'secure123',
      role: 'LEARNER'
    };

    service.register(request).subscribe(result => {
      expect(result.token).toBe('mock-jwt-token');
      expect(localStorage.getItem('token')).toBe('mock-jwt-token');
    });

    const req = httpMock.expectOne(`${BASE}/register`);
    expect(req.request.method).toBe('POST');
    req.flush(mockAuthResponse);
  });

  it('should propagate 400 error when registering with duplicate email', () => {
    const request: RegisterRequest = {
      firstName: 'Alice',
      email: 'existing@smartek.com',
      password: 'pass',
      role: 'LEARNER'
    };

    service.register(request).subscribe({
      error: err => expect(err.status).toBe(400)
    });

    httpMock.expectOne(`${BASE}/register`).flush('Email already exists', { status: 400, statusText: 'Bad Request' });
  });

  // ─── logout ────────────────────────────────────────────────────────────────

  it('should clear localStorage and navigate to / on logout', () => {
    localStorage.setItem('token', 'some-token');
    localStorage.setItem('userInfo', JSON.stringify(mockAuthResponse));

    service.logout();

    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('userInfo')).toBeNull();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
  });

  // ─── getToken / isAuthenticated ────────────────────────────────────────────

  it('should return token from localStorage', () => {
    localStorage.setItem('token', 'abc-token');
    expect(service.getToken()).toBe('abc-token');
  });

  it('should return null when no token is stored', () => {
    expect(service.getToken()).toBeNull();
  });

  it('should return true for isAuthenticated when token exists', () => {
    localStorage.setItem('token', 'valid-token');
    expect(service.isAuthenticated()).toBeTrue();
  });

  it('should return false for isAuthenticated when no token', () => {
    expect(service.isAuthenticated()).toBeFalse();
  });

  // ─── getUserInfo ───────────────────────────────────────────────────────────

  it('should return parsed userInfo from localStorage', () => {
    localStorage.setItem('userInfo', JSON.stringify(mockAuthResponse));
    const result = service.getUserInfo();
    expect(result?.userId).toBe(42);
    expect(result?.role).toBe('LEARNER');
  });

  it('should return null when no userInfo is stored', () => {
    expect(service.getUserInfo()).toBeNull();
  });

  it('should return null and not throw when userInfo is malformed JSON', () => {
    localStorage.setItem('userInfo', 'not-valid-json{{{');
    expect(service.getUserInfo()).toBeNull();
  });

  it('should default experience to 0 when missing from stored userInfo', () => {
    const withoutExperience = { ...mockAuthResponse };
    delete (withoutExperience as any).experience;
    localStorage.setItem('userInfo', JSON.stringify(withoutExperience));

    const result = service.getUserInfo();
    expect(result?.experience).toBe(0);
  });

  // ─── validateUser ──────────────────────────────────────────────────────────

  it('should return true when backend confirms user exists', () => {
    localStorage.setItem('userInfo', JSON.stringify(mockAuthResponse));

    service.validateUser().subscribe(result => {
      expect(result).toBeTrue();
    });

    httpMock.expectOne(`${BASE}/validate/42`).flush({ valid: true });
  });

  it('should return false when no userInfo is stored', () => {
    service.validateUser().subscribe(result => {
      expect(result).toBeFalse();
    });

    httpMock.expectNone(`${BASE}/validate/42`);
  });

  it('should return false when backend returns error on validateUser', () => {
    localStorage.setItem('userInfo', JSON.stringify(mockAuthResponse));

    service.validateUser().subscribe(result => {
      expect(result).toBeFalse();
    });

    httpMock.expectOne(`${BASE}/validate/42`).flush('Not found', { status: 404, statusText: 'Not Found' });
  });
});
