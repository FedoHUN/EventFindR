import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { User, UserRole } from './auth.model';

interface TokenResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
  token_type: string;
}

interface JwtPayload {
  sub: string;
  email?: string;
  name?: string;
  preferred_username?: string;
  realm_access?: { roles: string[] };
  exp: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly keycloakUrl = '/auth';
  private readonly realm = 'EventfindR';
  private readonly clientId = 'eventfindr-client';

  private readonly tokenKey = 'ef_access_token';
  private readonly refreshTokenKey = 'ef_refresh_token';

  private readonly _isLoggedIn = signal(this.hasValidToken());
  private readonly _currentUser = signal<Partial<User> | null>(this.extractUserFromToken());

  readonly isLoggedIn = this._isLoggedIn.asReadonly();
  readonly currentUser = this._currentUser.asReadonly();
  readonly isOrganizer = computed(() => {
    const user = this._currentUser();
    return user?.rola === 'ORGANIZER' || user?.rola === 'ADMIN';
  });

  // Only return a token that is still valid; drop stale tokens to avoid 401s on public endpoints.
  get accessToken(): string | null {
    if (typeof localStorage === 'undefined') return null;
    const token = localStorage.getItem(this.tokenKey);
    if (!token) return null;

    const payload = this.decodeToken(token);
    const isValid = !!payload && payload.exp * 1000 > Date.now();
    if (!isValid) {
      this.clearTokens();
      return null;
    }

    return token;
  }

  login(username: string, password: string): void {
    const tokenUrl = `${this.keycloakUrl}/realms/${this.realm}/protocol/openid-connect/token`;
    const body = new HttpParams()
      .set('grant_type', 'password')
      .set('client_id', this.clientId)
      .set('username', username)
      .set('password', password);

    this.http.post<TokenResponse>(tokenUrl, body.toString(), {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    }).subscribe({
      next: (response) => {
        this.storeTokens(response);
        this._isLoggedIn.set(true);
        this._currentUser.set(this.extractUserFromToken());
      },
      error: () => {
        this.clearTokens();
        this._isLoggedIn.set(false);
        this._currentUser.set(null);
      }
    });
  }

  logout(): void {
    this.clearTokens();
    this._isLoggedIn.set(false);
    this._currentUser.set(null);
  }

  private storeTokens(response: TokenResponse): void {
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(this.tokenKey, response.access_token);
      localStorage.setItem(this.refreshTokenKey, response.refresh_token);
    }
  }

  private clearTokens(): void {
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem(this.tokenKey);
      localStorage.removeItem(this.refreshTokenKey);
    }
  }

  private hasValidToken(): boolean {
    return this.accessToken !== null;
  }

  private extractUserFromToken(): Partial<User> | null {
    const token = this.accessToken;
    if (!token) return null;
    const payload = this.decodeToken(token);
    if (!payload) return null;

    const roles = payload.realm_access?.roles ?? [];
    let rola: UserRole = 'USER';
    if (roles.includes('ADMIN')) rola = 'ADMIN';
    else if (roles.includes('ORGANIZER')) rola = 'ORGANIZER';

    return {
      name: payload.name ?? payload.preferred_username ?? 'User',
      email: payload.email ?? payload.sub,
      rola
    };
  }

  private decodeToken(token: string): JwtPayload | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;
      const payload = atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(payload);
    } catch {
      return null;
    }
  }
}
