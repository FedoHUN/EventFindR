import { Component, ChangeDetectionStrategy, inject, signal, computed, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { EventApi } from '../../event-api';
import { Event } from '../../event.model';

@Component({
  selector: 'app-event-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, FormsModule, DatePipe, CurrencyPipe],
  templateUrl: './event-list.html',
  styleUrl: './event-list.scss'
})
export class EventListComponent implements OnInit {
  private readonly eventApi = inject(EventApi);

  readonly allEvents = signal<Event[]>([]);
  readonly loading = signal(true);
  readonly searchQuery = signal('');
  readonly locationFilter = signal('');
  readonly dateFilter = signal('');

  readonly locations = computed(() => {
    const locs = new Set(this.allEvents().map(e => e.location));
    return Array.from(locs).sort();
  });

  readonly filteredEvents = computed(() => {
    let events = this.allEvents();
    const query = this.searchQuery().toLowerCase();
    const location = this.locationFilter();
    const dateStr = this.dateFilter();

    if (query) {
      events = events.filter(e =>
        e.name.toLowerCase().includes(query) ||
        (e.performers?.toLowerCase().includes(query)) ||
        (e.description?.toLowerCase().includes(query))
      );
    }

    if (location) {
      events = events.filter(e => e.location === location);
    }

    if (dateStr) {
      const filterDate = new Date(dateStr);
      events = events.filter(e => {
        const eventDate = new Date(e.eventDate);
        return eventDate.toDateString() === filterDate.toDateString();
      });
    }

    return events.sort((a, b) =>
      new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime()
    );
  });

  ngOnInit(): void {
    this.eventApi.getAllEvents().subscribe({
      next: (events) => {
        this.allEvents.set(events);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  onSearchInput(value: string): void {
    this.searchQuery.set(value);
  }

  onLocationChange(value: string): void {
    this.locationFilter.set(value);
  }

  onDateChange(value: string): void {
    this.dateFilter.set(value);
  }

  clearFilters(): void {
    this.searchQuery.set('');
    this.locationFilter.set('');
    this.dateFilter.set('');
  }
}
