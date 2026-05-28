import { Injectable, inject } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OAuthService } from 'angular-oauth2-oidc';
import { environment } from '../../../environments/environment';

@Injectable()
export class AuthTokenInterceptor implements HttpInterceptor {
  private readonly oauthService = inject(OAuthService);

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (!this.shouldAttachToken(request)) {
      return next.handle(request);
    }

    const token = this.oauthService.getAccessToken();
    return next.handle(request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    }));
  }

  private shouldAttachToken(request: HttpRequest<unknown>): boolean {
    return this.isBackendRequest(request.url)
      && !this.isPublicRequest(request)
      && this.oauthService.hasValidAccessToken()
      && !!this.oauthService.getAccessToken();
  }

  private isBackendRequest(url: string): boolean {
    if (url.startsWith('/')) {
      return true;
    }

    const allowedBases = [environment.beUrl, environment.uploadUrl].filter(Boolean);
    return allowedBases.some(base => url === base || url.startsWith(`${base}/`));
  }

  private isPublicRequest(request: HttpRequest<unknown>): boolean {
    if (request.method !== 'GET' && request.method !== 'HEAD') {
      return false;
    }

    const path = this.apiPathOf(request.url);

    return path === '/events'
      || path === '/events/trending'
      || /^\/events\/\d+$/.test(path)
      || /^\/events\/\d+\/similar$/.test(path)
      || /^\/events\/\d+\/attendance-counts$/.test(path)
      || /^\/events\/\d+\/media$/.test(path)
      || /^\/events\/\d+\/media\/\d+\/file$/.test(path)
      || /^\/events\/\d+\/comments$/.test(path)
      || path === '/users/organizers'
      || path === '/artists/search'
      || /^\/users\/\d+$/.test(path)
      || /^\/users\/\d+\/posts$/.test(path)
      || /^\/posts\/\d+\/media\/\d+\/file$/.test(path);
  }

  private apiPathOf(url: string): string {
    const path = new URL(url, 'http://eventfindr.local').pathname;
    return path.startsWith('/api/') ? path.slice(4) : path;
  }
}
