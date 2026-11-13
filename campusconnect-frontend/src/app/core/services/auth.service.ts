import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, SignupRequest, VerifyOtpRequest } from '../models/auth.model';
import { User } from '../models/user.model';
import { NotificationService } from './notification.service';

const TOKEN_KEY = 'cc_token';
const REFRESH_TOKEN_KEY = 'cc_refresh_token';
const USER_KEY = 'cc_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = `${environment.apiUrl}/users`;

  currentUser = signal<User | null>(this.loadStoredUser());
  isLoggedIn = signal<boolean>(!!this.getToken());

  constructor(
    private http: HttpClient,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  signup(request: SignupRequest): Observable<string> {
    return this.http.post(`${this.apiUrl}/signup`, request, { responseType: 'text' });
  }

  verifyOtp(request: VerifyOtpRequest): Observable<string> {
    return this.http.post(`${this.apiUrl}/verify-otp`, request, { responseType: 'text' });
  }

  resendOtp(email: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/resend-otp`, null, {
      params: { email },
      responseType: 'text'
    });
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap((res) => {
        localStorage.setItem(TOKEN_KEY, res.token);
        localStorage.setItem(REFRESH_TOKEN_KEY, res.refreshToken);
        this.isLoggedIn.set(true);
        this.refreshUser().subscribe();
      })
    );
  }

  refreshUser(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`).pipe(
      tap((user) => {
        localStorage.setItem(USER_KEY, JSON.stringify(user));
        this.currentUser.set(user);
        this.notificationService.refreshUnreadCount(user.id);
      })
    );
  }

  refreshAccessToken(): Observable<{ token: string }> {
    const refreshToken = this.getRefreshToken();
    return this.http.post<{ token: string }>(`${this.apiUrl}/refresh-token`, { refreshToken }).pipe(
      tap((res) => {
        localStorage.setItem(TOKEN_KEY, res.token);
      })
    );
  }

  logout(): void {
    const refreshToken = this.getRefreshToken();
    if (refreshToken) {
      this.http.post(`${this.apiUrl}/logout`, { refreshToken }, { responseType: 'text' }).subscribe({
        error: () => {} // best-effort - clear local state regardless of server response
      });
    }

    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUser.set(null);
    this.isLoggedIn.set(false);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  private loadStoredUser(): User | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }

  isVendor(): boolean {
    return this.currentUser()?.role === 'ROLE_VENDOR';
  }

  isAdmin(): boolean {
    return this.currentUser()?.isAdmin === true;
  }
}
