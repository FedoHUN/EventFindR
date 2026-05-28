import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { EventApi } from '../../../events/event-api';
import { AuthService } from '../../../../core/auth/auth';
import { ToastService } from '../../../../core/services/toast';
import { StarRatingComponent } from '../../../../core/components/star-rating';
import { Event, FollowStatus, Post } from '../../../events/event.model';
import { User } from '../../../../core/auth/auth.model';
import { UserApi } from '../../user-api';
import { PostApi } from '../../post-api';
import { FollowApi } from '../../follow-api';
import { toApiError } from '../../../../core/http/api-error';

interface MediaUploadItem {
  file: File;
  progress: number;
  done: boolean;
  error: boolean;
}

@Component({
  selector: 'app-public-profile',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe, CurrencyPipe, ReactiveFormsModule, StarRatingComponent],
  template: `
    <div class="container py-5">
      <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb">
          <li class="breadcrumb-item"><a routerLink="/events">Events</a></li>
          <li class="breadcrumb-item"><a routerLink="/discover">Discover</a></li>
          @if (profile()) {
            <li class="breadcrumb-item active" aria-current="page">{{ displayName() }}</li>
          }
        </ol>
      </nav>

      @if (loading()) {
        <div class="text-center py-5">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
        </div>
      } @else if (!profile()) {
        <div class="text-center py-5">
          <div class="empty-icon"><i class="bi bi-person-x"></i></div>
          <h4 class="fw-bold mt-3">Profile not found</h4>
          <p class="text-muted">This user may not exist or their profile is not public.</p>
          <a routerLink="/events" class="btn btn-primary">Browse Events</a>
        </div>
      } @else {
        <div class="row g-4 g-lg-5">
          <!-- Profile sidebar -->
          <div class="col-lg-4">
            <div class="profile-card">
              <div class="profile-avatar">
                <i class="bi bi-person-fill"></i>
              </div>
              <h2 class="profile-name">{{ displayName() }}</h2>
              @if (profile()!.organizationName) {
                <p class="profile-subtitle mb-1">
                  <i class="bi bi-building me-1"></i>{{ profile()!.organizationName }}
                </p>
              }
              @if (profile()!.artistName) {
                <p class="profile-subtitle mb-1">
                  <i class="bi bi-music-note me-1"></i>{{ profile()!.artistName }}
                </p>
              }

              <div class="d-flex flex-wrap justify-content-center gap-2 mb-3 mt-2">
                @if (profile()!.role === 'ADMIN') {
                  <span class="badge bg-danger">Admin</span>
                }
                @if (profile()!.role === 'ORGANIZER' || profile()!.role === 'ADMIN') {
                  <span class="badge bg-warning text-dark">Organizer</span>
                }
                @if (profile()!.role === 'ARTIST' || profile()!.artistName) {
                  <span class="badge bg-info text-dark">Artist</span>
                }
              </div>

              @if (profile()!.organizationDescription) {
                <div class="profile-description mb-3">
                  <h6 class="fw-bold small text-uppercase text-muted mb-1">About the Organization</h6>
                  <p class="small mb-0" style="white-space: pre-line;">{{ profile()!.organizationDescription }}</p>
                </div>
              }
              @if (profile()!.artistDescription) {
                <div class="profile-description mb-3">
                  <h6 class="fw-bold small text-uppercase text-muted mb-1">About the Artist</h6>
                  <p class="small mb-0" style="white-space: pre-line;">{{ profile()!.artistDescription }}</p>
                </div>
              }

              <div class="profile-stats">
                <div class="profile-stat">
                  <span class="profile-stat-value">{{ events().length }}</span>
                  <span class="profile-stat-label">Events</span>
                </div>
                <div class="profile-stat">
                  <span class="profile-stat-value">{{ followStatus()?.followerCount ?? 0 }}</span>
                  <span class="profile-stat-label">Followers</span>
                </div>
                @if (avgRating()) {
                  <div class="profile-stat">
                    <span class="profile-stat-value">{{ avgRating()!.toFixed(1) }}</span>
                    <span class="profile-stat-label">Avg Rating</span>
                  </div>
                }
              </div>

              <div class="follow-section">
                @if (followStatus(); as fs) {
                  @if (!isOwnProfile()) {
                    <button
                      class="btn w-100"
                      [class.btn-primary]="!fs.following"
                      [class.btn-outline-primary]="fs.following"
                      [disabled]="followBusy()"
                      (click)="toggleFollow()">
                      @if (fs.following) {
                        <i class="bi bi-person-check me-1"></i>Following
                      } @else {
                        <i class="bi bi-person-plus me-1"></i>Follow
                      }
                    </button>
                  }
                } @else if (!auth.user()) {
                  <button class="btn btn-primary w-100" (click)="auth.login()">
                    <i class="bi bi-person-plus me-1"></i>Log in to follow
                  </button>
                }
              </div>
            </div>
          </div>

          <!-- Main content -->
          <div class="col-lg-8">

            <!-- ═══ POSTS SECTION ═══ -->

            <!-- Create post form (own profile, organizer/artist only) -->
            @if (isOwnProfile() && canPost()) {
              <div class="post-create-card mb-4">
                <div class="post-create-header" (click)="createFormOpen.set(!createFormOpen())">
                  <i class="bi bi-pencil-square me-2"></i>
                  <span>Create a post</span>
                  <i class="bi ms-auto" [class.bi-chevron-down]="!createFormOpen()" [class.bi-chevron-up]="createFormOpen()"></i>
                </div>
                @if (createFormOpen()) {
                  <div class="post-create-body">
                    <textarea
                      class="form-control mb-2"
                      rows="3"
                      maxlength="5000"
                      placeholder="Share something with your followers..."
                      [formControl]="postContentControl"
                      [disabled]="posting()"></textarea>
                    <div class="d-flex justify-content-between align-items-center mb-2">
                      <span class="text-muted small">{{ postContentControl.value.length }} / 5000</span>
                    </div>

                    <!-- Selected files preview -->
                    @if (pendingFiles().length > 0) {
                      <div class="selected-files mb-3">
                        @for (item of pendingFiles(); track $index) {
                          <div class="selected-file">
                            <i class="bi me-2" [class.bi-image]="item.file.type.startsWith('image')" [class.bi-camera-video]="item.file.type.startsWith('video')"></i>
                            <span class="file-name">{{ item.file.name }}</span>
                            <span class="text-muted small ms-2">({{ (item.file.size / 1024 / 1024).toFixed(1) }} MB)</span>
                            @if (item.done) {
                              <i class="bi bi-check-circle-fill text-success ms-auto"></i>
                            } @else if (item.error) {
                              <i class="bi bi-x-circle-fill text-danger ms-auto"></i>
                            } @else if (posting()) {
                              <div class="upload-progress ms-auto">
                                <div class="progress" style="width: 80px; height: 6px;">
                                  <div class="progress-bar" [style.width.%]="item.progress"></div>
                                </div>
                              </div>
                            } @else {
                              <button class="btn btn-sm btn-link text-danger ms-auto p-0" (click)="removeFile($index)">
                                <i class="bi bi-x-lg"></i>
                              </button>
                            }
                          </div>
                        }
                      </div>
                    }

                    <div class="d-flex gap-2 align-items-center">
                      <label class="btn btn-outline-secondary btn-sm" [class.disabled]="posting()">
                        <i class="bi bi-image me-1"></i>Add Media
                        <input
                          type="file"
                          class="d-none"
                          multiple
                          accept="image/jpeg,image/png,image/webp,video/mp4,video/quicktime"
                          (change)="onFilesSelected($event)"
                          [disabled]="posting()">
                      </label>
                      <span class="text-muted small">Max 10 images + 1 video</span>
                      <button
                        class="btn btn-primary btn-sm ms-auto"
                        [disabled]="posting() || !postContentControl.value.trim()"
                        (click)="submitPost()">
                        @if (posting()) {
                          <span class="spinner-border spinner-border-sm me-1" role="status"></span>Posting...
                        } @else {
                          <i class="bi bi-send me-1"></i>Post
                        }
                      </button>
                    </div>
                  </div>
                }
              </div>
            }

            <!-- Post feed -->
            @if (posts().length > 0) {
              <h4 class="fw-bold mb-3">
                <i class="bi bi-journal-richtext me-2"></i>Posts
                <span class="badge bg-primary ms-2">{{ postTotalCount() }}</span>
              </h4>

              @for (post of posts(); track post.id) {
                <div class="post-card mb-3">
                  <div class="post-header">
                    <div class="post-author-avatar">
                      <i class="bi bi-person-fill"></i>
                    </div>
                    <div>
                      <span class="post-author-name">{{ post.author.organizationName || post.author.artistName || post.author.name }}</span>
                      <span class="post-time">{{ post.created | date:'medium' }}</span>
                    </div>
                    @if (isOwnProfile()) {
                      <button
                        class="btn btn-sm btn-link text-danger ms-auto p-0"
                        aria-label="Delete post"
                        (click)="deletePost(post.id)">
                        <i class="bi bi-trash"></i>
                      </button>
                    }
                  </div>

                  <div class="post-content">{{ post.content }}</div>

                  @if (post.media && post.media.length > 0) {
                    <div class="post-media-grid" [class.post-media-single]="post.media.length === 1" [class.post-media-duo]="post.media.length === 2">
                      @for (m of post.media; track m.id) {
                        @if (m.mediaType === 'IMAGE') {
                          <div class="post-media-item">
                            <img [src]="postApi.resolvePostMediaUrl(m.url)" [alt]="'Post image'" loading="lazy">
                          </div>
                        } @else if (m.mediaType === 'VIDEO') {
                          <div class="post-media-item post-media-video">
                            <video controls preload="metadata">
                              <source [src]="postApi.resolvePostMediaUrl(m.url)" [type]="m.contentType">
                            </video>
                          </div>
                        }
                      }
                    </div>
                  }
                </div>
              }

              @if (hasMorePosts()) {
                <div class="text-center mt-3 mb-4">
                  <button
                    class="btn btn-outline-primary btn-sm"
                    [disabled]="postsLoading()"
                    (click)="loadMorePosts()">
                    @if (postsLoading()) {
                      <span class="spinner-border spinner-border-sm me-1" role="status"></span>Loading...
                    } @else {
                      <i class="bi bi-arrow-down-circle me-1"></i>Load More Posts
                    }
                  </button>
                </div>
              }
            } @else if (!loading()) {
              @if (isOwnProfile() && canPost()) {
                <!-- Don't show "No posts" if they can create -->
              } @else if (canAuthorPost()) {
                <div class="text-center py-3 mb-4 empty-section">
                  <i class="bi bi-journal-richtext display-5 text-muted"></i>
                  <p class="text-muted mt-2 mb-0">No posts yet.</p>
                </div>
              }
            }

            <!-- ═══ EVENTS SECTION ═══ -->

            <!-- Upcoming Events -->
            <h4 class="fw-bold mb-3">
              <i class="bi bi-calendar-event me-2"></i>Upcoming Events
              @if (upcomingEvents().length > 0) {
                <span class="badge bg-primary ms-2">{{ upcomingEvents().length }}</span>
              }
            </h4>

            @if (upcomingEvents().length === 0) {
              <div class="text-center py-4 mb-4 empty-section">
                <i class="bi bi-calendar-plus display-5 text-muted"></i>
                <p class="text-muted mt-2 mb-0">No upcoming events scheduled.</p>
              </div>
            } @else {
              <div class="row g-3 mb-4">
                @for (event of upcomingEvents(); track event.id) {
                  <div class="col-md-6">
                    <a [routerLink]="['/events', event.id]" class="text-decoration-none">
                      <div class="card border-0 h-100 event-card">
                        @if (event.imageUrl) {
                          <div class="event-img-wrap">
                            <img [src]="eventApi.resolveImageUrl(event.imageUrl)" [alt]="event.name">
                          </div>
                        } @else {
                          <div class="event-img-wrap event-placeholder">
                            <i class="bi bi-music-note-beamed display-4 text-white opacity-75"></i>
                          </div>
                        }
                        <div class="card-body">
                          <div class="d-flex flex-wrap align-items-center gap-2 mb-2">
                            <span class="date-badge">
                              <i class="bi bi-calendar3 me-1"></i>{{ event.eventDate | date:'mediumDate' }}
                            </span>
                            @if (event.genre) {
                              <span class="genre-badge">{{ event.genre }}</span>
                            }
                            @if (event.price) {
                              <span class="price-badge">{{ event.price | currency:'EUR' }}</span>
                            } @else {
                              <span class="price-badge price-badge--free">Free</span>
                            }
                          </div>
                          <h6 class="card-title fw-bold mb-1">{{ event.name }}</h6>
                          <p class="text-muted small mb-1">
                            <i class="bi bi-geo-alt me-1"></i>{{ event.location }}
                          </p>
                          @if (event.averageRating) {
                            <div class="mb-1" style="font-size: 0.8rem">
                              <app-star-rating [value]="event.averageRating" [count]="event.ratingCount ?? 0" [showValue]="true" [showCount]="true" />
                            </div>
                          }
                          @if (event.attendingCount) {
                            <p class="text-muted small mb-0">
                              <i class="bi bi-people-fill me-1"></i>{{ event.attendingCount }} attending
                            </p>
                          }
                        </div>
                      </div>
                    </a>
                  </div>
                }
              </div>
            }

            <!-- Past Events -->
            @if (pastEvents().length > 0) {
              <h4 class="fw-bold mb-3 mt-4">
                <i class="bi bi-clock-history me-2"></i>Past Events
                <span class="badge bg-secondary ms-2">{{ pastEvents().length }}</span>
              </h4>
              <div class="row g-3 mb-4">
                @for (event of pastEvents(); track event.id) {
                  <div class="col-md-6">
                    <a [routerLink]="['/events', event.id]" class="text-decoration-none">
                      <div class="card border-0 h-100 event-card event-card--past">
                        @if (event.imageUrl) {
                          <div class="event-img-wrap">
                            <img [src]="eventApi.resolveImageUrl(event.imageUrl)" [alt]="event.name">
                          </div>
                        } @else {
                          <div class="event-img-wrap event-placeholder">
                            <i class="bi bi-music-note-beamed display-4 text-white opacity-75"></i>
                          </div>
                        }
                        <div class="card-body">
                          <div class="d-flex flex-wrap align-items-center gap-2 mb-2">
                            <span class="date-badge">
                              <i class="bi bi-calendar3 me-1"></i>{{ event.eventDate | date:'mediumDate' }}
                            </span>
                            @if (event.genre) {
                              <span class="genre-badge">{{ event.genre }}</span>
                            }
                          </div>
                          <h6 class="card-title fw-bold mb-1">{{ event.name }}</h6>
                          <p class="text-muted small mb-1">
                            <i class="bi bi-geo-alt me-1"></i>{{ event.location }}
                          </p>
                          @if (event.averageRating) {
                            <div class="mb-1" style="font-size: 0.8rem">
                              <app-star-rating [value]="event.averageRating" [count]="event.ratingCount ?? 0" [showValue]="true" [showCount]="true" />
                            </div>
                          }
                        </div>
                      </div>
                    </a>
                  </div>
                }
              </div>
            }

            <!-- Performances (for artists) -->
            @if (performances().length > 0) {
              <h4 class="fw-bold mb-3 mt-4">
                <i class="bi bi-mic me-2"></i>Performances
                <span class="badge bg-info text-dark ms-2">{{ performances().length }}</span>
              </h4>
              <div class="row g-3">
                @for (event of performances(); track event.id) {
                  <div class="col-md-6">
                    <a [routerLink]="['/events', event.id]" class="text-decoration-none">
                      <div class="card border-0 h-100 event-card">
                        @if (event.imageUrl) {
                          <div class="event-img-wrap">
                            <img [src]="eventApi.resolveImageUrl(event.imageUrl)" [alt]="event.name">
                          </div>
                        } @else {
                          <div class="event-img-wrap event-placeholder">
                            <i class="bi bi-music-note-beamed display-4 text-white opacity-75"></i>
                          </div>
                        }
                        <div class="card-body">
                          <div class="d-flex flex-wrap align-items-center gap-2 mb-2">
                            <span class="date-badge">
                              <i class="bi bi-calendar3 me-1"></i>{{ event.eventDate | date:'mediumDate' }}
                            </span>
                            @if (event.genre) {
                              <span class="genre-badge">{{ event.genre }}</span>
                            }
                            @if (event.price) {
                              <span class="price-badge">{{ event.price | currency:'EUR' }}</span>
                            } @else {
                              <span class="price-badge price-badge--free">Free</span>
                            }
                          </div>
                          <h6 class="card-title fw-bold mb-1">{{ event.name }}</h6>
                          <p class="text-muted small mb-1">
                            <i class="bi bi-geo-alt me-1"></i>{{ event.location }}
                          </p>
                          @if (event.organizer) {
                            <p class="text-muted small mb-1">
                              <i class="bi bi-building me-1"></i>{{ event.organizer.organizationName || event.organizer.name }}
                            </p>
                          }
                          @if (event.averageRating) {
                            <div class="mb-1" style="font-size: 0.8rem">
                              <app-star-rating [value]="event.averageRating" [count]="event.ratingCount ?? 0" [showValue]="true" [showCount]="true" />
                            </div>
                          }
                        </div>
                      </div>
                    </a>
                  </div>
                }
              </div>
            }
          </div>
        </div>
      }
    </div>
  `,
  styles: `
    .profile-card {
      background: var(--ef-surface);
      border: 1px solid var(--ef-border-subtle);
      border-radius: var(--ef-radius-lg);
      padding: 2rem 1.5rem;
      text-align: center;
      box-shadow: var(--ef-shadow-sm);
      position: sticky;
      top: calc(var(--app-navbar-height, 64px) + 1.5rem);
    }

    .profile-avatar {
      width: 96px;
      height: 96px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--ef-primary-dark), var(--ef-primary-light));
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 2.5rem;
      margin: 0 auto 1rem;
    }

    .profile-name { font-size: 1.5rem; font-weight: 800; margin-bottom: 0.25rem; }
    .profile-subtitle { color: var(--ef-text-muted); font-size: 0.9375rem; margin-bottom: 0.75rem; }

    .profile-description {
      text-align: left;
      padding: 0.75rem;
      background: var(--ef-bg);
      border-radius: var(--ef-radius-sm);
      border: 1px solid var(--ef-border-light);
      color: var(--ef-text);
    }

    .profile-stats {
      display: flex;
      justify-content: center;
      gap: 1.5rem;
      padding: 1rem 0;
      margin: 0.75rem 0;
      border-top: 1px solid var(--ef-border-light);
      border-bottom: 1px solid var(--ef-border-light);
    }
    .profile-stat { text-align: center; }
    .profile-stat-value { display: block; font-size: 1.25rem; font-weight: 800; color: var(--ef-text); }
    .profile-stat-label { display: block; font-size: 0.7rem; color: var(--ef-text-muted); text-transform: uppercase; letter-spacing: 0.05em; font-weight: 600; }
    .follow-section { margin-top: 1rem; }
    .empty-icon { font-size: 3.5rem; color: var(--ef-text-light); }
    .empty-section { background: var(--ef-primary-50); border-radius: var(--ef-radius-lg); }

    /* ── Post create card ── */
    .post-create-card {
      background: var(--ef-surface);
      border: 1px solid var(--ef-border-subtle);
      border-radius: var(--ef-radius-lg);
      box-shadow: var(--ef-shadow-sm);
      overflow: hidden;
    }
    .post-create-header {
      display: flex;
      align-items: center;
      padding: 0.75rem 1rem;
      font-weight: 600;
      cursor: pointer;
      transition: background 0.15s;
      color: var(--ef-text);
    }
    .post-create-header:hover { background: var(--ef-primary-50, #f8f5ff); }
    .post-create-body { padding: 0 1rem 1rem; }

    .selected-files {
      border: 1px solid var(--ef-border-light);
      border-radius: 0.5rem;
      overflow: hidden;
    }
    .selected-file {
      display: flex;
      align-items: center;
      padding: 0.4rem 0.75rem;
      font-size: 0.8125rem;
      border-bottom: 1px solid var(--ef-border-light);
    }
    .selected-file:last-child { border-bottom: none; }
    .file-name {
      max-width: 180px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    /* ── Post card ── */
    .post-card {
      background: var(--ef-surface);
      border: 1px solid var(--ef-border-subtle);
      border-radius: var(--ef-radius-lg);
      box-shadow: var(--ef-shadow-sm);
      overflow: hidden;
    }
    .post-header {
      display: flex;
      align-items: center;
      gap: 0.625rem;
      padding: 0.875rem 1rem;
    }
    .post-author-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--ef-primary-dark), var(--ef-primary-light));
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 0.9rem;
      flex-shrink: 0;
    }
    .post-author-name { font-weight: 600; font-size: 0.9rem; display: block; line-height: 1.2; color: var(--ef-text); }
    .post-time { font-size: 0.75rem; color: var(--ef-text-muted); display: block; }
    .post-content {
      padding: 0 1rem 0.75rem;
      font-size: 0.9375rem;
      line-height: 1.6;
      white-space: pre-wrap;
      word-break: break-word;
      color: var(--ef-text);
    }

    /* ── Post media grid ── */
    .post-media-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 4px;
    }
    .post-media-single { grid-template-columns: 1fr; }
    .post-media-duo { grid-template-columns: repeat(2, 1fr); }

    .post-media-item {
      overflow: hidden;
      border-radius: 6px;
      background: var(--ef-bg);
    }
    .post-media-single .post-media-item {
      max-height: 420px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .post-media-single .post-media-item img {
      max-width: 100%;
      max-height: 420px;
      width: auto;
      object-fit: contain;
      display: block;
    }
    .post-media-duo .post-media-item,
    .post-media-grid:not(.post-media-single):not(.post-media-duo) .post-media-item {
      aspect-ratio: 1;
    }
    .post-media-duo .post-media-item img,
    .post-media-grid:not(.post-media-single):not(.post-media-duo) .post-media-item img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
    .post-media-video { grid-column: 1 / -1; }
    .post-media-video video {
      width: 100%;
      max-height: 450px;
      background: #000000;
      display: block;
    }

    /* ── Event cards ── */
    .event-card {
      border-radius: var(--ef-radius-lg);
      overflow: hidden;
      box-shadow: var(--ef-shadow-sm);
      transition: transform 0.25s ease, box-shadow 0.25s ease;
    }
    .event-card:hover { transform: translateY(-4px); box-shadow: var(--ef-shadow-xl); }
    .event-card--past { opacity: 0.75; }
    .event-card--past:hover { opacity: 1; }

    .event-img-wrap { height: 160px; overflow: hidden; }
    .event-img-wrap img { width: 100%; height: 100%; object-fit: cover; }
    .event-placeholder {
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, var(--ef-primary-dark), var(--ef-primary-light));
    }

    .date-badge { font-size: 0.75rem; font-weight: 600; color: var(--ef-primary); background: var(--ef-primary-50); padding: 0.2rem 0.5rem; border-radius: 0.25rem; }
    .genre-badge { font-size: 0.7rem; font-weight: 600; color: var(--ef-primary); background: var(--ef-primary-50); padding: 0.2rem 0.5rem; border-radius: 0.25rem; }
    .price-badge { font-size: 0.75rem; font-weight: 700; color: var(--ef-primary); background: var(--ef-primary-50); padding: 0.2rem 0.5rem; border-radius: 0.25rem; }
    .price-badge--free { color: #059669; background: rgba(5, 150, 105, 0.1); }
    .breadcrumb { font-size: 0.875rem; }

    @media (max-width: 575.98px) {
      .profile-card { padding: 1.25rem 1rem; position: static; }
      .profile-avatar { width: 64px; height: 64px; font-size: 1.75rem; }
      .profile-name { font-size: 1.2rem; }
      .profile-subtitle { font-size: 0.8rem; }
      .profile-stats { gap: 1rem; padding: 0.75rem 0; }
      .profile-stat-value { font-size: 1rem; }
      .event-img-wrap { height: 100px; }
      .event-card:hover { transform: none; }
      .event-card .card-body { padding: 0.5rem; }
      .event-card .card-title { font-size: 0.82rem; }
      .date-badge, .genre-badge, .price-badge { font-size: 0.62rem; padding: 0.125rem 0.3rem; }
      .post-header { padding: 0.625rem 0.75rem; }
      .post-content { padding: 0 0.75rem 0.625rem; font-size: 0.875rem; }
      .post-author-avatar { width: 30px; height: 30px; font-size: 0.75rem; }
      .file-name { max-width: 120px; }
    }
  `
})
export class PublicProfileComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  readonly eventApi = inject(EventApi);
  readonly postApi = inject(PostApi);
  readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly userApi = inject(UserApi);
  private readonly followApi = inject(FollowApi);

  readonly loading = signal(true);
  readonly profile = signal<User | null>(null);
  readonly events = signal<Event[]>([]);
  readonly allEvents = signal<Event[]>([]);
  readonly followStatus = signal<FollowStatus | null>(null);
  readonly followBusy = signal(false);

  // Posts
  readonly posts = signal<Post[]>([]);
  readonly postPage = signal(0);
  readonly postTotalCount = signal(0);
  readonly postsLoading = signal(false);
  readonly hasMorePosts = computed(() => this.posts().length < this.postTotalCount());
  readonly createFormOpen = signal(false);
  readonly postContentControl = new FormControl('', { nonNullable: true });
  readonly pendingFiles = signal<MediaUploadItem[]>([]);
  readonly posting = signal(false);

  readonly displayName = computed(() => {
    const p = this.profile();
    if (!p) return '';
    return p.name;
  });

  readonly isOwnProfile = computed(() => {
    const me = this.auth.user();
    const p = this.profile();
    return !!(me && p && me.id === p.id);
  });

  readonly canPost = computed(() => {
    return this.auth.isOrganizer() || this.auth.isArtist();
  });

  readonly canAuthorPost = computed(() => {
    const p = this.profile();
    if (!p) return false;
    return p.role === 'ORGANIZER' || p.role === 'ARTIST' || p.role === 'ADMIN' || !!p.artistName;
  });

  readonly avgRating = computed(() => {
    const rated = this.events().filter(e => e.averageRating);
    if (rated.length === 0) return null;
    return rated.reduce((sum, e) => sum + e.averageRating!, 0) / rated.length;
  });

  readonly upcomingEvents = computed(() => {
    const now = Date.now();
    return this.events()
      .filter(e => new Date(e.eventDate).getTime() > now)
      .sort((a, b) => new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime());
  });

  readonly pastEvents = computed(() => {
    const now = Date.now();
    return this.events()
      .filter(e => new Date(e.eventDate).getTime() <= now)
      .sort((a, b) => new Date(b.eventDate).getTime() - new Date(a.eventDate).getTime());
  });

  readonly performances = computed(() => {
    const p = this.profile();
    if (!p?.artistName) return [];
    return this.allEvents()
      .filter(e =>
        !e.canceled &&
        e.artists?.some(a => a.artistUserId === p.id) &&
        e.organizer?.id !== p.id
      )
      .sort((a, b) => new Date(b.eventDate).getTime() - new Date(a.eventDate).getTime());
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.loading.set(false);
      return;
    }

    this.userApi.getUserById(id).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (user) => {
        this.profile.set(user);
        this.loading.set(false);
        this.loadEvents(user.id);
        this.loadPosts(user.id);
        if (this.auth.user()) {
          this.loadFollowStatus(user.id);
        }
      },
      error: () => this.loading.set(false)
    });
  }

  // ── Posts ──

  onFilesSelected(event: globalThis.Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files) return;

    const current = this.pendingFiles();
    const imageCount = current.filter(f => f.file.type.startsWith('image')).length;
    const videoCount = current.filter(f => f.file.type.startsWith('video')).length;

    const newItems: MediaUploadItem[] = [];
    for (const file of Array.from(input.files)) {
      const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/webp';
      const isVideo = file.type === 'video/mp4' || file.type === 'video/quicktime';
      if (!isImage && !isVideo) {
        this.toast.error(`${file.name} is not a supported media type.`);
        continue;
      }
      if (file.size <= 0) {
        this.toast.error(`${file.name} is empty.`);
        continue;
      }
      if (isImage && imageCount + newItems.filter(f => f.file.type.startsWith('image')).length >= 10) {
        this.toast.error('Maximum 10 images per post.');
        break;
      }
      if (isVideo && videoCount + newItems.filter(f => f.file.type.startsWith('video')).length >= 1) {
        this.toast.error('Maximum 1 video per post.');
        continue;
      }
      if (isImage && file.size > 5 * 1024 * 1024) {
        this.toast.error(`${file.name} exceeds 5 MB limit.`);
        continue;
      }
      if (isVideo && file.size > 75 * 1024 * 1024) {
        this.toast.error(`${file.name} exceeds 75 MB limit.`);
        continue;
      }
      newItems.push({ file, progress: 0, done: false, error: false });
    }

    this.pendingFiles.set([...current, ...newItems]);
    input.value = '';
  }

  removeFile(index: number): void {
    this.pendingFiles.update(files => files.filter((_, i) => i !== index));
  }

  submitPost(): void {
    const content = this.postContentControl.value.trim();
    if (!content) return;

    this.posting.set(true);

    this.postApi.createPost(content).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: ({ id }) => {
        const files = this.pendingFiles();
        if (files.length === 0) {
          this.onPostCreated(id);
          return;
        }
        this.uploadFilesSequentially(id, 0);
      },
      error: (error: unknown) => {
        this.toast.error(toApiError(error).message);
        this.posting.set(false);
      }
    });
  }

  deletePost(postId: number): void {
    if (!confirm('Delete this post?')) return;
    this.postApi.deletePost(postId).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => {
        this.posts.update(list => list.filter(p => p.id !== postId));
        this.toast.success('Post deleted.');
      },
      error: (error: unknown) => this.toast.error(toApiError(error).message)
    });
  }

  // ── Follow ──

  toggleFollow(): void {
    const p = this.profile();
    if (!p) return;
    if (!this.auth.user()) {
      this.auth.login();
      return;
    }

    this.followBusy.set(true);
    const isFollowing = this.followStatus()?.following;
    const action$ = isFollowing
      ? this.followApi.unfollowUser(p.id)
      : this.followApi.followUser(p.id);

    action$.pipe(
      finalize(() => this.followBusy.set(false)),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => {
        const current = this.followStatus();
        this.followStatus.set({
          following: !isFollowing,
          followerCount: (current?.followerCount ?? 0) + (isFollowing ? -1 : 1)
        });
        this.toast.success(isFollowing ? 'Unfollowed.' : 'Following!');
      },
      error: (error: unknown) => this.toast.error(toApiError(error).message)
    });
  }

  // ── Private ──

  private loadEvents(userId: number): void {
    this.eventApi.getAllEvents().pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (allEvents) => {
        this.allEvents.set(allEvents);
        this.events.set(allEvents.filter(e => e.organizer?.id === userId && !e.canceled));
      },
      error: () => {}
    });
  }

  loadMorePosts(): void {
    const p = this.profile();
    if (!p || this.postsLoading()) return;
    this.postsLoading.set(true);
    const nextPage = this.postPage();
    this.postApi.getPostsByUserPaginated(p.id, nextPage, 5).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (result) => {
        this.posts.update(existing => [...existing, ...result.content]);
        this.postTotalCount.set(result.totalElements);
        this.postPage.update(p => p + 1);
        this.postsLoading.set(false);
      },
      error: () => this.postsLoading.set(false)
    });
  }

  private loadPosts(userId: number): void {
    this.postPage.set(0);
    this.posts.set([]);
    this.postsLoading.set(true);
    this.postApi.getPostsByUserPaginated(userId, 0, 5).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (result) => {
        this.posts.set(result.content);
        this.postTotalCount.set(result.totalElements);
        this.postPage.set(1);
        this.postsLoading.set(false);
      },
      error: () => this.postsLoading.set(false)
    });
  }

  private loadFollowStatus(userId: number): void {
    this.followApi.getFollowStatus(userId).pipe(
      catchError(() => of(null)),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (status) => this.followStatus.set(status)
    });
  }

  private uploadFilesSequentially(postId: number, index: number): void {
    const files = this.pendingFiles();
    if (index >= files.length) {
      this.onPostCreated(postId);
      return;
    }

    const item = files[index];
    this.postApi.uploadPostMedia(postId, item.file, (percent) => {
      this.pendingFiles.update(list =>
        list.map((f, i) => i === index ? { ...f, progress: percent } : f)
      );
    }).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => {
        this.pendingFiles.update(list =>
          list.map((f, i) => i === index ? { ...f, done: true, progress: 100 } : f)
        );
        this.uploadFilesSequentially(postId, index + 1);
      },
      error: () => {
        this.pendingFiles.update(list =>
          list.map((f, i) => i === index ? { ...f, error: true } : f)
        );
        this.rollbackFailedPost(postId, item.file.name);
      }
    });
  }

  private rollbackFailedPost(postId: number, failedFileName: string): void {
    this.postApi.deletePost(postId).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => {
        this.posting.set(false);
        this.toast.error(`Failed to upload ${failedFileName}. The post was not published.`);
      },
      error: () => {
        this.posting.set(false);
        this.toast.error(`Failed to upload ${failedFileName}. The post was created without all media; delete it manually if needed.`);
        const userId = this.profile()?.id;
        if (userId) {
          this.loadPosts(userId);
        }
      }
    });
  }

  private onPostCreated(postId: number): void {
    this.posting.set(false);
    this.postContentControl.reset('');
    this.pendingFiles.set([]);
    this.createFormOpen.set(false);
    this.toast.success('Post published!');

    // Reload posts to get the full post with media URLs
    const userId = this.profile()?.id;
    if (userId) {
      this.loadPosts(userId);
    }
  }
}
