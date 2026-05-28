import { Component, ChangeDetectionStrategy, DestroyRef, inject, signal, computed, OnInit, AfterViewInit, OnDestroy, ElementRef, ViewChild, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { EventApi } from '../../event-api';
import { AuthService } from '../../../../core/auth/auth';
import { ToastService } from '../../../../core/services/toast';
import { SkeletonCardComponent } from '../../../../core/components/skeleton-card';
import { BackToTopComponent } from '../../../../core/components/back-to-top';
import { StarRatingComponent } from '../../../../core/components/star-rating';
import { Event } from '../../event.model';
import { FollowApi } from '../../../profile/follow-api';

@Component({
  selector: 'app-event-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, ReactiveFormsModule, DatePipe, CurrencyPipe, SkeletonCardComponent, BackToTopComponent, StarRatingComponent],
  templateUrl: './event-list.html',
  styleUrl: './event-list.scss'
})
export class EventListComponent implements OnInit, AfterViewInit, OnDestroy {
  readonly eventApi = inject(EventApi);
  readonly auth = inject(AuthService);
  private readonly followApi = inject(FollowApi);
  private readonly toast = inject(ToastService);

  readonly allEvents = signal<Event[]>([]);
  readonly loading = signal(true);
  readonly searchQuery = signal('');
  readonly locationFilter = signal('');
  readonly dateFilter = signal('');
  readonly artistFilter = signal('');
  readonly genreFilter = signal('');
  readonly sortBy = signal<'date' | 'rating' | 'popularity'>('date');
  readonly showPastEvents = signal(false);
  readonly followingOnly = signal(false);
  readonly followedOrganizerIds = signal<Set<number>>(new Set());
  readonly displayedCount = signal(12);
  private readonly batchSize = 12;
  readonly searchControl = new FormControl('', { nonNullable: true });
  readonly genreControl = new FormControl('', { nonNullable: true });
  readonly locationControl = new FormControl('', { nonNullable: true });
  readonly dateControl = new FormControl('', { nonNullable: true });
  readonly artistControl = new FormControl('', { nonNullable: true });
  readonly sortControl = new FormControl<'date' | 'rating' | 'popularity'>('date', { nonNullable: true });

  readonly isAdmin = computed(() => this.auth.user()?.role === 'ADMIN');

  readonly locations = computed(() => {
    const locs = new Set(this.allEvents().map(e => e.location));
    return Array.from(locs).sort();
  });

  readonly artists = computed(() => {
    const names = new Set<string>();
    for (const e of this.allEvents()) {
      for (const a of e.artists ?? []) {
        names.add(a.artistName);
      }
    }
    return Array.from(names).sort();
  });

  readonly genres = computed(() => {
    const gs = new Set<string>();
    for (const e of this.allEvents()) {
      if (e.genre) gs.add(e.genre);
    }
    return Array.from(gs).sort();
  });

  readonly featuredEvents = computed(() => {
    const now = new Date();
    now.setHours(0, 0, 0, 0);
    return this.allEvents().filter(e =>
      e.featured && !e.canceled && new Date(e.eventDate).getTime() >= now.getTime()
    );
  });

  readonly filteredEvents = computed(() => {
    let events = this.allEvents();
    const query = this.searchQuery().toLowerCase();
    const location = this.locationFilter();
    const dateStr = this.dateFilter();
    const artist = this.artistFilter();
    const genre = this.genreFilter();
    const now = new Date();
    now.setHours(0, 0, 0, 0);

    if (this.showPastEvents()) {
      events = events.filter(e => new Date(e.eventDate).getTime() < now.getTime());
    } else {
      events = events.filter(e => new Date(e.eventDate).getTime() >= now.getTime());
    }

    if (this.followingOnly()) {
      const organizerIds = this.followedOrganizerIds();
      events = events.filter(e => e.organizer?.id && organizerIds.has(e.organizer.id));
    }

    if (query) {
      events = events.filter(e =>
        e.name.toLowerCase().includes(query) ||
        (e.artists?.some(a => a.artistName.toLowerCase().includes(query))) ||
        (e.description?.toLowerCase().includes(query))
      );
    }

    if (artist) {
      events = events.filter(e => e.artists?.some(a => a.artistName === artist));
    }

    if (location) {
      events = events.filter(e => e.location === location);
    }

    if (genre) {
      events = events.filter(e => e.genre === genre);
    }

    if (dateStr) {
      const filterDate = new Date(dateStr);
      events = events.filter(e => {
        const eventDate = new Date(e.eventDate);
        return eventDate.toDateString() === filterDate.toDateString();
      });
    }

    const sort = this.sortBy();
    if (sort === 'rating') {
      return [...events].sort((a, b) => (b.averageRating ?? 0) - (a.averageRating ?? 0));
    }
    if (sort === 'popularity') {
      return [...events].sort((a, b) => (b.attendingCount ?? 0) - (a.attendingCount ?? 0));
    }
    if (this.showPastEvents()) {
      return [...events].sort((a, b) =>
        new Date(b.eventDate).getTime() - new Date(a.eventDate).getTime()
      );
    }
    return [...events].sort((a, b) =>
      new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime()
    );
  });

  readonly displayedEvents = computed(() =>
    this.filteredEvents().slice(0, this.displayedCount())
  );

  readonly hasMore = computed(() =>
    this.displayedCount() < this.filteredEvents().length
  );

  @ViewChild('scrollSentinel', { static: false }) private scrollSentinel?: ElementRef<HTMLElement>;
  private observer?: IntersectionObserver;
  private readonly platformId = inject(PLATFORM_ID);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.searchControl.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(value => this.onSearchInput(value));
    this.genreControl.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(value => this.onGenreChange(value));
    this.locationControl.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(value => this.onLocationChange(value));
    this.dateControl.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(value => this.onDateChange(value));
    this.artistControl.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(value => this.onArtistChange(value));
    this.sortControl.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(value => this.onSortChange(value));

    this.eventApi.getAllEvents().pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (events) => {
        this.allEvents.set(events);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });

    if (this.auth.user()) {
      this.followApi.getMyFollowing().pipe(
        takeUntilDestroyed(this.destroyRef)
      ).subscribe({
        next: (users) => {
          this.followedOrganizerIds.set(new Set(users.map(u => u.id)));
        },
        error: () => {}
      });
    }
  }

  onSearchInput(value: string): void {
    this.searchQuery.set(value);
    this.displayedCount.set(this.batchSize);
  }

  onLocationChange(value: string): void {
    this.locationFilter.set(value);
    this.displayedCount.set(this.batchSize);
  }

  onDateChange(value: string): void {
    this.dateFilter.set(value);
    this.displayedCount.set(this.batchSize);
  }

  onArtistChange(value: string): void {
    this.artistFilter.set(value);
    this.displayedCount.set(this.batchSize);
  }

  onGenreChange(value: string): void {
    this.genreFilter.set(value);
    this.displayedCount.set(this.batchSize);
  }

  onSortChange(value: string): void {
    this.sortBy.set(value as 'date' | 'rating' | 'popularity');
    this.displayedCount.set(this.batchSize);
  }

  artistNames(event: Event): string {
    return event.artists?.map(a => a.artistName).join(', ') ?? '';
  }

  isSoldOut(event: Event): boolean {
    return !!event.capacity && (event.attendingCount ?? 0) >= event.capacity;
  }

  spotsLeft(event: Event): number | null {
    if (!event.capacity) return null;
    return Math.max(0, event.capacity - (event.attendingCount ?? 0));
  }

  toggleShowPast(): void {
    this.showPastEvents.set(!this.showPastEvents());
    this.displayedCount.set(this.batchSize);
  }

  toggleFollowingOnly(): void {
    this.followingOnly.set(!this.followingOnly());
    this.displayedCount.set(this.batchSize);
  }

  clearFilters(): void {
    this.searchQuery.set('');
    this.locationFilter.set('');
    this.dateFilter.set('');
    this.artistFilter.set('');
    this.genreFilter.set('');
    this.sortBy.set('date');
    this.showPastEvents.set(false);
    this.followingOnly.set(false);
    this.displayedCount.set(this.batchSize);
    this.searchControl.setValue('', { emitEvent: false });
    this.genreControl.setValue('', { emitEvent: false });
    this.locationControl.setValue('', { emitEvent: false });
    this.dateControl.setValue('', { emitEvent: false });
    this.artistControl.setValue('', { emitEvent: false });
    this.sortControl.setValue('date', { emitEvent: false });
  }

  loadMore(): void {
    this.displayedCount.update(c => c + this.batchSize);
  }

  ngAfterViewInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.setupIntersectionObserver();
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }

  private setupIntersectionObserver(): void {
    if (typeof IntersectionObserver === 'undefined') return;

    this.observer = new IntersectionObserver(
      (entries) => {
        if (entries[0]?.isIntersecting && this.hasMore()) {
          this.loadMore();
        }
      },
      { rootMargin: '200px' }
    );

    // Observe after a tick to ensure sentinel is rendered
    setTimeout(() => {
      if (this.scrollSentinel?.nativeElement) {
        this.observer!.observe(this.scrollSentinel.nativeElement);
      }
    });
  }

  onAdminCancel(event: globalThis.Event, ev: Event): void {
    event.stopPropagation();
    event.preventDefault();
    const action$ = ev.canceled
      ? this.eventApi.restoreEvent(ev.id)
      : this.eventApi.cancelEvent(ev.id);
    action$.pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => {
        this.allEvents.set(this.allEvents().map(e =>
          e.id === ev.id ? { ...e, canceled: !e.canceled } : e
        ));
      },
      error: () => this.toast.error('Failed to update event status.')
    });
  }

  onAdminToggleFeatured(event: globalThis.Event, ev: Event): void {
    event.stopPropagation();
    event.preventDefault();
    this.eventApi.toggleFeatured(ev.id).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => {
        this.allEvents.set(this.allEvents().map(e =>
          e.id === ev.id ? { ...e, featured: !e.featured } : e
        ));
        this.toast.success(ev.featured ? 'Event unfeatured.' : 'Event featured!');
      },
      error: () => this.toast.error('Failed to toggle featured status.')
    });
  }

  onAdminDelete(event: globalThis.Event, ev: Event): void {
    event.stopPropagation();
    event.preventDefault();
    if (!confirm(`Permanently delete "${ev.name}"?`)) return;
    this.eventApi.deleteEvent(ev.id).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => this.allEvents.set(this.allEvents().filter(e => e.id !== ev.id)),
      error: () => this.toast.error('Failed to delete event.')
    });
  }
}
