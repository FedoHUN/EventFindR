import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/auth/auth';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-my-profile',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  template: `
    <div class="container py-5">
      <div class="row justify-content-center">
        <div class="col-lg-8">
          <h1 class="fw-bold mb-4"><i class="bi bi-person-circle me-2"></i>My Profile</h1>

          @if (auth.user(); as user) {
            <div class="card border-0 shadow-sm">
              <div class="card-body p-4">
                <div class="d-flex align-items-center mb-4">
                  <div class="rounded-circle bg-primary d-flex align-items-center justify-content-center me-3" style="width: 64px; height: 64px;">
                    <i class="bi bi-person-fill text-white fs-3"></i>
                  </div>
                  <div>
                    <h4 class="fw-bold mb-0">{{ user.name || 'User' }}</h4>
                    <p class="text-muted mb-0">Personal account details</p>
                  </div>
                </div>

                <div class="row g-3">
                  <div class="col-md-6">
                    <div class="p-3 rounded bg-light border">
                      <small class="text-muted d-block">Full name</small>
                      <strong>{{ user.name || 'Not provided' }}</strong>
                    </div>
                  </div>
                  <div class="col-md-6">
                    <div class="p-3 rounded bg-light border">
                      <small class="text-muted d-block">Email</small>
                      <strong>{{ user.email || 'Not provided' }}</strong>
                    </div>
                  </div>
                  <div class="col-md-6">
                    <div class="p-3 rounded bg-light border">
                      <small class="text-muted d-block">Role</small>
                      <strong>{{ user.rola || 'USER' }}</strong>
                    </div>
                  </div>
                  <div class="col-md-6">
                    <div class="p-3 rounded bg-light border">
                      <small class="text-muted d-block">Authentication</small>
                      <strong>Logged in</strong>
                    </div>
                  </div>
                </div>

                <div class="card border-0 bg-light mt-4">
                  <div class="card-body">
                    @if (isOrganizerOrAdmin()) {
                      <div class="d-flex align-items-center">
                        <i class="bi bi-patch-check-fill text-success fs-4 me-3"></i>
                        <div>
                          <h5 class="fw-bold mb-1">You're an Organizer</h5>
                          <p class="text-muted mb-0">You can create and manage events.</p>
                        </div>
                      </div>
                    } @else {
                      <h5 class="fw-bold mb-2"><i class="bi bi-megaphone me-2"></i>Become an Organizer</h5>
                      <p class="text-muted mb-3">Upgrade your account to create and manage your own events.</p>

                      @if (upgradeError()) {
                        <div class="alert alert-danger py-2 mb-3">
                          <i class="bi bi-exclamation-triangle me-1"></i>{{ upgradeError() }}
                        </div>
                      }

                      @if (upgradeSuccess()) {
                        <div class="alert alert-success py-2 mb-0">
                          <i class="bi bi-check-circle me-1"></i>You are now an Organizer!
                        </div>
                      } @else if (!confirmVisible()) {
                        <button
                          class="btn btn-primary"
                          [disabled]="upgradeBusy()"
                          (click)="confirmVisible.set(true)">
                          <i class="bi bi-arrow-up-circle me-1"></i>Become Organizer
                        </button>
                      } @else {
                        <div class="d-flex align-items-center gap-2">
                          <span class="text-muted">Are you sure?</span>
                          <button
                            class="btn btn-success btn-sm"
                            [disabled]="upgradeBusy()"
                            (click)="becomeOrganizer()">
                            @if (upgradeBusy()) {
                              <span class="spinner-border spinner-border-sm me-1" role="status"></span>
                            }
                            Yes, upgrade
                          </button>
                          <button
                            class="btn btn-outline-secondary btn-sm"
                            [disabled]="upgradeBusy()"
                            (click)="confirmVisible.set(false)">
                            Cancel
                          </button>
                        </div>
                      }
                    }
                  </div>
                </div>

                <div class="d-flex gap-2 mt-4">
                  <a routerLink="/events" class="btn btn-primary">
                    <i class="bi bi-calendar-event me-1"></i>Browse Events
                  </a>
                  <button class="btn btn-outline-secondary" (click)="auth.logout()">
                    <i class="bi bi-box-arrow-right me-1"></i>Logout
                  </button>
                </div>
              </div>
            </div>
          } @else {
            <div class="card border-0 shadow-sm">
              <div class="card-body p-4 text-center">
                <i class="bi bi-shield-lock text-primary fs-1"></i>
                <h4 class="fw-bold mt-3">You are not logged in</h4>
                <p class="text-muted mb-4">Log in to view your personal profile details.</p>
                <button class="btn btn-primary" (click)="auth.login()">
                  <i class="bi bi-box-arrow-in-right me-1"></i>Login
                </button>
              </div>
            </div>
          }
        </div>
      </div>
    </div>
  `
})
export class MyProfileComponent {
  readonly auth = inject(AuthService);
  private readonly http = inject(HttpClient);

  readonly confirmVisible = signal(false);
  readonly upgradeBusy = signal(false);
  readonly upgradeSuccess = signal(false);
  readonly upgradeError = signal('');

  readonly isOrganizerOrAdmin = computed(() => {
    const user = this.auth.user();
    return user?.rola === 'ORGANIZER' || user?.rola === 'ADMIN';
  });

  becomeOrganizer(): void {
    this.upgradeBusy.set(true);
    this.upgradeError.set('');

    this.http.post<void>(`${environment.beUrl}/users/me/become-organizer`, {}).subscribe({
      next: () => {
        this.upgradeBusy.set(false);
        this.upgradeSuccess.set(true);
        this.confirmVisible.set(false);
        this.auth.refreshRole();
      },
      error: (err) => {
        this.upgradeBusy.set(false);
        if (err.status === 409) {
          this.upgradeError.set('You already have the Organizer role.');
        } else {
          this.upgradeError.set('Something went wrong. Please try again.');
        }
      }
    });
  }
}
