import { ChangeDetectionStrategy, Component, DestroyRef, computed, effect, inject, signal, untracked } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../../../core/auth/auth';
import { EventApi } from '../../../events/event-api';
import { FollowApi } from '../../follow-api';
import { ToastService } from '../../../../core/services/toast';
import { Event } from '../../../events/event.model';
import { User } from '../../../../core/auth/auth.model';
import { environment } from '../../../../../environments/environment';
import { toApiError } from '../../../../core/http/api-error';

@Component({
  selector: 'app-my-profile',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, ReactiveFormsModule, DatePipe],
  template: `
    <div class="container py-5">
      <div class="row justify-content-center">
        <div class="col-lg-8">

          @if (auth.user(); as user) {
            <!-- Profile header -->
            <div class="profile-header">
              <div class="profile-avatar">
                <i class="bi bi-person-fill"></i>
              </div>
              <div>
                <h1 class="profile-name">{{ user.name || 'User' }}</h1>
                <p class="profile-email">{{ user.email }}</p>
              </div>
            </div>

            <!-- Info cards -->
            <div class="row g-3 mb-4">
              <div class="col-md-4">
                <div class="info-card">
                  <span class="info-card-label">Role</span>
                  <span class="info-card-value">
                    @if (user.role === 'ADMIN') {
                      <span class="badge bg-danger">Admin</span>
                    } @else if (user.role === 'ORGANIZER') {
                      <span class="badge bg-warning text-dark">Organizer</span>
                      @if (user.artistName) {
                        <span class="badge bg-info text-dark ms-1">Artist</span>
                      }
                    } @else if (user.role === 'ARTIST') {
                      <span class="badge bg-info text-dark">Artist</span>
                    } @else {
                      <span class="badge bg-secondary">User</span>
                    }
                  </span>
                </div>
              </div>
              @if (isOrganizerOrAdmin()) {
                <div class="col-md-4">
                  <div class="info-card">
                    <span class="info-card-label">Organization</span>
                    <span class="info-card-value">{{ user.organizationName || '—' }}</span>
                  </div>
                </div>
              }
              @if (isArtist()) {
                <div class="col-md-4">
                  <div class="info-card">
                    <span class="info-card-label">Artist Name</span>
                    <span class="info-card-value">{{ user.artistName || '—' }}</span>
                  </div>
                </div>
              }
            </div>

            <!-- Organizer section -->
            <div class="organizer-section mb-3">
              @if (isOrganizerOrAdmin()) {
                <div class="d-flex align-items-start gap-3">
                  <div class="organizer-check-icon">
                    <i class="bi bi-patch-check-fill"></i>
                  </div>
                  <div class="flex-grow-1">
                    <h5 class="fw-bold mb-1">You're an Organizer</h5>
                    @if (!editingOrgName()) {
                      <p class="text-muted mb-1">
                        Organization: <strong>{{ user.organizationName || 'Not set' }}</strong>
                        <button class="btn btn-link btn-sm p-0 ms-2" (click)="startEditOrgName()">
                          <i class="bi bi-pencil"></i> Edit
                        </button>
                      </p>
                    } @else {
                      <div class="upgrade-form mt-2">
                        @if (orgNameError()) {
                          <div class="alert alert-danger py-2 mb-2">{{ orgNameError() }}</div>
                        }
                        <div class="mb-2">
                          <input
                            type="text"
                            class="form-control form-control-sm"
                            placeholder="Organization name"
                            [formControl]="newOrgNameControl"
                            [disabled]="orgNameBusy()">
                        </div>
                        <div class="d-flex gap-2">
                          <button class="btn btn-primary btn-sm" [disabled]="orgNameBusy() || !newOrgNameControl.value.trim()" (click)="updateOrganizationName()">
                            @if (orgNameBusy()) { <span class="spinner-border spinner-border-sm me-1"></span> }
                            Save
                          </button>
                          <button class="btn btn-outline-secondary btn-sm" [disabled]="orgNameBusy()" (click)="editingOrgName.set(false)">Cancel</button>
                        </div>
                      </div>
                    }
                    <!-- Organization description -->
                    @if (!editingOrgDescription()) {
                      <p class="text-muted mb-1">
                        @if (user.organizationDescription) {
                          <span class="d-block mb-1" style="white-space: pre-line;">{{ user.organizationDescription }}</span>
                        } @else {
                          <span class="fst-italic">No organization description yet.</span>
                        }
                        <button class="btn btn-link btn-sm p-0 ms-1" (click)="startEditOrgDescription()">
                          <i class="bi bi-pencil"></i> {{ user.organizationDescription ? 'Edit description' : 'Add description' }}
                        </button>
                      </p>
                    } @else {
                      <div class="upgrade-form mt-2 mb-2">
                        @if (orgDescriptionError()) {
                          <div class="alert alert-danger py-2 mb-2">{{ orgDescriptionError() }}</div>
                        }
                        <div class="mb-2">
                          <textarea
                            class="form-control form-control-sm"
                            rows="4"
                            placeholder="Describe your organization..."
                            maxlength="2000"
                            [formControl]="newOrgDescriptionControl"
                            [disabled]="orgDescriptionBusy()"></textarea>
                          <div class="form-text text-end">{{ newOrgDescriptionControl.value.length }} / 2000</div>
                        </div>
                        <div class="d-flex gap-2">
                          <button class="btn btn-primary btn-sm" [disabled]="orgDescriptionBusy()" (click)="updateOrganizationDescription()">
                            @if (orgDescriptionBusy()) { <span class="spinner-border spinner-border-sm me-1"></span> }
                            Save
                          </button>
                          <button class="btn btn-outline-secondary btn-sm" [disabled]="orgDescriptionBusy()" (click)="editingOrgDescription.set(false)">Cancel</button>
                        </div>
                      </div>
                    }
                    <p class="text-muted mb-0 small">You can <a routerLink="/events/create">create</a> and <a routerLink="/events/my">manage</a> events.</p>
                  </div>
                </div>
              } @else {
                <div class="d-flex align-items-start gap-3">
                  <div class="upgrade-icon">
                    <i class="bi bi-rocket-takeoff"></i>
                  </div>
                  <div class="flex-grow-1">
                    <h5 class="fw-bold mb-1">Become an Organizer</h5>
                    <p class="text-muted small mb-3">Upgrade your account to create and manage your own events.</p>

                    @if (upgradeError()) {
                      <div class="alert alert-danger py-2 mb-3 d-flex align-items-center">
                        <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ upgradeError() }}
                      </div>
                    }

                    @if (upgradeSuccess()) {
                      <div class="alert alert-success py-2 mb-0 d-flex align-items-center">
                        <i class="bi bi-check-circle-fill me-2"></i>You are now an Organizer! Refresh the page to see your new options.
                      </div>
                    } @else if (!confirmVisible()) {
                      <button
                        class="btn btn-primary"
                        [disabled]="upgradeBusy()"
                        (click)="confirmVisible.set(true)">
                        <i class="bi bi-arrow-up-circle me-1"></i>Become Organizer
                      </button>
                    } @else {
                      <div class="upgrade-form">
                        <div class="mb-3">
                          <label for="orgName" class="form-label">Organization Name <span class="text-danger">*</span></label>
                          <input
                            type="text"
                            class="form-control"
                            id="orgName"
                            placeholder="Enter your organization name"
                            [formControl]="organizationNameControl"
                            [disabled]="upgradeBusy()">
                          <div class="form-text">This will be displayed alongside your name on events.</div>
                        </div>
                        <div class="d-flex gap-2">
                          <button
                            class="btn btn-primary"
                            [disabled]="upgradeBusy() || !organizationNameControl.value.trim()"
                            (click)="becomeOrganizer()">
                            @if (upgradeBusy()) {
                              <span class="spinner-border spinner-border-sm me-1" role="status"></span>
                            }
                            <i class="bi bi-check-lg me-1"></i>Confirm Upgrade
                          </button>
                          <button
                            class="btn btn-outline-secondary"
                            [disabled]="upgradeBusy()"
                            (click)="confirmVisible.set(false)">
                            Cancel
                          </button>
                        </div>
                      </div>
                    }
                  </div>
                </div>
              }
            </div>

            <!-- Artist section -->
            <div class="organizer-section mb-3">
              @if (isArtist()) {
                <div class="d-flex align-items-start gap-3">
                  <div class="organizer-check-icon" style="background: rgba(37, 99, 235, 0.1); color: #3b82f6;">
                    <i class="bi bi-music-note-beamed"></i>
                  </div>
                  <div class="flex-grow-1">
                    <h5 class="fw-bold mb-1">You're an Artist</h5>
                    <p class="text-muted mb-2">Artist name: <strong>{{ user.artistName }}</strong></p>

                    <!-- Artist description -->
                    @if (!editingArtistDescription()) {
                      <p class="text-muted mb-2">
                        @if (user.artistDescription) {
                          <span class="d-block mb-1" style="white-space: pre-line;">{{ user.artistDescription }}</span>
                        } @else {
                          <span class="fst-italic">No artist description yet.</span>
                        }
                        <button class="btn btn-link btn-sm p-0 ms-1" (click)="startEditArtistDescription()">
                          <i class="bi bi-pencil"></i> {{ user.artistDescription ? 'Edit description' : 'Add description' }}
                        </button>
                      </p>
                    } @else {
                      <div class="upgrade-form mt-2 mb-2" style="background: rgba(37, 99, 235, 0.08); border-color: rgba(37, 99, 235, 0.2);">
                        @if (artistDescriptionError()) {
                          <div class="alert alert-danger py-2 mb-2">{{ artistDescriptionError() }}</div>
                        }
                        <div class="mb-2">
                          <textarea
                            class="form-control form-control-sm"
                            rows="4"
                            placeholder="Describe yourself as an artist..."
                            maxlength="2000"
                            [formControl]="newArtistDescriptionControl"
                            [disabled]="artistDescriptionBusy()"></textarea>
                          <div class="form-text text-end">{{ newArtistDescriptionControl.value.length }} / 2000</div>
                        </div>
                        <div class="d-flex gap-2">
                          <button class="btn btn-primary btn-sm" [disabled]="artistDescriptionBusy()" (click)="updateArtistDescription()">
                            @if (artistDescriptionBusy()) { <span class="spinner-border spinner-border-sm me-1"></span> }
                            Save
                          </button>
                          <button class="btn btn-outline-secondary btn-sm" [disabled]="artistDescriptionBusy()" (click)="editingArtistDescription.set(false)">Cancel</button>
                        </div>
                      </div>
                    }

                    @if (performancesLoading()) {
                      <div class="d-flex align-items-center gap-2 text-muted small">
                        <span class="spinner-border spinner-border-sm"></span> Loading performances...
                      </div>
                    } @else if (myPerformances().length > 0) {
                      <p class="text-muted small mb-2">Your upcoming performances:</p>
                      <div class="performance-list">
                        @for (event of myPerformances(); track event.id) {
                          <a [routerLink]="['/events', event.id]" class="performance-item">
                            <div class="d-flex justify-content-between align-items-center">
                              <span class="fw-medium">{{ event.name }}</span>
                              <span class="text-muted small">{{ event.eventDate | date:'mediumDate' }}</span>
                            </div>
                            <small class="text-muted"><i class="bi bi-geo-alt me-1"></i>{{ event.location }}</small>
                          </a>
                        }
                      </div>
                    } @else {
                      <p class="text-muted small mb-0">No performances yet. When organizers add you to events, they'll appear here.</p>
                    }
                  </div>
                </div>
              } @else {
                <div class="d-flex align-items-start gap-3">
                  <div class="upgrade-icon" style="background: rgba(37, 99, 235, 0.1); color: #3b82f6;">
                    <i class="bi bi-music-note-beamed"></i>
                  </div>
                  <div class="flex-grow-1">
                    <h5 class="fw-bold mb-1">Become an Artist</h5>
                    <p class="text-muted small mb-3">Register as an artist to be discoverable by event organizers.</p>

                    @if (artistError()) {
                      <div class="alert alert-danger py-2 mb-3 d-flex align-items-center">
                        <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ artistError() }}
                      </div>
                    }

                    @if (artistSuccess()) {
                      <div class="alert alert-success py-2 mb-0 d-flex align-items-center">
                        <i class="bi bi-check-circle-fill me-2"></i>You are now an Artist! Refresh the page to see your new options.
                      </div>
                    } @else if (!artistConfirmVisible()) {
                      <button
                        class="btn btn-primary"
                        [disabled]="artistBusy()"
                        (click)="artistConfirmVisible.set(true)">
                        <i class="bi bi-music-note me-1"></i>Become Artist
                      </button>
                    } @else {
                      <div class="upgrade-form" style="background: rgba(37, 99, 235, 0.08); border-color: rgba(37, 99, 235, 0.2);">
                        <div class="mb-3">
                          <label for="artistName" class="form-label">Artist / Stage Name <span class="text-danger">*</span></label>
                          <input
                            type="text"
                            class="form-control"
                            id="artistName"
                            placeholder="Enter your artist name"
                            [formControl]="artistNameControl"
                            [disabled]="artistBusy()">
                          <div class="form-text">This is how organizers and attendees will find you.</div>
                        </div>
                        <div class="d-flex gap-2">
                          <button
                            class="btn btn-primary"
                            [disabled]="artistBusy() || !artistNameControl.value.trim()"
                            (click)="becomeArtist()">
                            @if (artistBusy()) {
                              <span class="spinner-border spinner-border-sm me-1" role="status"></span>
                            }
                            <i class="bi bi-check-lg me-1"></i>Confirm
                          </button>
                          <button
                            class="btn btn-outline-secondary"
                            [disabled]="artistBusy()"
                            (click)="artistConfirmVisible.set(false)">
                            Cancel
                          </button>
                        </div>
                      </div>
                    }
                  </div>
                </div>
              }
            </div>

            <!-- Followers section (organizers) -->
            @if (isOrganizerOrAdmin()) {
              <div class="organizer-section mb-3">
                <div class="d-flex align-items-start gap-3">
                  <div class="organizer-check-icon" style="background: rgba(217, 119, 6, 0.1); color: #d97706;">
                    <i class="bi bi-people-fill"></i>
                  </div>
                  <div class="flex-grow-1">
                    <h5 class="fw-bold mb-1">Your Followers</h5>
                    <p class="text-muted small mb-2">{{ followerCount() }} follower{{ followerCount() === 1 ? '' : 's' }}</p>
                    @if (followersLoading()) {
                      <div class="d-flex align-items-center gap-2 text-muted small">
                        <span class="spinner-border spinner-border-sm"></span> Loading...
                      </div>
                    } @else if (myFollowers().length > 0) {
                      <div class="follower-list">
                        @for (follower of myFollowers(); track follower.id) {
                          <div class="follower-item">
                            <div class="follower-avatar"><i class="bi bi-person-fill"></i></div>
                            <div>
                              <span class="fw-medium">{{ follower.name }}</span>
                              <span class="text-muted small d-block">{{ follower.email }}</span>
                            </div>
                          </div>
                        }
                      </div>
                    } @else {
                      <p class="text-muted small mb-0">No followers yet. When users follow you, they'll see your events in their feed.</p>
                    }
                  </div>
                </div>
              </div>
            }

            <!-- Following section -->
            <div class="organizer-section mb-3">
              <div class="d-flex align-items-start gap-3">
                <div class="organizer-check-icon" style="background: rgba(111, 66, 193, 0.1); color: #8b5cf6;">
                  <i class="bi bi-person-heart"></i>
                </div>
                <div class="flex-grow-1">
                  <h5 class="fw-bold mb-1">Following</h5>
                  <p class="text-muted small mb-2">Organizers you follow — their events appear in your "Following" feed.</p>
                  @if (followingLoading()) {
                    <div class="d-flex align-items-center gap-2 text-muted small">
                      <span class="spinner-border spinner-border-sm"></span> Loading...
                    </div>
                  } @else if (myFollowing().length > 0) {
                    <div class="follower-list">
                      @for (followed of myFollowing(); track followed.id) {
                        <div class="follower-item">
                          <div class="follower-avatar" style="background: var(--ef-primary-50); color: var(--ef-primary)">
                            <i class="bi bi-person-fill"></i>
                          </div>
                          <div class="flex-grow-1">
                            <span class="fw-medium">{{ followed.organizationName || followed.name }}</span>
                            @if (followed.organizationName) {
                              <span class="text-muted small d-block">{{ followed.name }}</span>
                            }
                          </div>
                          <button class="btn btn-sm btn-outline-danger" (click)="onUnfollow(followed)">
                            <i class="bi bi-person-dash me-1"></i>Unfollow
                          </button>
                        </div>
                      }
                    </div>
                  } @else {
                    <p class="text-muted small mb-0">You're not following anyone yet. Follow organizers on event pages to see their new events here.</p>
                    <a routerLink="/events" class="btn btn-sm btn-outline-primary mt-2">
                      <i class="bi bi-compass me-1"></i>Browse Events
                    </a>
                  }
                </div>
              </div>
            </div>

            <!-- Quick actions for organizers/artists -->
            @if (isOrganizerOrAdmin() || isArtist()) {
              <div class="organizer-section mb-3">
                <div class="d-flex align-items-start gap-3">
                  <div class="organizer-check-icon" style="background: rgba(124, 58, 237, 0.1); color: #8b5cf6;">
                    <i class="bi bi-pencil-square"></i>
                  </div>
                  <div class="flex-grow-1">
                    <h5 class="fw-bold mb-1">Posts</h5>
                    <p class="text-muted small mb-2">Share updates, leaks, and announcements with your followers.</p>
                    <a [routerLink]="['/profile', user.id]" class="btn btn-primary btn-sm">
                      <i class="bi bi-plus-lg me-1"></i>Create Post
                    </a>
                    <a [routerLink]="['/profile', user.id]" class="btn btn-outline-secondary btn-sm ms-2">
                      <i class="bi bi-eye me-1"></i>View Public Profile
                    </a>
                  </div>
                </div>
              </div>
            }

            <!-- Actions -->
            <div class="d-flex gap-2 mt-4">
              <a routerLink="/events" class="btn btn-primary">
                <i class="bi bi-compass me-1"></i>Browse Events
              </a>
              <a [routerLink]="['/profile', user.id]" class="btn btn-outline-primary">
                <i class="bi bi-person-badge me-1"></i>Public Profile
              </a>
              <button class="btn btn-outline-secondary" (click)="auth.logout()">
                <i class="bi bi-box-arrow-right me-1"></i>Logout
              </button>
            </div>

          } @else {
            <div class="text-center py-5">
              <div class="lock-icon">
                <i class="bi bi-shield-lock"></i>
              </div>
              <h4 class="fw-bold mt-3">You are not logged in</h4>
              <p class="text-muted mb-4">Log in to view your profile.</p>
              <button class="btn btn-primary btn-lg" (click)="auth.login()">
                <i class="bi bi-box-arrow-in-right me-1"></i>Log in
              </button>
            </div>
          }
        </div>
      </div>
    </div>
  `,
  styles: `
    .profile-header {
      display: flex;
      align-items: center;
      gap: 1.25rem;
      margin-bottom: 2rem;
    }

    .profile-avatar {
      width: 72px;
      height: 72px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--ef-primary-dark), var(--ef-primary-light));
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 2rem;
      flex-shrink: 0;
    }

    .profile-name {
      font-size: 1.75rem;
      font-weight: 800;
      margin-bottom: 0.125rem;
    }

    .profile-email {
      color: var(--ef-text-muted);
      margin-bottom: 0;
    }

    .info-card {
      background: var(--ef-surface);
      border: 1px solid var(--ef-border-subtle);
      border-radius: var(--ef-radius-md);
      padding: 1rem 1.25rem;
      height: 100%;
    }

    .info-card-label {
      display: block;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      color: var(--ef-text-muted);
      margin-bottom: 0.375rem;
    }

    .info-card-value {
      font-size: 0.9375rem;
      font-weight: 600;
      color: var(--ef-text);
    }

    .organizer-section {
      background: var(--ef-surface);
      border: 1px solid var(--ef-border-subtle);
      border-radius: var(--ef-radius-lg);
      padding: 1.5rem;
      box-shadow: var(--ef-shadow-xs);
    }

    .organizer-check-icon {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: rgba(5, 150, 105, 0.1);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #059669;
      font-size: 1.25rem;
      flex-shrink: 0;
    }

    .upgrade-icon {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: var(--ef-primary-50);
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--ef-primary);
      font-size: 1.25rem;
      flex-shrink: 0;
    }

    .upgrade-form {
      background: var(--ef-primary-50);
      border: 1px solid var(--ef-primary-200);
      border-radius: var(--ef-radius-md);
      padding: 1.25rem;
    }

    .lock-icon {
      font-size: 3.5rem;
      color: var(--ef-text-light);
    }

    .follower-list {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .follower-item {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.5rem 0.75rem;
      background: var(--ef-bg);
      border-radius: var(--ef-radius-sm);
      border: 1px solid var(--ef-border-light);
    }

    .performance-list {
      display: flex;
      flex-direction: column;
      gap: 0.375rem;
    }

    .performance-item {
      display: block;
      padding: 0.5rem 0.75rem;
      background: var(--ef-bg);
      border: 1px solid var(--ef-border-light);
      border-radius: var(--ef-radius-sm);
      text-decoration: none;
      color: var(--ef-text);
      transition: background 0.15s ease;
    }

    .performance-item:hover {
      background: var(--ef-primary-50);
      color: var(--ef-text);
    }

    .follower-avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: rgba(217, 119, 6, 0.15);
      color: #d97706;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.875rem;
      flex-shrink: 0;
    }

    @media (max-width: 575.98px) {
      .profile-header {
        gap: 0.75rem;
        margin-bottom: 1.25rem;
      }

      .profile-avatar {
        width: 52px;
        height: 52px;
        font-size: 1.5rem;
      }

      .profile-name {
        font-size: 1.25rem;
      }

      .profile-email {
        font-size: 0.8rem;
      }

      .info-card {
        padding: 0.75rem;
      }

      .organizer-section {
        padding: 1rem;
      }
    }
  `
})
export class MyProfileComponent {
  readonly auth = inject(AuthService);
  private readonly http = inject(HttpClient);
  private readonly eventApi = inject(EventApi);
  private readonly followApi = inject(FollowApi);
  private readonly toast = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  // Become organizer
  readonly confirmVisible = signal(false);
  readonly upgradeBusy = signal(false);
  readonly upgradeSuccess = signal(false);
  readonly upgradeError = signal('');
  readonly organizationNameControl = new FormControl('', { nonNullable: true });

  // Edit organization name
  readonly editingOrgName = signal(false);
  readonly newOrgNameControl = new FormControl('', { nonNullable: true });
  readonly orgNameBusy = signal(false);
  readonly orgNameError = signal('');

  // Edit organization description
  readonly editingOrgDescription = signal(false);
  readonly newOrgDescriptionControl = new FormControl('', { nonNullable: true });
  readonly orgDescriptionBusy = signal(false);
  readonly orgDescriptionError = signal('');

  // Edit artist description
  readonly editingArtistDescription = signal(false);
  readonly newArtistDescriptionControl = new FormControl('', { nonNullable: true });
  readonly artistDescriptionBusy = signal(false);
  readonly artistDescriptionError = signal('');

  // Become artist
  readonly artistConfirmVisible = signal(false);
  readonly artistBusy = signal(false);
  readonly artistSuccess = signal(false);
  readonly artistError = signal('');
  readonly artistNameControl = new FormControl('', { nonNullable: true });

  // Artist performances
  readonly myPerformances = signal<Event[]>([]);
  readonly performancesLoading = signal(false);

  // Following / Followers
  readonly myFollowing = signal<User[]>([]);
  readonly followingLoading = signal(false);
  readonly myFollowers = signal<User[]>([]);
  readonly followerCount = signal(0);
  readonly followersLoading = signal(false);

  readonly isOrganizerOrAdmin = computed(() => {
    const user = this.auth.user();
    return user?.role === 'ORGANIZER' || user?.role === 'ADMIN';
  });

  readonly isArtist = computed(() => {
    const user = this.auth.user();
    return !!user?.artistName || user?.role === 'ARTIST' || user?.role === 'ADMIN';
  });

  private dataLoaded = false;

  constructor() {
    effect(() => {
      const user = this.auth.user();
      if (!user || this.dataLoaded) return;
      this.dataLoaded = true;
      untracked(() => this.loadProfileData());
    });
  }

  private loadProfileData(): void {
      // Load performances for artists
      if (this.isArtist()) {
        this.performancesLoading.set(true);
        this.eventApi.getMyPerformances().pipe(
          takeUntilDestroyed(this.destroyRef)
        ).subscribe({
          next: (events) => {
            this.myPerformances.set(events);
            this.performancesLoading.set(false);
          },
          error: () => this.performancesLoading.set(false)
        });
      }

      // Load who I'm following
      this.followingLoading.set(true);
      this.followApi.getMyFollowing().pipe(
        takeUntilDestroyed(this.destroyRef)
      ).subscribe({
        next: (users) => {
          this.myFollowing.set(users);
          this.followingLoading.set(false);
        },
        error: () => this.followingLoading.set(false)
      });

      // Load my followers (for organizers)
      if (this.isOrganizerOrAdmin()) {
        this.followersLoading.set(true);
        this.followApi.getMyFollowers().pipe(
          takeUntilDestroyed(this.destroyRef)
        ).subscribe({
          next: (data) => {
            this.myFollowers.set(data.followers);
            this.followerCount.set(data.count);
            this.followersLoading.set(false);
          },
          error: () => this.followersLoading.set(false)
        });
      }
  }

  becomeOrganizer(): void {
    this.upgradeBusy.set(true);
    this.upgradeError.set('');

    this.http.post<void>(`${environment.beUrl}/users/me/become-organizer`, {
      organizationName: this.organizationNameControl.value.trim()
    }).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => {
        this.upgradeBusy.set(false);
        this.upgradeSuccess.set(true);
        this.confirmVisible.set(false);
        this.auth.refreshRole();
      },
      error: (error: unknown) => {
        const apiError = toApiError(error);
        this.upgradeBusy.set(false);
        if (apiError.status === 409) {
          this.upgradeError.set('You already have the Organizer role.');
        } else {
          this.upgradeError.set(apiError.message);
        }
      }
    });
  }

  becomeArtist(): void {
    this.artistBusy.set(true);
    this.artistError.set('');

    this.http.post<void>(`${environment.beUrl}/users/me/become-artist`, {
      artistName: this.artistNameControl.value.trim()
    }).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => {
        this.artistBusy.set(false);
        this.artistSuccess.set(true);
        this.artistConfirmVisible.set(false);
        this.auth.refreshRole();
      },
      error: (error: unknown) => {
        const apiError = toApiError(error);
        this.artistBusy.set(false);
        if (apiError.status === 409) {
          this.artistError.set('You already have a specialized role.');
        } else {
          this.artistError.set(apiError.message);
        }
      }
    });
  }

  startEditOrgName(): void {
    const user = this.auth.user();
    this.newOrgNameControl.setValue(user?.organizationName || '');
    this.editingOrgName.set(true);
    this.orgNameError.set('');
  }

  onUnfollow(user: User): void {
    this.followApi.unfollowUser(user.id).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => {
        this.myFollowing.update(list => list.filter(u => u.id !== user.id));
        this.toast.success(`Unfollowed ${user.organizationName || user.name}.`);
      },
      error: () => this.toast.error('Failed to unfollow.')
    });
  }

  updateOrganizationName(): void {
    this.orgNameBusy.set(true);
    this.orgNameError.set('');

    this.http.put<void>(`${environment.beUrl}/users/me/organization-name`, {
      organizationName: this.newOrgNameControl.value.trim()
    }).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => {
        this.orgNameBusy.set(false);
        this.editingOrgName.set(false);
        this.auth.refreshRole();
      },
      error: (error: unknown) => {
        this.orgNameBusy.set(false);
        this.orgNameError.set(toApiError(error).message);
      }
    });
  }

  startEditOrgDescription(): void {
    const user = this.auth.user();
    this.newOrgDescriptionControl.setValue(user?.organizationDescription || '');
    this.editingOrgDescription.set(true);
    this.orgDescriptionError.set('');
  }

  updateOrganizationDescription(): void {
    this.orgDescriptionBusy.set(true);
    this.orgDescriptionError.set('');

    this.http.put<void>(`${environment.beUrl}/users/me/organization-description`, {
      organizationDescription: this.newOrgDescriptionControl.value.trim() || null
    }).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => {
        this.orgDescriptionBusy.set(false);
        this.editingOrgDescription.set(false);
        this.auth.refreshRole();
      },
      error: (error: unknown) => {
        this.orgDescriptionBusy.set(false);
        this.orgDescriptionError.set(toApiError(error).message);
      }
    });
  }

  startEditArtistDescription(): void {
    const user = this.auth.user();
    this.newArtistDescriptionControl.setValue(user?.artistDescription || '');
    this.editingArtistDescription.set(true);
    this.artistDescriptionError.set('');
  }

  updateArtistDescription(): void {
    this.artistDescriptionBusy.set(true);
    this.artistDescriptionError.set('');

    this.http.put<void>(`${environment.beUrl}/users/me/artist-description`, {
      artistDescription: this.newArtistDescriptionControl.value.trim() || null
    }).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => {
        this.artistDescriptionBusy.set(false);
        this.editingArtistDescription.set(false);
        this.auth.refreshRole();
      },
      error: (error: unknown) => {
        this.artistDescriptionBusy.set(false);
        this.artistDescriptionError.set(toApiError(error).message);
      }
    });
  }
}
