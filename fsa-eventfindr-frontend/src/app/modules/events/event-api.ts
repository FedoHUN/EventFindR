import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, catchError, map, of, throwError } from 'rxjs';
import {
  AttendEventRequest,
  AttendanceResponse,
  AttendanceStatus,
  CreateEventRequest,
  Event,
  EventsResponse,
} from './event.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class EventApi {
  private readonly http = inject(HttpClient);
  private readonly eventsUrl = `${environment.beUrl}/events`;

  resolveImageUrl(imageUrl: string | undefined | null): string {
    if (!imageUrl) return '';
    if (imageUrl.startsWith('http')) return imageUrl;
    if (imageUrl.startsWith('/')) return environment.beUrl + imageUrl;
    return imageUrl;
  }

  getAllEvents(): Observable<Event[]> {
    return this.http.get<EventsResponse>(this.eventsUrl).pipe(
      map(response => response.events)
    );
  }

  getEventById(id: number): Observable<Event> {
    return this.http.get<Event>(`${this.eventsUrl}/${id}`);
  }

  createEvent(request: CreateEventRequest): Observable<number> {
    return this.http.post(this.eventsUrl, request, { observe: 'response' }).pipe(
      map((response: HttpResponse<unknown>) => {
        const eventId = response.headers.get('X-Event-Id');
        if (eventId) return Number(eventId);

        const location = response.headers.get('Location');
        if (!location) return 0;

        const parts = location.split('/');
        return Number(parts[parts.length - 1]);
      })
    );
  }

  updateEvent(id: number, request: CreateEventRequest): Observable<Event> {
    return this.http.put<Event>(`${this.eventsUrl}/${id}`, request);
  }

  deleteEvent(eventId: number): Observable<void> {
    return this.http.delete<void>(`${this.eventsUrl}/${eventId}`);
  }

  cancelEvent(eventId: number): Observable<void> {
    return this.http.post<void>(`${this.eventsUrl}/${eventId}/cancel`, {});
  }

  restoreEvent(eventId: number): Observable<void> {
    return this.http.post<void>(`${this.eventsUrl}/${eventId}/restore`, {});
  }

  publishEvent(eventId: number): Observable<void> {
    return this.http.post<void>(`${this.eventsUrl}/${eventId}/publish`, {});
  }

  toggleFeatured(eventId: number): Observable<void> {
    return this.http.post<void>(`${this.eventsUrl}/${eventId}/toggle-featured`, {});
  }

  attendEvent(eventId: number, request: AttendEventRequest): Observable<void> {
    return this.http.post<void>(`${this.eventsUrl}/${eventId}/attend`, request);
  }

  getMyAttendance(eventId: number): Observable<AttendanceStatus | null> {
    return this.http.get<AttendanceResponse>(`${this.eventsUrl}/${eventId}/attend`).pipe(
      map(response => response.status),
      catchError(error => error.status === 404 ? of(null) : throwError(() => error))
    );
  }

  removeAttendance(eventId: number): Observable<void> {
    return this.http.delete<void>(`${this.eventsUrl}/${eventId}/attend`);
  }

  getMyAttendances(): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.eventsUrl}/my-attendances`);
  }

  getMyPerformances(): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.eventsUrl}/my-performances`);
  }

  getMyDrafts(): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.eventsUrl}/my-drafts`);
  }

  getTrendingEvents(): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.eventsUrl}/trending`);
  }

  getSimilarEvents(eventId: number): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.eventsUrl}/${eventId}/similar`);
  }

  getAttendanceCounts(eventId: number): Observable<{ attending: number; watching: number }> {
    return this.http.get<{ attending: number; watching: number }>(`${this.eventsUrl}/${eventId}/attendance-counts`);
  }

  getEventsByOrganizer(organizerId: number): Observable<Event[]> {
    return this.getAllEvents().pipe(
      map(events => events.filter(event => event.organizer?.id === organizerId && !event.canceled))
    );
  }
}
