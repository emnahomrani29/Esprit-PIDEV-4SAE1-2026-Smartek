import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService, LoginRequest, RegisterRequest, AuthResponse } from './auth.service';
import { environment } from '../../../environments/environment';

/**
 * Tests unitaires pour AuthService.
 * Vérifie l'authentification, la gestion du token et du localStorage.
 */
describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/auth`;

  const mockAuthResponse: AuthResponse = {
    token: 'eyJhbGciOiJIUzI1NiJ9.test.token',
    userId: 5,
    email: 'trainer@smartek.com',
    firstName: 'Ahmed',
    role: 'TRAINER',
    message: 'Connexion réussie'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService, provideRouter([])]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // ─── login ────────────────────────────────────────────────────────────────
  describe('login()', () => {
    it('should call POST /auth/login and save token to localStorage', () => {
      const loginRequest: LoginRequest = {
        email: 'trainer@smartek.com',
        password: 'password123'
      };

      service.login(loginRequest).subscribe(response => {
        expect(response.token).toBe(mockAuthResponse.token);
        expect(response.role).toBe('TRAINER');
        // Vérifier que le token est sauvegardé
        expect(localStorage.getItem('token')).toBe(mockAuthResponse.token);
      });

      const req = httpMock.expectOne(`${apiUrl}/login`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(loginRequest);
      req.flush(mockAuthResponse);
    });

    it('should save user info to localStorage after login', () => {
      service.login({ email: 'trainer@smartek.com', password: 'pass' }).subscribe(() => {
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
        expect(userInfo.userId).toBe(5);
        expect(userInfo.role).toBe('TRAINER');
      });

      const req = httpMock.expectOne(`${apiUrl}/login`);
      req.flush(mockAuthResponse);
    });
  });

  // ─── register ─────────────────────────────────────────────────────────────
  describe('register()', () => {
    it('should call POST /auth/register and save token', () => {
      const registerRequest: RegisterRequest = {
        firstName: 'Ahmed',
        email: 'ahmed@smartek.com',
        password: 'password123',
        role: 'TRAINER'
      };

      service.register(registerRequest).subscribe(response => {
        expect(response.token).toBeTruthy();
        expect(localStorage.getItem('token')).toBe(mockAuthResponse.token);
      });

      const req = httpMock.expectOne(`${apiUrl}/register`);
      expect(req.request.method).toBe('POST');
      req.flush(mockAuthResponse);
    });
  });

  // ─── logout ───────────────────────────────────────────────────────────────
  describe('logout()', () => {
    it('should remove token and userInfo from localStorage', () => {
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('userInfo', JSON.stringify(mockAuthResponse));

      service.logout();

      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('userInfo')).toBeNull();
    });
  });

  // ─── getToken ─────────────────────────────────────────────────────────────
  describe('getToken()', () => {
    it('should return token from localStorage', () => {
      localStorage.setItem('token', 'my-jwt-token');
      expect(service.getToken()).toBe('my-jwt-token');
    });

    it('should return null when no token is stored', () => {
      expect(service.getToken()).toBeNull();
    });
  });

  // ─── getUserInfo ──────────────────────────────────────────────────────────
  describe('getUserInfo()', () => {
    it('should return parsed user info from localStorage', () => {
      localStorage.setItem('userInfo', JSON.stringify(mockAuthResponse));

      const userInfo = service.getUserInfo();
      expect(userInfo?.userId).toBe(5);
      expect(userInfo?.role).toBe('TRAINER');
      expect(userInfo?.email).toBe('trainer@smartek.com');
    });

    it('should return null when no user info is stored', () => {
      expect(service.getUserInfo()).toBeNull();
    });

    it('should return default experience=0 if not present in stored data', () => {
      const userWithoutExp = { ...mockAuthResponse };
      delete (userWithoutExp as any).experience;
      localStorage.setItem('userInfo', JSON.stringify(userWithoutExp));

      const userInfo = service.getUserInfo();
      expect(userInfo?.experience).toBe(0);
    });
  });

  // ─── isAuthenticated ──────────────────────────────────────────────────────
  describe('isAuthenticated()', () => {
    it('should return true when token exists', () => {
      localStorage.setItem('token', 'valid-token');
      expect(service.isAuthenticated()).toBeTrue();
    });

    it('should return false when no token exists', () => {
      expect(service.isAuthenticated()).toBeFalse();
    });
  });

  // ─── validateUser ─────────────────────────────────────────────────────────
  describe('validateUser()', () => {
    it('should return true when user is valid', () => {
      localStorage.setItem('userInfo', JSON.stringify(mockAuthResponse));

      service.validateUser().subscribe(isValid => {
        expect(isValid).toBeTrue();
      });

      const req = httpMock.expectOne(`${apiUrl}/validate/5`);
      expect(req.request.method).toBe('GET');
      req.flush(true);
    });

    it('should return false when no user info is stored', () => {
      service.validateUser().subscribe(isValid => {
        expect(isValid).toBeFalse();
      });
    });
  });
});
