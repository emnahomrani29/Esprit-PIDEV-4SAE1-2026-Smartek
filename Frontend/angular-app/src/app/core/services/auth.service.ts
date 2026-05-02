import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, map, catchError, of } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';

export interface RegisterRequest {
  firstName: string;
  email: string;
  password: string;
  phone?: string;
  imageBase64?: string;
  experience?: number;
  role: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  userId: number;
  email: string;
  firstName: string;
  role: string;
  imageBase64?: string;
  experience?: number;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = environment.authApiUrl;

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, request).pipe(
      tap(response => {
        this.saveToken(response.token);
        this.saveUserInfo(response);
      })
    );
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => {
        this.saveToken(response.token);
        this.saveUserInfo(response);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    this.router.navigate(['/']);
  }

  private saveToken(token: string): void {
    localStorage.setItem('token', token);
  }

  private saveUserInfo(userInfo: AuthResponse): void {
    localStorage.setItem('userInfo', JSON.stringify(userInfo));
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getUserInfo(): AuthResponse | null {
    const userInfo = localStorage.getItem('userInfo');
    if (!userInfo) return null;

    try {
      const parsed = JSON.parse(userInfo);
      // S'assurer que experience existe, sinon mettre 0 par défaut
      if (parsed && typeof parsed.experience === 'undefined') {
        parsed.experience = 0;
      }
      return parsed;
    } catch (e) {
      return null;
    }
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  validateUser(): Observable<boolean> {
    const userInfo = this.getUserInfo();
    if (!userInfo) {
      return of(false);
    }

    // Appeler le backend pour vérifier si l'utilisateur existe toujours
    return this.http.get<any>(`${this.apiUrl}/validate/${userInfo.userId}`).pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }

  fetchUserData(): Observable<AuthResponse> {
    const userInfo = this.getUserInfo();
    if (!userInfo) {
      return of({} as AuthResponse);
    }

    return this.http.get<AuthResponse>(`${this.apiUrl}/user/${userInfo.userId}`).pipe(
      tap(response => {
        // Mettre à jour les données dans le localStorage
        this.saveUserInfo(response);
      })
    );
  }
}
