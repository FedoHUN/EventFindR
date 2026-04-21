import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../auth/auth';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-navbar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive, FormsModule],
  template: `
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark sticky-top">
      <div class="container">
        <a class="navbar-brand fw-bold" routerLink="/">
          <i class="bi bi-calendar-event me-2"></i>EventfindR
        </a>
        <button
          class="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarNav"
          aria-controls="navbarNav"
          [attr.aria-expanded]="navOpen()"
          aria-label="Toggle navigation"
          (click)="navOpen.set(!navOpen())">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" [class.show]="navOpen()" id="navbarNav">
          <ul class="navbar-nav me-auto">
            <li class="nav-item">
              <a class="nav-link" routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Home</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" routerLink="/events" routerLinkActive="active">Events</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" routerLink="/about" routerLinkActive="active">About Us</a>
            </li>
          </ul>
          <div class="d-flex align-items-center gap-2">
            @if (auth.isLoggedIn()) {
              <span class="text-light me-2">
                <i class="bi bi-person-circle me-1"></i>{{ auth.currentUser()?.name }}
                @if (auth.isOrganizer()) {
                  <span class="badge bg-warning text-dark ms-1">Organizer</span>
                }
              </span>
              <button class="btn btn-outline-light btn-sm" (click)="auth.logout()">Logout</button>
            } @else {
              <button class="btn btn-outline-light btn-sm" (click)="showLogin.set(true)">
                <i class="bi bi-box-arrow-in-right me-1"></i>Login
              </button>
            }
          </div>
        </div>
      </div>
    </nav>

    @if (showLogin() && !auth.isLoggedIn()) {
      <div class="modal d-block" tabindex="-1" role="dialog" aria-labelledby="loginModalLabel" (click)="showLogin.set(false)">
        <div class="modal-dialog" role="document" (click)="$event.stopPropagation()">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title" id="loginModalLabel">Login to EventfindR</h5>
              <button type="button" class="btn-close" aria-label="Close" (click)="showLogin.set(false)"></button>
            </div>
            <div class="modal-body">
              <form (ngSubmit)="onLogin()">
                <div class="mb-3">
                  <label for="username" class="form-label">Username</label>
                  <input type="text" class="form-control" id="username" [(ngModel)]="username" name="username" required>
                </div>
                <div class="mb-3">
                  <label for="password" class="form-label">Password</label>
                  <input type="password" class="form-control" id="password" [(ngModel)]="password" name="password" required>
                </div>
                <button type="submit" class="btn btn-primary w-100">Login</button>
              </form>
              <p class="text-muted small mt-3 text-center">
                Use your Keycloak credentials to log in.
              </p>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-backdrop show"></div>
    }
  `
})
export class NavbarComponent {
  readonly auth = inject(AuthService);
  readonly navOpen = signal(false);
  readonly showLogin = signal(false);
  username = '';
  password = '';

  onLogin(): void {
    if (this.username && this.password) {
      this.auth.login(this.username, this.password);
      this.showLogin.set(false);
      this.username = '';
      this.password = '';
    }
  }
}
