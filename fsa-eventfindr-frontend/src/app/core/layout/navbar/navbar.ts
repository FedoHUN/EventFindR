import { DOCUMENT, isPlatformBrowser } from '@angular/common';
import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
  PLATFORM_ID,
  ViewChild,
  inject,
  signal
} from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../auth/auth';

@Component({
  selector: 'app-navbar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav #navbarEl class="navbar navbar-expand-lg navbar-dark bg-ef-primary-dark fixed-top">
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
            @if (auth.user()) {
              <li class="nav-item">
                <a class="nav-link" routerLink="/my-profile" routerLinkActive="active">My Profile</a>
              </li>
            }
          </ul>

          @if (auth.user(); as user) {
            <span class="navbar-text text-light mx-auto">
              Hi, {{ user.name }}
              @if (auth.isOrganizer()) {
                <span class="badge bg-warning text-dark ms-2">Organizer</span>
              }
            </span>
            <button class="btn btn-outline-light btn-sm" (click)="auth.logout()">Logout</button>
          } @else {
            <button class="btn btn-outline-light btn-sm ms-auto" (click)="auth.login()">
              <i class="bi bi-box-arrow-in-right me-1"></i>Login
            </button>
          }
        </div>
      </div>
    </nav>
  `
})
export class NavbarComponent implements AfterViewInit, OnDestroy {
  readonly auth = inject(AuthService);
  readonly navOpen = signal(false);

  @ViewChild('navbarEl', { static: true }) private navbarEl?: ElementRef<HTMLElement>;

  private readonly platformId = inject(PLATFORM_ID);
  private readonly document = inject(DOCUMENT);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  private resizeObserver?: ResizeObserver;
  private readonly onWindowResize = () => this.updateNavbarHeight();

  ngAfterViewInit(): void {
    if (!this.isBrowser || !this.navbarEl) {
      return;
    }

    this.updateNavbarHeight();

    this.resizeObserver = new ResizeObserver(() => this.updateNavbarHeight());
    this.resizeObserver.observe(this.navbarEl.nativeElement);
    window.addEventListener('resize', this.onWindowResize, { passive: true });
  }

  ngOnDestroy(): void {
    if (!this.isBrowser) {
      return;
    }

    this.resizeObserver?.disconnect();
    window.removeEventListener('resize', this.onWindowResize);
  }

  private updateNavbarHeight(): void {
    const height = this.navbarEl?.nativeElement.getBoundingClientRect().height;
    if (!height) {
      return;
    }

    // Keep subpixel precision to avoid creating a visible 1px seam below the fixed navbar.
    this.document.documentElement.style.setProperty('--app-navbar-height', `${height.toFixed(2)}px`);
  }
}
