import { DestroyRef, inject, Injectable, signal, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { OAuthService } from 'angular-oauth2-oidc';
import { catchError, firstValueFrom, of } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { User, UserRole } from './auth.model';
import { authCodeFlowConfig } from './oidc.config';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly oauthService = inject(OAuthService);
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  private readonly _currentUser = signal<Partial<User> | undefined>(undefined);
  private roleSyncInFlight?: Promise<UserRole | undefined>;

  readonly user = this._currentUser.asReadonly();

  constructor() {
    if (!this.isBrowser) return;

    this.oauthService.configure(authCodeFlowConfig);

    this.oauthService.events.pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(() => this.updateUserFromToken());

    this.tryLogin();
  }

  /**
   * Explicitly request the user's role from the backend and update the local user signal.
   * This method returns the resolved role (or undefined on error) — useful for guards
   * which need the authoritative role immediately after login.
   */
  async getRoleFromBackend(): Promise<UserRole | undefined> {
    if (!this.oauthService.hasValidAccessToken()) return undefined;
    if (this.roleSyncInFlight) return this.roleSyncInFlight;

    this.roleSyncInFlight = firstValueFrom(
      this.http.get<User>(`${environment.beUrl}/users/me`).pipe(
        catchError(() => of(undefined))
      )
    ).then((dbUser) => {
      const current = this._currentUser();
      if (current && dbUser?.role) {
        this._currentUser.set({ ...current, ...dbUser });
      }
      return dbUser?.role;
    }).finally(() => {
      this.roleSyncInFlight = undefined;
    });

    return this.roleSyncInFlight;
  }

  login() {
    this.oauthService.loadDiscoveryDocumentAndLogin().then(() => {
      this.updateUserFromToken();
      this.syncRoleFromBackend();
    });
  }

  register() {
    this.oauthService.loadDiscoveryDocument().then(() => {
      const registrationUrl =
        `${authCodeFlowConfig.issuer}/protocol/openid-connect/registrations` +
        `?client_id=${encodeURIComponent(authCodeFlowConfig.clientId!)}` +
        `&response_type=code` +
        `&scope=${encodeURIComponent(authCodeFlowConfig.scope!)}` +
        `&redirect_uri=${encodeURIComponent(authCodeFlowConfig.redirectUri!)}`;
      location.href = registrationUrl;
    });
  }

  logout() {
    this.oauthService.logOut();
    this._currentUser.set(undefined);
  }

  tryLogin() {
    return this.oauthService.loadDiscoveryDocumentAndTryLogin().then(() => {
      this.updateUserFromToken();
      this.syncRoleFromBackend();
      return this._currentUser();
    });
  }

  isOrganizer(): boolean {
    const user = this._currentUser();
    return user?.role === 'ORGANIZER' || user?.role === 'ADMIN';
  }

  isArtist(): boolean {
    const user = this._currentUser();
    return !!user?.artistName || user?.role === 'ARTIST' || user?.role === 'ADMIN';
  }

  refreshRole(): void {
    this.syncRoleFromBackend();
  }

  private syncRoleFromBackend(): void {
    if (!this.oauthService.hasValidAccessToken()) return;

    void this.getRoleFromBackend();
  }

  private extractUser(): Partial<User> | undefined {
    if (!this.oauthService.hasValidAccessToken()) return undefined;

    const claims = this.oauthService.getIdentityClaims() as Record<string, unknown> | null;
    if (!claims) return undefined;

    const roles = ((claims['realm_access'] as { roles?: string[] })?.roles ?? [])
      .map((role) => role.toUpperCase());
    let role: UserRole = 'USER';
    if (roles.includes('ADMIN')) role = 'ADMIN';
    else if (roles.includes('ORGANIZER')) role = 'ORGANIZER';
    else if (roles.includes('ARTIST')) role = 'ARTIST';

    return {
      name: (claims['name'] as string) ?? (claims['preferred_username'] as string) ?? 'User',
      email: (claims['email'] as string) ?? (claims['sub'] as string) ?? '',
      role
    };
  }

  private updateUserFromToken(): void {
    this._currentUser.set(this.extractUser());
  }
}
