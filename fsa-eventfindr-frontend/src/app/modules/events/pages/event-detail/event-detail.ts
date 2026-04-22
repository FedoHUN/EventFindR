import { Component, ChangeDetectionStrategy, computed, effect, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { finalize, switchMap } from 'rxjs';
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
  readonly currentAttendance = signal<AttendanceStatus | null>(null);
  readonly hoveredAttendance = signal<AttendanceStatus | null>(null);
  readonly attendanceBusy = signal(false);

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

  toggleAttendance(status: AttendanceStatus): void {
    const ev = this.event();
    if (!ev || this.attendanceBusy()) return;

    if (!this.auth.user()) {
      this.auth.login();
      return;
    }

    this.attendanceMessage.set('');
    this.attendanceError.set('');
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
      })
    ).subscribe({
      next: () => {
        if (currentStatus === status) {
          this.currentAttendance.set(null);
          this.attendanceMessage.set(status === 'ATTENDING' ? 'Attendance removed.' : 'Watching removed.');
          return;
        }
        this.currentAttendance.set(status);
        this.attendanceMessage.set(
          status === 'ATTENDING' ? 'You are attending this event!' : 'You are watching this event!'
        );
      },
      error: (err) => {
        if (err.status === 401) {
          this.attendanceError.set('Please log in to manage attendance.');
          this.auth.login();
        } else {
          this.attendanceError.set('Something went wrong. Please try again.');
        }
      }
    });
  }

  private loadMyAttendance(eventId: number): void {
    this.eventApi.getMyAttendance(eventId).subscribe({
      next: (status) => this.currentAttendance.set(status),
      error: () => this.currentAttendance.set(null)
    });
  }
}
