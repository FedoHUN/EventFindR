import { ChangeDetectionStrategy, Component, inject, OnInit, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { EventApi } from '../../event-api';
import { Event } from '../../event.model';

@Component({
  selector: 'app-my-calendar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe],
  templateUrl: './my-calendar.html',
  styleUrl: './my-calendar.scss'
})
export class MyCalendarComponent implements OnInit {
  readonly eventApi = inject(EventApi);
  readonly events = signal<Event[]>([]);
  readonly loading = signal(true);
  readonly filter = signal<'all' | 'upcoming' | 'past'>('upcoming');

  readonly filteredEvents = computed(() => {
    const now = Date.now();
    const all = this.events();
    switch (this.filter()) {
      case 'upcoming':
        return all.filter(e => new Date(e.eventDate).getTime() >= now);
      case 'past':
        return all.filter(e => new Date(e.eventDate).getTime() < now);
      default:
        return all;
    }
  });

  readonly groupedEvents = computed(() => {
    const events = this.filteredEvents();
    const groups = new Map<string, Event[]>();
    for (const ev of events) {
      const date = new Date(ev.eventDate);
      const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
      if (!groups.has(key)) groups.set(key, []);
      groups.get(key)!.push(ev);
    }
    return Array.from(groups.entries())
      .sort((a, b) => a[0].localeCompare(b[0]))
      .map(([key, evs]) => ({
        label: new Date(key + '-01').toLocaleDateString('en-US', { year: 'numeric', month: 'long' }),
        events: evs.sort((a, b) => new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime())
      }));
  });

  ngOnInit(): void {
    this.eventApi.getMyAttendances().subscribe({
      next: (events) => {
        this.events.set(events);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  setFilter(f: 'all' | 'upcoming' | 'past'): void {
    this.filter.set(f);
  }

  isUpcoming(event: Event): boolean {
    return new Date(event.eventDate).getTime() >= Date.now();
  }

  daysUntil(event: Event): number {
    return Math.ceil((new Date(event.eventDate).getTime() - Date.now()) / 86400000);
  }
}
