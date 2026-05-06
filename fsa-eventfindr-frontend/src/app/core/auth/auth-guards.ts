import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth';

export const isLoggedIn: CanActivateFn = () => {
  const authService = inject(AuthService);

  return authService.tryLogin().then((user) => {
    if (user) {
      return true;
    } else {
      authService.login();
      return false;
    }
  });
};

export const isOrganizer: CanActivateFn = async () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const user = await authService.tryLogin();
  if (!user) {
    authService.login();
    return false;
  }

  // Ensure we have the authoritative role from the backend (roles may not be present in the token
  // or may be updated on the server). getRoleFromBackend updates the local user signal.
  if (typeof authService.getRoleFromBackend === 'function') {
    await authService.getRoleFromBackend();
  }

  if (authService.isOrganizer()) {
    return true;
  }

  return router.createUrlTree(['/events']);
};
