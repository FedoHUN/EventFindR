import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { provideRouter } from '@angular/router';
import { App } from './app';
import { AuthService } from './core/auth/auth';
import { NotificationApi } from './core/notifications/notification-api';

describe('App', () => {
  beforeEach(async () => {
    const anonymousUser = signal(undefined);

    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            user: anonymousUser.asReadonly(),
            isOrganizer: () => false,
            login: () => undefined,
            register: () => undefined,
            logout: () => undefined,
          }
        },
        {
          provide: NotificationApi,
          useValue: {
            getUnreadNotificationCount: () => undefined,
          }
        }
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render application shell', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.navbar-brand')?.textContent).toContain('EventfindR');
    expect(compiled.querySelector('router-outlet')).toBeTruthy();
  });
});
