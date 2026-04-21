import { Component, ChangeDetectionStrategy, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { EventApi } from '../../event-api';
import { AuthService } from '../../../../core/auth/auth';
import { Event, AttendanceStatus } from '../../event.model';

@Component({
  selector: 'app-event-detail',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe, CurrencyPipe],
  templateUrl: './event-detail.html',
  styleUrl: './event-detail.scss'
})
export class EventDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly eventApi = inject(EventApi);
  readonly auth = inject(AuthService);

  readonly event = signal<Event | null>(null);
  readonly loading = signal(true);
  readonly attendanceMessage = signal('');
  readonly attendanceError = signal('');

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.eventApi.getEventById(id).subscribe({
        next: (event) => {
          this.event.set(event);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
        }
      });
    }
  }

  markAttendance(status: AttendanceStatus): void {
    const ev = this.event();
    if (!ev) return;
    this.attendanceMessage.set('');
    this.attendanceError.set('');

    this.eventApi.attendEvent(ev.id, { status }).subscribe({
      next: () => {
        this.attendanceMessage.set(
          status === 'ATTENDING' ? 'You are attending this event!' : 'You are watching this event!'
        );
      },
      error: (err) => {
        if (err.status === 409) {
          this.attendanceError.set('You have already registered for this event.');
        } else if (err.status === 401) {
          this.attendanceError.set('Please log in to mark attendance.');
        } else {
          this.attendanceError.set('Something went wrong. Please try again.');
        }
      }
    });
  }
}
