import { ChangeDetectionStrategy, Component, inject, OnInit, signal, computed } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { EventApi } from '../../event-api';
import { AuthService } from '../../../../core/auth/auth';
import { ToastService } from '../../../../core/services/toast';
import { Event } from '../../event.model';

@Component({
  selector: 'app-my-events',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe, CurrencyPipe],
  templateUrl: './my-events.html',
  styleUrl: './my-events.scss'
})
export class MyEventsComponent implements OnInit {
  readonly eventApi = inject(EventApi);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly myEvents = signal<Event[]>([]);
  readonly drafts = signal<Event[]>([]);
  readonly loading = signal(true);
  readonly activeTab = signal<'upcoming' | 'past' | 'drafts'>('upcoming');

  readonly upcomingEvents = computed(() => {
    const now = new Date();
    now.setHours(0, 0, 0, 0);
    return this.myEvents()
      .filter(e => e.status !== 'DRAFT' && new Date(e.eventDate).getTime() >= now.getTime())
      .sort((a, b) => new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime());
  });

  readonly pastEvents = computed(() => {
    const now = new Date();
    now.setHours(0, 0, 0, 0);
    return this.myEvents()
      .filter(e => e.status !== 'DRAFT' && new Date(e.eventDate).getTime() < now.getTime())
      .sort((a, b) => new Date(b.eventDate).getTime() - new Date(a.eventDate).getTime());
  });

  ngOnInit(): void {
    this.loadEvents();
    this.loadDrafts();
  }

  artistNames(event: Event): string {
    return event.artists?.map(a => a.artistName).join(', ') ?? '';
  }

  isPast(event: Event): boolean {
    return new Date(event.eventDate).getTime() < new Date().setHours(0, 0, 0, 0);
  }

  onEdit(event: Event): void {
    this.router.navigate(['/events', event.id, 'edit']);
  }

  onDelete(event: Event): void {
    if (!confirm(`Are you sure you want to permanently delete "${event.name}"?`)) return;
    this.eventApi.deleteEvent(event.id).subscribe({
      next: () => {
        this.myEvents.set(this.myEvents().filter(e => e.id !== event.id));
        this.drafts.set(this.drafts().filter(e => e.id !== event.id));
        this.toast.success('Event deleted.');
      },
      error: () => this.toast.error('Failed to delete event. Please try again.')
    });
  }

  onToggleCanceled(event: Event): void {
    const action$ = event.canceled
      ? this.eventApi.restoreEvent(event.id)
      : this.eventApi.cancelEvent(event.id);

    action$.subscribe({
      next: () => {
        this.myEvents.set(this.myEvents().map(e =>
          e.id === event.id ? { ...e, canceled: !e.canceled } : e
        ));
        this.toast.success(event.canceled ? 'Event restored.' : 'Event canceled.');
      },
      error: () => this.toast.error('Failed to update event status. Please try again.')
    });
  }

  onPublish(event: Event): void {
    this.eventApi.publishEvent(event.id).subscribe({
      next: () => {
        this.drafts.set(this.drafts().filter(e => e.id !== event.id));
        this.loadEvents();
        this.toast.success('Event published!');
      },
      error: () => this.toast.error('Failed to publish event.')
    });
  }

  private loadEvents(): void {
    this.eventApi.getAllEvents().subscribe({
      next: (events) => {
        const userEmail = this.auth.user()?.email;
        const mine = events.filter(e => e.organizer?.email === userEmail);
        this.myEvents.set(mine.sort((a, b) => new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime()));
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  private loadDrafts(): void {
    this.eventApi.getMyDrafts().subscribe({
      next: (drafts) => this.drafts.set(drafts),
      error: () => {}
    });
  }
}
