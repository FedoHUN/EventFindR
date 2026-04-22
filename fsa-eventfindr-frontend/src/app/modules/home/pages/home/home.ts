import { Component, ChangeDetectionStrategy, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { EventApi } from '../../../events/event-api';
import { Event } from '../../../events/event.model';
import { User } from '../../../../core/auth/auth.model';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-home',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe, CurrencyPipe],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class HomeComponent implements OnInit {
  private readonly eventApi = inject(EventApi);
  private readonly http = inject(HttpClient);

  readonly featuredEvents = signal<Event[]>([]);
  readonly organizers = signal<Partial<User>[]>([]);
  readonly loading = signal(true);

  ngOnInit(): void {
    this.eventApi.getAllEvents().subscribe({
      next: (events) => {
        const sorted = [...events].sort((a, b) =>
          new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime()
        );
        this.featuredEvents.set(sorted.slice(0, 6));
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });

    this.http.get<Partial<User>[]>(`${environment.beUrl}/users/organizers`).subscribe({
      next: (organizers) => this.organizers.set(organizers),
      error: () => {}
    });
  }
}
