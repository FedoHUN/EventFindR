import { Component, ChangeDetectionStrategy, inject, signal, OnInit, AfterViewInit, OnDestroy, PLATFORM_ID, ElementRef } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { EventApi } from '../../../events/event-api';
import { UserApi } from '../../../profile/user-api';
import { StarRatingComponent } from '../../../../core/components/star-rating';
import { Event } from '../../../events/event.model';
import { User } from '../../../../core/auth/auth.model';

@Component({
  selector: 'app-home',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe, CurrencyPipe, StarRatingComponent],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class HomeComponent implements OnInit, AfterViewInit, OnDestroy {
  readonly eventApi = inject(EventApi);
  private readonly userApi = inject(UserApi);
  private readonly el = inject(ElementRef);
  private readonly platformId = inject(PLATFORM_ID);
  private scrollObserver?: IntersectionObserver;

  readonly featuredEvents = signal<Event[]>([]);
  readonly trendingEvents = signal<Event[]>([]);
  readonly organizers = signal<Partial<User>[]>([]);
  readonly loading = signal(true);
  readonly trendingLoading = signal(true);

  ngOnInit(): void {
    this.eventApi.getAllEvents().subscribe({
      next: (events) => {
        const now = new Date();
        now.setHours(0, 0, 0, 0);
        const upcoming = events
          .filter(e => new Date(e.eventDate).getTime() >= now.getTime() && !e.canceled)
          .sort((a, b) => new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime());
        this.featuredEvents.set(upcoming.slice(0, 6));
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });

    this.eventApi.getTrendingEvents().subscribe({
      next: (events) => {
        const now = new Date();
        now.setHours(0, 0, 0, 0);
        this.trendingEvents.set(
          events.filter(e => new Date(e.eventDate).getTime() >= now.getTime())
        );
        this.trendingLoading.set(false);
      },
      error: () => this.trendingLoading.set(false)
    });

    this.userApi.getOrganizers().subscribe({
      next: (organizers) => this.organizers.set(organizers),
      error: () => {}
    });
  }

  ngAfterViewInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    this.scrollObserver = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            entry.target.classList.add('revealed');
            this.scrollObserver!.unobserve(entry.target);
          }
        }
      },
      { threshold: 0.1, rootMargin: '0px 0px -40px 0px' }
    );

    const revealEls = this.el.nativeElement.querySelectorAll('.reveal');
    revealEls.forEach((el: Element) => this.scrollObserver!.observe(el));
  }

  ngOnDestroy(): void {
    this.scrollObserver?.disconnect();
  }

  isSoldOut(event: Event): boolean {
    return !!event.capacity && (event.attendingCount ?? 0) >= event.capacity;
  }

  spotsLeft(event: Event): number | null {
    if (!event.capacity) return null;
    return Math.max(0, event.capacity - (event.attendingCount ?? 0));
  }
}
