import { HTTP_INTERCEPTORS, HttpClient, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { OAuthService } from 'angular-oauth2-oidc';
import { AuthTokenInterceptor } from './auth-token.interceptor';
import { environment } from '../../../environments/environment';

describe('AuthTokenInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        {
          provide: OAuthService,
          useValue: {
            hasValidAccessToken: () => true,
            getAccessToken: () => 'access-token',
          },
        },
        { provide: HTTP_INTERCEPTORS, useClass: AuthTokenInterceptor, multi: true },
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('does not attach tokens to public event reads', () => {
    http.get(`${environment.beUrl}/events`).subscribe();

    const request = httpMock.expectOne(`${environment.beUrl}/events`);
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({ events: [] });
  });

  it('attaches tokens to protected self-service reads', () => {
    http.get(`${environment.beUrl}/users/me`).subscribe();

    const request = httpMock.expectOne(`${environment.beUrl}/users/me`);
    expect(request.request.headers.get('Authorization')).toBe('Bearer access-token');
    request.flush({});
  });
});
