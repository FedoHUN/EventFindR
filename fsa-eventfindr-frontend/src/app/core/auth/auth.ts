import { inject, Injectable, signal, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { OAuthService } from 'angular-oauth2-oidc';
import { User, UserRole } from './auth.model';
import { authCodeFlowConfig } from './oidc.config';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly oauthService = inject(OAuthService);
  private readonly http = inject(HttpClient);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  private readonly _currentUser = signal<Partial<User> | undefined>(undefined);

  readonly user = this._currentUser.asReadonly();

  constructor() {
    if (!this.isBrowser) return;

    this.oauthService.configure(authCodeFlowConfig);

    this.oauthService.events.subscribe(() => {
      const jwtUser = this.extractUser();
      this._currentUser.set(jwtUser);
      if (jwtUser) {
        this.syncRoleFromBackend();
      }
    });

    this.tryLogin();
  }

  login() {
    this.oauthService.loadDiscoveryDocumentAndLogin().then(() => {
      this._currentUser.set(this.extractUser());
      this.syncRoleFromBackend();
    });
  }

  logout() {
    this.oauthService.logOut();
    this._currentUser.set(undefined);
  }

  tryLogin() {
    return this.oauthService.loadDiscoveryDocumentAndTryLogin().then(() => {
      this._currentUser.set(this.extractUser());
      this.syncRoleFromBackend();
      return this._currentUser();
    });
  }

  isOrganizer(): boolean {
    const user = this._currentUser();
    return user?.rola === 'ORGANIZER' || user?.rola === 'ADMIN';
  }

  refreshRole(): void {
    this.syncRoleFromBackend();
  }

  private syncRoleFromBackend(): void {
    if (!this.oauthService.hasValidAccessToken()) return;

    this.http.get<{ rola: UserRole }>(`${environment.beUrl}/users/me`).subscribe({
      next: (dbUser) => {
        const current = this._currentUser();
        if (current && dbUser.rola) {
          this._currentUser.set({ ...current, rola: dbUser.rola });
        }
      },
      error: () => {}
    });
  }

  private extractUser(): Partial<User> | undefined {
    if (!this.oauthService.hasValidAccessToken()) return undefined;

    const claims = this.oauthService.getIdentityClaims() as Record<string, unknown> | null;
    if (!claims) return undefined;

    const roles = (claims['realm_access'] as { roles?: string[] })?.roles ?? [];
    let rola: UserRole = 'USER';
    if (roles.includes('ADMIN')) rola = 'ADMIN';
    else if (roles.includes('ORGANIZER')) rola = 'ORGANIZER';

    return {
      name: (claims['name'] as string) ?? (claims['preferred_username'] as string) ?? 'User',
      email: (claims['email'] as string) ?? (claims['sub'] as string) ?? '',
      rola
    };
  }
}
