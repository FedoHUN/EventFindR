import { Component, ChangeDetectionStrategy, computed, effect, inject, signal, OnInit, OnDestroy, PLATFORM_ID, DestroyRef } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { finalize, switchMap } from 'rxjs';
import { EventApi } from '../../event-api';
import { EventMediaApi } from '../../event-media-api';
import { EventCommentApi } from '../../event-comment-api';
import { AuthService } from '../../../../core/auth/auth';
import { ToastService } from '../../../../core/services/toast';
import { StarRatingComponent } from '../../../../core/components/star-rating';
import { Event, EventMedia, EventComment, AttendanceStatus, FollowStatus } from '../../event.model';
import { FollowApi } from '../../../profile/follow-api';
import { toApiError } from '../../../../core/http/api-error';

@Component({
  selector: 'app-event-detail',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe, CurrencyPipe, ReactiveFormsModule, StarRatingComponent],
  templateUrl: './event-detail.html',
  styleUrl: './event-detail.scss'
})
export class EventDetailComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  readonly eventApi = inject(EventApi);
  private readonly eventMediaApi = inject(EventMediaApi);
  private readonly eventCommentApi = inject(EventCommentApi);
  private readonly followApi = inject(FollowApi);
  readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly destroyRef = inject(DestroyRef);

  readonly event = signal<Event | null>(null);
  readonly loading = signal(true);
  readonly mediaList = signal<EventMedia[]>([]);
  readonly currentSlide = signal(0);
  readonly currentAttendance = signal<AttendanceStatus | null>(null);
  readonly hoveredAttendance = signal<AttendanceStatus | null>(null);
  readonly attendanceBusy = signal(false);

  // Comments
  readonly comments = signal<EventComment[]>([]);
  readonly commentControl = new FormControl('', { nonNullable: true });
  readonly newCommentRating = signal(0);
  readonly commentSubmitting = signal(false);

  // Similar events
  readonly similarEvents = signal<Event[]>([]);

  // Follow
  readonly followStatus = signal<FollowStatus | null>(null);
  readonly followBusy = signal(false);

  // Countdown
  readonly countdownText = signal('');
  private countdownInterval: ReturnType<typeof setInterval> | null = null;

  private touchStartX = 0;

  readonly artistNames = computed(() =>
    this.event()?.artists?.map(a => a.artistName).join(', ') ?? ''
  );

  readonly attendingActive = computed(() => this.currentAttendance() === 'ATTENDING');
  readonly watchingActive = computed(() => this.currentAttendance() === 'WATCHING');
  readonly attendingHoverDanger = computed(() => this.attendingActive() && this.hoveredAttendance() === 'ATTENDING');
  readonly watchingHoverDanger = computed(() => this.watchingActive() && this.hoveredAttendance() === 'WATCHING');

  readonly attendingButtonText = computed(() => {
    if (this.attendingHoverDanger()) return 'Unattend';
    return this.attendingActive() ? "I'm attending" : 'Attend';
  });

  readonly watchingButtonText = computed(() => {
    if (this.watchingHoverDanger()) return 'Unwatch';
    return this.watchingActive() ? "I'm watching" : 'Watch this event';
  });

  readonly isSoldOut = computed(() => {
    const ev = this.event();
    if (!ev?.capacity) return false;
    return (ev.attendingCount ?? 0) >= ev.capacity;
  });

  readonly spotsLeft = computed(() => {
    const ev = this.event();
    if (!ev?.capacity) return null;
    return Math.max(0, ev.capacity - (ev.attendingCount ?? 0));
  });

  readonly isOrganizer = computed(() => {
    const user = this.auth.user();
    const ev = this.event();
    return user && ev?.organizer && user.email === ev.organizer.email;
  });

  constructor() {
    effect(() => {
      const user = this.auth.user();
      const ev = this.event();
      if (user && ev) {
        this.loadMyAttendance(ev.id);
      } else {
        this.currentAttendance.set(null);
      }
    });
  }

  ngOnInit(): void {
    this.route.paramMap.pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(params => {
      const id = Number(params.get('id'));
      if (!id) return;
      this.loadEvent(id);
    });
  }

  private loadEvent(id: number): void {
    this.loading.set(true);
    this.event.set(null);
    this.comments.set([]);
    this.similarEvents.set([]);
    this.mediaList.set([]);
    this.currentSlide.set(0);
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
      this.countdownInterval = null;
    }

    this.eventApi.getEventById(id).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (event) => {
        this.event.set(event);
        this.loading.set(false);
        this.loadMedia(id);
        this.loadComments(id);
        this.loadSimilarEvents(id);
        this.startCountdown(event);
        if (event.organizer?.id) {
          this.loadFollowStatus(event.organizer.id);
        }
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  ngOnDestroy(): void {
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
    }
  }

  private startCountdown(event: Event): void {
    if (!isPlatformBrowser(this.platformId)) return;
    const update = () => {
      const now = Date.now();
      const eventTime = new Date(event.eventDate).getTime();
      const diff = eventTime - now;
      if (diff <= 0) {
        this.countdownText.set('Event has started');
        if (this.countdownInterval) clearInterval(this.countdownInterval);
        return;
      }
      const days = Math.floor(diff / 86400000);
      const hours = Math.floor((diff % 86400000) / 3600000);
      const minutes = Math.floor((diff % 3600000) / 60000);
      if (days > 0) {
        this.countdownText.set(`Starts in ${days}d ${hours}h ${minutes}m`);
      } else if (hours > 0) {
        this.countdownText.set(`Starts in ${hours}h ${minutes}m`);
      } else {
        this.countdownText.set(`Starts in ${minutes}m`);
      }
    };
    update();
    this.countdownInterval = setInterval(update, 60000);
  }

  mediaUrl(media: EventMedia): string {
    return this.eventMediaApi.resolveUrl(media.url);
  }

  prevSlide(): void {
    const total = this.mediaList().length;
    if (total > 1) this.currentSlide.update(i => (i - 1 + total) % total);
  }

  nextSlide(): void {
    const total = this.mediaList().length;
    if (total > 1) this.currentSlide.update(i => (i + 1) % total);
  }

  goToSlide(index: number): void {
    this.currentSlide.set(index);
  }

  onTouchStart(event: TouchEvent): void {
    this.touchStartX = event.touches[0].clientX;
  }

  onTouchEnd(event: TouchEvent): void {
    const diff = this.touchStartX - event.changedTouches[0].clientX;
    if (Math.abs(diff) > 50) {
      diff > 0 ? this.nextSlide() : this.prevSlide();
    }
  }

  private loadMedia(eventId: number): void {
    this.eventMediaApi.getEventMedia(eventId).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (media) => this.mediaList.set(media),
      error: () => {}
    });
  }

  // Comments
  private loadComments(eventId: number): void {
    this.eventCommentApi.getComments(eventId).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (comments) => this.comments.set(comments),
      error: () => {}
    });
  }

  submitComment(): void {
    const ev = this.event();
    const content = this.commentControl.value.trim();
    if (!ev || !content || this.commentSubmitting()) return;

    if (!this.auth.user()) {
      this.auth.login();
      return;
    }

    this.commentSubmitting.set(true);
    const rating = this.newCommentRating() || undefined;
    this.eventCommentApi.addComment(ev.id, content, rating).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (comment) => {
        this.comments.update(c => [comment, ...c]);
        this.commentControl.reset('');
        this.newCommentRating.set(0);
        this.commentSubmitting.set(false);
        this.toast.success('Comment posted!');
      },
      error: () => {
        this.commentSubmitting.set(false);
        this.toast.error('Failed to post comment.');
      }
    });
  }

  deleteComment(commentId: number): void {
    this.eventCommentApi.deleteComment(commentId).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => {
        this.comments.update(c => c.filter(comment => comment.id !== commentId));
        this.toast.success('Comment deleted.');
      },
      error: () => this.toast.error('Failed to delete comment.')
    });
  }

  canDeleteComment(comment: EventComment): boolean {
    const user = this.auth.user();
    if (!user) return false;
    return user.email === comment.user?.email || user.role === 'ADMIN';
  }

  // Similar events
  private loadSimilarEvents(eventId: number): void {
    this.eventApi.getSimilarEvents(eventId).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (events) => this.similarEvents.set(events),
      error: () => {}
    });
  }

  // Follow
  private loadFollowStatus(userId: number): void {
    if (!this.auth.user()) return;
    this.followApi.getFollowStatus(userId).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (status) => this.followStatus.set(status),
      error: () => {}
    });
  }

  toggleFollow(): void {
    const organizer = this.event()?.organizer;
    if (!organizer?.id) return;
    if (!this.auth.user()) {
      this.auth.login();
      return;
    }
    this.followBusy.set(true);
    const isFollowing = this.followStatus()?.following;
    const action$ = isFollowing
      ? this.followApi.unfollowUser(organizer.id)
      : this.followApi.followUser(organizer.id);

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
      error: () => this.toast.error('Failed to update follow status.')
    });
  }

  // Share
  async shareEvent(): Promise<void> {
    const ev = this.event();
    if (!ev) return;
    const url = window.location.href;
    if (navigator.share) {
      try {
        await navigator.share({ title: ev.name, text: ev.description ?? '', url });
      } catch { /* user cancelled */ }
    } else {
      await navigator.clipboard.writeText(url);
      this.toast.success('Link copied to clipboard!');
    }
  }

  // Attendance
  toggleAttendance(status: AttendanceStatus): void {
    const ev = this.event();
    if (!ev || this.attendanceBusy()) return;

    if (!this.auth.user()) {
      this.auth.login();
      return;
    }

    this.attendanceBusy.set(true);

    const currentStatus = this.currentAttendance();
    const request$ = currentStatus === status
      ? this.eventApi.removeAttendance(ev.id)
      : currentStatus
        ? this.eventApi.removeAttendance(ev.id).pipe(
            switchMap(() => this.eventApi.attendEvent(ev.id, { status }))
          )
        : this.eventApi.attendEvent(ev.id, { status });

    request$.pipe(
      finalize(() => {
        this.attendanceBusy.set(false);
        this.hoveredAttendance.set(null);
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => {
        if (currentStatus === status) {
          this.currentAttendance.set(null);
          this.toast.info(status === 'ATTENDING' ? 'Attendance removed.' : 'Watching removed.');
          return;
        }
        this.currentAttendance.set(status);
        this.toast.success(
          status === 'ATTENDING' ? 'You are attending this event!' : 'You are watching this event!'
        );
      },
      error: (error: unknown) => {
        const apiError = toApiError(error);
        if (apiError.status === 401) {
          this.auth.login();
        } else if (apiError.message.toLowerCase().includes('sold out') || apiError.message.toLowerCase().includes('capacity')) {
          this.toast.error('This event is sold out!');
        } else {
          this.toast.error(apiError.message);
        }
      }
    });
  }

  private loadMyAttendance(eventId: number): void {
    this.eventApi.getMyAttendance(eventId).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (status) => this.currentAttendance.set(status),
      error: () => this.currentAttendance.set(null)
    });
  }
}
