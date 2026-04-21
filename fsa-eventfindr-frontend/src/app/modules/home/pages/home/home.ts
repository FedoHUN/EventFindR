import { Component, ChangeDetectionStrategy, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { EventApi } from '../../../events/event-api';
import { Event } from '../../../events/event.model';

@Component({
  selector: 'app-home',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe, CurrencyPipe],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class HomeComponent implements OnInit {
  private readonly eventApi = inject(EventApi);

  readonly featuredEvents = signal<Event[]>([]);
  readonly organizers = signal<{ name: string; eventCount: number }[]>([]);
  readonly loading = signal(true);

  ngOnInit(): void {
    this.eventApi.getAllEvents().subscribe({
      next: (events) => {
        const sorted = [...events].sort((a, b) =>
          new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime()
        );
        this.featuredEvents.set(sorted.slice(0, 6));

        const orgMap = new Map<string, number>();
        events.forEach(e => {
          if (e.organizer?.name) {
            orgMap.set(e.organizer.name, (orgMap.get(e.organizer.name) ?? 0) + 1);
          }
        });
        const orgList = Array.from(orgMap.entries())
          .map(([name, eventCount]) => ({ name, eventCount }))
          .sort((a, b) => b.eventCount - a.eventCount)
          .slice(0, 4);
        this.organizers.set(orgList);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }
}
