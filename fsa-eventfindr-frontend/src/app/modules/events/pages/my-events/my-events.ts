import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { EventApi } from '../../event-api';
import { AuthService } from '../../../../core/auth/auth';
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

  readonly myEvents = signal<Event[]>([]);
  readonly loading = signal(true);

  ngOnInit(): void {
    this.eventApi.getAllEvents().subscribe({
      next: (events) => {
        const userEmail = this.auth.user()?.email;
        const mine = events.filter(e => e.organizer?.email === userEmail && !this.eventApi.isDeleted(e.id));
        this.myEvents.set(mine.sort((a, b) => new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime()));
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  isPast(event: Event): boolean {
    return new Date(event.eventDate).getTime() < new Date().setHours(0,0,0,0);
  }

  onEdit(event: Event): void {
    // Open create page in edit mode (backend update not implemented) - prefill is not supported yet.
    // Navigate to create and pass ?editId= to allow future enhancement.
    this.router.navigate(['/events/create'], { queryParams: { editId: event.id } });
  }

  onDelete(event: Event): void {
    if (!confirm(`Delete event "${event.name}"? This is a local action until backend delete is implemented.`)) return;
    this.eventApi.markDeleted(event.id);
    // remove locally
    this.myEvents.set(this.myEvents().filter(e => e.id !== event.id));
  }

  onToggleCanceled(event: Event): void {
    this.eventApi.toggleCanceled(event.id);
    // refresh list so UI updates
    this.myEvents.set(this.myEvents().slice());
  }
}


