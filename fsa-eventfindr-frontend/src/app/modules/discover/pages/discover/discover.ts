import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';
import { EventApi } from '../../../events/event-api';
import { UserApi } from '../../../profile/user-api';
import { StarRatingComponent } from '../../../../core/components/star-rating';
import { Event } from '../../../events/event.model';
import { User } from '../../../../core/auth/auth.model';

interface OrganizerCard {
  user: User;
  eventCount: number;
  avgRating: number | null;
  upcomingEvents: number;
}

@Component({
  selector: 'app-discover',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, ReactiveFormsModule, StarRatingComponent],
  template: `
    <div class="container py-5">
      <div class="page-header mb-4">
        <h1 class="fw-bold">Discover</h1>
        <p class="text-muted">Find organizers and artists behind the events you love</p>
      </div>

      <!-- Tabs -->
      <ul class="nav nav-pills ef-tabs mb-4" role="tablist">
        <li class="nav-item" role="presentation">
          <button
            class="nav-link"
            [class.active]="activeTab() === 'organizers'"
            (click)="activeTab.set('organizers')"
            role="tab"
            [attr.aria-selected]="activeTab() === 'organizers'">
            <i class="bi bi-building me-1"></i>Organizers
            <span class="tab-count">{{ filteredOrganizers().length }}</span>
          </button>
        </li>
        <li class="nav-item" role="presentation">
          <button
            class="nav-link"
            [class.active]="activeTab() === 'artists'"
            (click)="activeTab.set('artists')"
            role="tab"
            [attr.aria-selected]="activeTab() === 'artists'">
            <i class="bi bi-music-note me-1"></i>Artists
            <span class="tab-count">{{ filteredArtists().length }}</span>
          </button>
        </li>
      </ul>

      <!-- Search -->
      <div class="search-bar mb-4">
        <div class="input-group">
          <span class="input-group-text border-end-0">
            <i class="bi bi-search text-muted"></i>
          </span>
          <input
            type="text"
            class="form-control border-start-0 ps-0"
            [placeholder]="activeTab() === 'organizers' ? 'Search organizers...' : 'Search artists...'"
            [formControl]="searchControl"
            aria-label="Search">
        </div>
      </div>

      @if (loading()) {
        <div class="text-center py-5">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
        </div>
      } @else if (loadError()) {
        <div class="alert alert-danger" role="alert">
          <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ loadError() }}
        </div>
      } @else {
        <!-- Organizers Tab -->
        @if (activeTab() === 'organizers') {
          @if (filteredOrganizers().length === 0) {
            <div class="text-center py-5">
              <i class="bi bi-building display-4 text-muted"></i>
              <h5 class="fw-bold mt-3">No organizers found</h5>
              <p class="text-muted">Try a different search term</p>
            </div>
          } @else {
            <div class="row g-4">
              @for (org of filteredOrganizers(); track org.user.id) {
                <div class="col-sm-6 col-lg-4 col-xl-3">
                  <a [routerLink]="['/profile', org.user.id]" class="text-decoration-none">
                    <div class="card border-0 h-100 discover-card">
                      <div class="card-body text-center py-4">
                        <div class="discover-avatar mx-auto mb-3">
                          <i class="bi bi-person-fill"></i>
                        </div>
                        <h6 class="fw-bold mb-1 text-body">{{ org.user.name }}</h6>
                        @if (org.user.organizationName) {
                          <p class="text-muted small mb-1"><i class="bi bi-building me-1"></i>{{ org.user.organizationName }}</p>
                        }
                        @if (org.user.artistName) {
                          <p class="text-muted small mb-1"><i class="bi bi-music-note me-1"></i>{{ org.user.artistName }}</p>
                        }
                        <div class="d-flex justify-content-center gap-2 mb-2">
                          <span class="role-badge role-badge--organizer">
                            <i class="bi bi-patch-check-fill me-1"></i>Organizer
                          </span>
                          @if (org.user.artistName) {
                            <span class="role-badge role-badge--artist">
                              <i class="bi bi-music-note me-1"></i>Artist
                            </span>
                          }
                        </div>
                        @if (org.user.organizationDescription) {
                          <p class="discover-description text-muted small mb-3">{{ org.user.organizationDescription }}</p>
                        }
                        <div class="discover-stats">
                          <div class="discover-stat">
                            <span class="discover-stat-value">{{ org.eventCount }}</span>
                            <span class="discover-stat-label">Events</span>
                          </div>
                          @if (org.avgRating) {
                            <div class="discover-stat">
                              <div class="discover-stat-value">
                                <app-star-rating [value]="org.avgRating" size="sm" />
                              </div>
                              <span class="discover-stat-label">{{ org.avgRating.toFixed(1) }} avg</span>
                            </div>
                          }
                          <div class="discover-stat">
                            <span class="discover-stat-value">{{ org.upcomingEvents }}</span>
                            <span class="discover-stat-label">Upcoming</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </a>
                </div>
              }
            </div>
          }
        }

        <!-- Artists Tab -->
        @if (activeTab() === 'artists') {
          @if (filteredArtists().length === 0) {
            <div class="text-center py-5">
              <i class="bi bi-music-note-beamed display-4 text-muted"></i>
              <h5 class="fw-bold mt-3">No artists found</h5>
              <p class="text-muted">Try a different search term</p>
            </div>
          } @else {
            <div class="row g-4">
              @for (artist of filteredArtists(); track artist.id) {
                <div class="col-sm-6 col-lg-4 col-xl-3">
                  <a [routerLink]="['/profile', artist.id]" class="text-decoration-none">
                    <div class="card border-0 h-100 discover-card">
                      <div class="card-body text-center py-4">
                        <div class="discover-avatar discover-avatar--artist mx-auto mb-3">
                          <i class="bi bi-music-note-beamed"></i>
                        </div>
                        <h6 class="fw-bold mb-1 text-body">{{ artist.name }}</h6>
                        @if (artist.artistName) {
                          <p class="text-muted small mb-1"><i class="bi bi-music-note me-1"></i>{{ artist.artistName }}</p>
                        }
                        @if (artist.organizationName) {
                          <p class="text-muted small mb-1"><i class="bi bi-building me-1"></i>{{ artist.organizationName }}</p>
                        }
                        <div class="d-flex justify-content-center gap-2 mb-2">
                          <span class="role-badge role-badge--artist">
                            <i class="bi bi-music-note me-1"></i>Artist
                          </span>
                          @if (artist.role === 'ORGANIZER' || artist.role === 'ADMIN') {
                            <span class="role-badge role-badge--organizer">
                              <i class="bi bi-patch-check-fill me-1"></i>Organizer
                            </span>
                          }
                        </div>
                        @if (artist.artistDescription) {
                          <p class="discover-description text-muted small mb-2">{{ artist.artistDescription }}</p>
                        }
                        @if (getArtistPerformanceCount(artist); as count) {
                          <p class="text-muted small mb-0">
                            <i class="bi bi-calendar-event me-1"></i>{{ count }} performance{{ count === 1 ? '' : 's' }}
                          </p>
                        }
                      </div>
                    </div>
                  </a>
                </div>
              }
            </div>
          }
        }
      }
    </div>
  `,
  styles: `
    .page-header h1 {
      font-size: 2rem;
      letter-spacing: -0.02em;
    }

    .ef-tabs {
      gap: 0.5rem;
    }

    .ef-tabs .nav-link {
      border-radius: 0.5rem;
      font-weight: 600;
      font-size: 0.9rem;
      color: var(--ef-text-muted);
      padding: 0.5rem 1rem;
      transition: all 0.15s ease;
    }

    .ef-tabs .nav-link.active {
      background: var(--ef-primary-dark);
      color: #fff;
    }

    .ef-tabs .nav-link:not(.active):hover {
      background: var(--ef-primary-50);
      color: var(--ef-primary);
    }

    .tab-count {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 22px;
      height: 22px;
      border-radius: 11px;
      font-size: 0.75rem;
      font-weight: 700;
      margin-left: 0.375rem;
      padding: 0 6px;
      background: rgba(128, 128, 128, 0.15);
    }

    .nav-link.active .tab-count {
      background: rgba(255, 255, 255, 0.25);
    }

    .search-bar {
      max-width: 400px;
    }

    .discover-card {
      border-radius: var(--ef-radius-lg);
      box-shadow: var(--ef-shadow-sm);
      transition: transform 0.35s cubic-bezier(0.22, 1, 0.36, 1),
                  box-shadow 0.35s cubic-bezier(0.22, 1, 0.36, 1);
    }

    .discover-card:hover {
      transform: translateY(-6px);
      box-shadow: 0 16px 32px -8px rgba(111, 66, 193, 0.12),
                  var(--ef-shadow-xl);
    }

    .discover-card:hover .discover-avatar {
      transform: scale(1.08);
    }

    .discover-avatar {
      width: 64px;
      height: 64px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--ef-primary-dark), var(--ef-primary-light));
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 1.75rem;
      transition: transform 0.35s cubic-bezier(0.22, 1, 0.36, 1);
    }

    .discover-avatar--artist {
      background: linear-gradient(135deg, #6f42c1, #a78bfa);
    }

    .role-badge {
      display: inline-block;
      font-size: 0.7rem;
      font-weight: 600;
      padding: 0.2rem 0.6rem;
      border-radius: 1rem;
    }

    .role-badge--organizer {
      color: #059669;
      background: rgba(5, 150, 105, 0.1);
    }

    .role-badge--artist {
      color: var(--ef-primary);
      background: var(--ef-primary-50);
    }

    .discover-stats {
      display: flex;
      justify-content: center;
      gap: 1rem;
      padding-top: 0.75rem;
      border-top: 1px solid var(--ef-border-light);
    }

    .discover-stat {
      text-align: center;
    }

    .discover-stat-value {
      display: block;
      font-size: 1.1rem;
      font-weight: 700;
      color: var(--ef-text);
    }

    .discover-stat-label {
      display: block;
      font-size: 0.7rem;
      color: var(--ef-text-muted);
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }

    .discover-description {
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
      overflow: hidden;
      text-align: center;
      line-height: 1.4;
    }

    @media (max-width: 575.98px) {
      :host {
        display: block;
        overflow-x: clip;
      }

      .page-header h1 {
        font-size: 1.4rem;
      }

      .ef-tabs .nav-link {
        font-size: 0.8rem;
        padding: 0.375rem 0.625rem;
      }

      .tab-count {
        min-width: 18px;
        height: 18px;
        font-size: 0.65rem;
      }

      .search-bar {
        max-width: none;
      }

      .discover-card .card-body {
        padding: 0.75rem 0.5rem;
      }

      .discover-card:hover {
        transform: none;
      }

      .discover-avatar {
        width: 48px;
        height: 48px;
        font-size: 1.25rem;
      }

      .role-badge {
        font-size: 0.6rem;
        padding: 0.15rem 0.4rem;
      }

      .discover-stats {
        gap: 0.5rem;
      }

      .discover-stat-value {
        font-size: 0.9rem;
      }

      .discover-stat-label {
        font-size: 0.6rem;
      }
    }
  `
})
export class DiscoverComponent implements OnInit {
  private readonly eventApi = inject(EventApi);
  private readonly userApi = inject(UserApi);
  private readonly destroyRef = inject(DestroyRef);

  readonly loading = signal(true);
  readonly loadError = signal('');
  readonly activeTab = signal<'organizers' | 'artists'>('organizers');
  readonly searchQuery = signal('');
  readonly searchControl = new FormControl('', { nonNullable: true });

  private readonly organizers = signal<User[]>([]);
  private readonly artists = signal<User[]>([]);
  private readonly allEvents = signal<Event[]>([]);

  readonly organizerCards = computed<OrganizerCard[]>(() => {
    const orgs = this.organizers();
    const events = this.allEvents();
    const now = Date.now();

    return orgs.map(user => {
      const orgEvents = events.filter(e => e.organizer?.id === user.id && !e.canceled);
      const ratings = orgEvents.filter(e => e.averageRating).map(e => e.averageRating!);
      const avgRating = ratings.length > 0
        ? ratings.reduce((sum, r) => sum + r, 0) / ratings.length
        : null;
      const upcomingEvents = orgEvents.filter(e => new Date(e.eventDate).getTime() > now).length;

      return { user, eventCount: orgEvents.length, avgRating, upcomingEvents };
    }).sort((a, b) => b.eventCount - a.eventCount);
  });

  readonly filteredOrganizers = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    if (!query) return this.organizerCards();
    return this.organizerCards().filter(o =>
      (o.user.organizationName?.toLowerCase().includes(query)) ||
      o.user.name.toLowerCase().includes(query) ||
      (o.user.organizationDescription?.toLowerCase().includes(query))
    );
  });

  readonly filteredArtists = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    const list = this.artists();
    if (!query) return list;
    return list.filter(a =>
      (a.artistName?.toLowerCase().includes(query)) ||
      a.name.toLowerCase().includes(query) ||
      (a.artistDescription?.toLowerCase().includes(query))
    );
  });

  ngOnInit(): void {
    this.searchControl.valueChanges.pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(value => this.searchQuery.set(value));

    forkJoin({
      organizers: this.userApi.getOrganizers(),
      artists: this.userApi.getAllArtists(),
      events: this.eventApi.getAllEvents()
    }).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: ({ organizers, artists, events }) => {
        this.organizers.set(organizers);
        this.artists.set(artists);
        this.allEvents.set(events);
        this.loadError.set('');
        this.loading.set(false);
      },
      error: () => {
        this.loadError.set('Could not load discovery data. Please try again later.');
        this.loading.set(false);
      }
    });
  }

  getArtistPerformanceCount(artist: User): number {
    return this.allEvents().filter(e =>
      !e.canceled && e.artists?.some(a => a.artistUserId === artist.id)
    ).length;
  }
}
