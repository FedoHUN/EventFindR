import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth';

function isProtectedApiRequest(url: string, method: string): boolean {
  const upperMethod = method.toUpperCase();

  if (upperMethod === 'POST' && (url === '/api/events' || /^\/api\/events\/\d+\/attend$/.test(url))) {
    return true;
  }

  if (upperMethod === 'POST' && url === '/api/users') {
    return true;
  }

  return false;
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.accessToken;

  if (token && isProtectedApiRequest(req.url, req.method)) {
    const cloned = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
    return next(cloned);
  }

  return next(req);
};
