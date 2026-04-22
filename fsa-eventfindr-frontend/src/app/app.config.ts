import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { HTTP_INTERCEPTORS, provideHttpClient, withFetch, withInterceptorsFromDi } from '@angular/common/http';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { DefaultOAuthInterceptor, provideOAuthClient } from 'angular-oauth2-oidc';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withFetch(), withInterceptorsFromDi()),
    provideClientHydration(withEventReplay()),
    provideOAuthClient({
      resourceServer: {
        sendAccessToken: true,
      },
    }),
    { provide: HTTP_INTERCEPTORS, useClass: DefaultOAuthInterceptor, multi: true },
  ]
};
