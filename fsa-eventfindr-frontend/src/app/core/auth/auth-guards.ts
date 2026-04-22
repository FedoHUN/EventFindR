import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
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
