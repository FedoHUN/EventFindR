import { DOCUMENT, isPlatformBrowser } from '@angular/common';
import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  OnDestroy,
  PLATFORM_ID,
  ViewChild,
  inject,
  signal
} from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Notification } from '../../../modules/events/event.model';
import { AuthService } from '../../auth/auth';
import { NotificationApi } from '../../notifications/notification-api';
import { ThemeService } from '../../services/theme';

@Component({
  selector: 'app-navbar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive, DatePipe],
  template: `
    <nav #navbarEl class="navbar navbar-expand-lg fixed-top ef-navbar">
      <div class="container">
        <a class="navbar-brand d-flex align-items-center gap-2" routerLink="/" (click)="closeNav()">
          <span class="brand-icon">
            <i class="bi bi-calendar-event"></i>
          </span>
          <span class="brand-text">EventfindR</span>
        </a>

        <button
          class="navbar-toggler border-0"
          type="button"
          aria-controls="navbarNav"
          [attr.aria-expanded]="navOpen()"
          aria-label="Toggle navigation"
          (click)="navOpen.set(!navOpen())">
          <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" [class.show]="navOpen()" id="navbarNav">
          <ul class="navbar-nav me-auto gap-1">
            <li class="nav-item">
              <a class="nav-link" routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }" (click)="closeNav()">Home</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" routerLink="/events" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }" (click)="closeNav()">Events</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" routerLink="/discover" routerLinkActive="active" (click)="closeNav()">Discover</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" routerLink="/about" routerLinkActive="active" (click)="closeNav()">About</a>
            </li>
            @if (auth.isOrganizer()) {
              <li class="nav-item">
                <a class="nav-link" routerLink="/events/create" routerLinkActive="active" (click)="closeNav()">
                  <i class="bi bi-plus-lg me-1"></i>Create
                </a>
              </li>
              <li class="nav-item">
                <a class="nav-link" routerLink="/events/my" routerLinkActive="active" (click)="closeNav()">My Events</a>
              </li>
            }
          </ul>

          @if (auth.user(); as user) {
            <div class="nav-actions d-flex align-items-center gap-2 ms-auto ms-lg-3">
              <button
                class="nav-icon-link"
                (click)="themeService.cycleTheme()"
                [attr.aria-label]="'Theme: ' + themeService.theme()"
                type="button">
                @switch (themeService.theme()) {
                  @case ('light') { <i class="bi bi-sun-fill"></i> }
                  @case ('dark') { <i class="bi bi-moon-fill"></i> }
                  @case ('system') { <i class="bi bi-circle-half"></i> }
                }
              </button>
              <a routerLink="/my-calendar" class="nav-icon-link" routerLinkActive="nav-icon-link--active" aria-label="My Calendar" (click)="closeNav()">
                <i class="bi bi-calendar-week"></i>
              </a>
              <div class="notif-wrapper">
                <button
                  class="nav-icon-link position-relative"
                  (click)="toggleNotifications()"
                  aria-label="Notifications"
                  aria-haspopup="menu"
                  [attr.aria-expanded]="notificationsOpen()">
                  <i class="bi bi-bell"></i>
                  @if (unreadCount() > 0) {
                    <span class="notif-badge">{{ unreadCount() > 9 ? '9+' : unreadCount() }}</span>
                  }
                </button>
                @if (notificationsOpen()) {
                  <div class="notif-dropdown" role="menu" aria-label="Notifications">
                    <div class="notif-header">
                      <span class="fw-semibold">Notifications</span>
                      @if (unreadCount() > 0) {
                        <button class="btn btn-sm btn-link p-0" (click)="markAllRead()">Mark all read</button>
                      }
                    </div>
                    @if (notifications().length === 0) {
                      <div class="notif-empty">No notifications</div>
                    } @else {
                      <div class="notif-list">
                        @for (n of notifications(); track n.id) {
                          <div class="notif-item" [class.notif-item--unread]="!n.read" (click)="onNotificationClick(n)">
                            <i class="bi me-2" [class]="notifIcon(n.type)"></i>
                            <div class="notif-content">
                              <p class="mb-0 small">{{ n.message }}</p>
                              <span class="text-muted" style="font-size:0.7rem">{{ n.created | date:'short' }}</span>
                            </div>
                          </div>
                        }
                      </div>
                    }
                  </div>
                }
              </div>
              <a routerLink="/my-profile" class="user-pill" routerLinkActive="user-pill--active" (click)="closeNav()">
                <span class="user-avatar">
                  <i class="bi bi-person-fill"></i>
                </span>
                <span class="user-name">{{ user.name }}</span>
                @if (auth.isOrganizer()) {
                  <span class="badge bg-warning text-dark badge-sm">Organizer</span>
                }
              </a>
              <button class="btn btn-outline-light btn-sm" (click)="closeNav(); auth.logout()">
                <i class="bi bi-box-arrow-right me-1"></i>Logout
              </button>
            </div>
          } @else {
            <div class="nav-actions d-flex align-items-center gap-2 ms-auto ms-lg-3">
              <button
                class="nav-icon-link"
                (click)="themeService.cycleTheme()"
                [attr.aria-label]="'Theme: ' + themeService.theme()"
                type="button">
                @switch (themeService.theme()) {
                  @case ('light') { <i class="bi bi-sun-fill"></i> }
                  @case ('dark') { <i class="bi bi-moon-fill"></i> }
                  @case ('system') { <i class="bi bi-circle-half"></i> }
                }
              </button>
              <button class="btn btn-outline-light btn-sm" (click)="closeNav(); auth.login()">
                Log in
              </button>
              <button class="btn btn-light btn-sm fw-semibold" (click)="closeNav(); auth.register()">
                Sign up
              </button>
            </div>
          }
        </div>
      </div>
    </nav>
  `,
  styles: `
    .ef-navbar {
      background: rgba(27, 15, 53, 0.92);
      backdrop-filter: blur(12px);
      -webkit-backdrop-filter: blur(12px);
      border-bottom: 1px solid rgba(255, 255, 255, 0.06);
      padding-block: 0.5rem;
    }

    .brand-icon {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 32px;
      height: 32px;
      border-radius: 8px;
      background: linear-gradient(135deg, var(--ef-primary-light), var(--ef-primary-dark));
      color: #fff;
      font-size: 1rem;
      box-shadow: 0 0 12px rgba(139, 92, 246, 0.3);
    }

    .brand-text {
      font-family: 'Bricolage Grotesque', 'Segoe UI', sans-serif;
      font-weight: 700;
      font-size: 1.25rem;
      color: #fff;
      letter-spacing: -0.02em;
    }

    .navbar-nav .nav-link {
      color: rgba(255, 255, 255, 0.7);
      font-weight: 500;
      font-size: 0.9rem;
      padding: 0.5rem 0.875rem;
      border-radius: 0.5rem;
      transition: color 0.15s ease, background-color 0.15s ease;
    }

    .navbar-nav .nav-link:hover {
      color: #fff;
      background: rgba(255, 255, 255, 0.08);
    }

    .navbar-nav .nav-link.active {
      color: #fff;
      background: rgba(139, 92, 246, 0.25);
    }

    .user-pill {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.25rem 0.75rem 0.25rem 0.25rem;
      border-radius: 2rem;
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.1);
      color: rgba(255, 255, 255, 0.9);
      text-decoration: none;
      transition: background 0.15s ease;
      font-size: 0.875rem;
      font-weight: 500;
    }

    .user-pill:hover,
    .user-pill--active {
      background: rgba(255, 255, 255, 0.14);
      color: #fff;
    }

    .user-avatar {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: var(--ef-primary-dark);
      color: #fff;
      font-size: 0.8rem;
    }

    .user-name {
      max-width: 120px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .badge-sm {
      font-size: 0.65rem;
      padding: 0.2em 0.5em;
    }

    .navbar-toggler-icon {
      filter: brightness(1.2);
    }

    .nav-icon-link {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.1);
      color: rgba(255, 255, 255, 0.8);
      font-size: 1rem;
      cursor: pointer;
      transition: background 0.15s ease;
      text-decoration: none;
    }

    .nav-icon-link:hover,
    .nav-icon-link--active {
      background: rgba(255, 255, 255, 0.15);
      color: #fff;
    }

    .notif-badge {
      position: absolute;
      top: -2px;
      right: -2px;
      background: #dc3545;
      color: #fff;
      font-size: 0.6rem;
      font-weight: 700;
      min-width: 16px;
      height: 16px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 4px;
    }

    .notif-wrapper {
      position: relative;
    }

    .notif-dropdown {
      position: absolute;
      top: calc(100% + 8px);
      right: 50%;
      transform: translateX(50%);
      width: 320px;
      background: var(--ef-surface);
      border: 1px solid var(--ef-border-subtle);
      border-radius: 12px;
      box-shadow: var(--ef-shadow-lg);
      z-index: 1000;
      overflow: hidden;
    }

    .notif-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.75rem 1rem;
      border-bottom: 1px solid var(--ef-border-subtle);
      color: var(--ef-text);
    }

    .notif-empty {
      padding: 2rem 1rem;
      text-align: center;
      color: var(--ef-text-light);
      font-size: 0.875rem;
    }

    .notif-list {
      max-height: 300px;
      overflow-y: auto;
    }

    .notif-item {
      display: flex;
      align-items: flex-start;
      padding: 0.625rem 1rem;
      cursor: pointer;
      transition: background 0.1s;
      color: var(--ef-text);
    }

    .notif-item:hover {
      background: var(--ef-bg);
    }

    .notif-item--unread {
      background: var(--ef-primary-50);
    }

    .notif-content {
      flex: 1;
      min-width: 0;
    }

    @media (max-width: 991.98px) {
      .ef-navbar .container {
        max-width: 100%;
        padding-inline: 0.75rem;
      }

      .navbar-collapse {
        max-height: calc(100vh - var(--app-navbar-height, 64px) - 0.5rem);
        overflow-y: auto;
        overflow-x: hidden;
        padding: 0.5rem 0 0.25rem;
      }

      .navbar-nav {
        gap: 0 !important;
      }

      .navbar-nav .nav-link {
        padding: 0.625rem 0.75rem;
        font-size: 0.95rem;
        border-radius: 0.375rem;
      }

      .nav-actions {
        width: 100%;
        flex-wrap: wrap;
        justify-content: flex-start;
        margin-left: 0 !important;
        padding-top: 0.625rem;
        border-top: 1px solid rgba(255, 255, 255, 0.08);
        gap: 0.5rem !important;
      }

      .user-pill {
        max-width: calc(100vw - 2rem);
        font-size: 0.8rem;
      }

      .user-name {
        max-width: 100px;
      }

      .notif-dropdown {
        position: fixed;
        top: calc(var(--app-navbar-height, 64px) + 0.5rem);
        left: 0.75rem;
        right: 0.75rem;
        width: auto;
        transform: none;
      }
    }
  `
})
export class NavbarComponent implements AfterViewInit, OnDestroy {
  readonly auth = inject(AuthService);
  readonly themeService = inject(ThemeService);
  private readonly notificationApi = inject(NotificationApi);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  readonly navOpen = signal(false);
  readonly notificationsOpen = signal(false);
  readonly notifications = signal<Notification[]>([]);
  readonly unreadCount = signal(0);

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

    if (typeof ResizeObserver !== 'undefined') {
      this.resizeObserver = new ResizeObserver(() => this.updateNavbarHeight());
      this.resizeObserver.observe(this.navbarEl.nativeElement);
    }
    window.addEventListener('resize', this.onWindowResize, { passive: true });

    // Load notifications if logged in
    if (this.auth.user()) {
      this.loadUnreadCount();
    }
  }

  ngOnDestroy(): void {
    if (!this.isBrowser) {
      return;
    }

    this.resizeObserver?.disconnect();
    window.removeEventListener('resize', this.onWindowResize);
  }

  closeNav(): void {
    this.navOpen.set(false);
    this.notificationsOpen.set(false);
  }

  toggleNotifications(): void {
    if (this.notificationsOpen()) {
      this.notificationsOpen.set(false);
      return;
    }
    this.notificationsOpen.set(true);
    this.notificationApi.getNotifications().pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (n) => this.notifications.set(n),
      error: () => {}
    });
  }

  markAllRead(): void {
    this.notificationApi.markAllNotificationsAsRead().pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => {
        this.notifications.update(list => list.map(n => ({ ...n, read: true })));
        this.unreadCount.set(0);
      }
    });
  }

  onNotificationClick(n: Notification): void {
    if (!n.read) {
      this.notificationApi.markNotificationAsRead(n.id).pipe(
        takeUntilDestroyed(this.destroyRef)
      ).subscribe();
      this.notifications.update(list => list.map(x => x.id === n.id ? { ...x, read: true } : x));
      this.unreadCount.update(c => Math.max(0, c - 1));
    }
    this.closeNav();
    if (n.eventId) {
      this.router.navigate(['/events', n.eventId]);
    }
  }

  notifIcon(type: string): string {
    switch (type) {
      case 'EVENT_REMINDER': return 'bi-bell-fill text-primary';
      case 'EVENT_CANCELED': return 'bi-slash-circle-fill text-danger';
      case 'NEW_COMMENT': return 'bi-chat-fill text-info';
      case 'NEW_FOLLOWER': return 'bi-person-plus-fill text-success';
      case 'NEW_EVENT': return 'bi-calendar-plus-fill text-primary';
      case 'EVENT_UPDATED': return 'bi-pencil-square text-warning';
      default: return 'bi-info-circle-fill';
    }
  }

  private loadUnreadCount(): void {
    this.notificationApi.getUnreadNotificationCount().pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (count) => this.unreadCount.set(count),
      error: () => {}
    });
  }

  private updateNavbarHeight(): void {
    const height = this.navbarEl?.nativeElement.getBoundingClientRect().height;
    if (!height) {
      return;
    }

    this.document.documentElement.style.setProperty('--app-navbar-height', `${height.toFixed(2)}px`);
  }
}
