import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of, throwError } from 'rxjs';
import { Event, EventsResponse, CreateEventRequest, AttendEventRequest, AttendanceResponse, AttendanceStatus } from './event.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class EventApi {
  private readonly http = inject(HttpClient);
  private readonly _url = environment.beUrl + '/events';

  getAllEvents(): Observable<Event[]> {
    return this.http.get<EventsResponse>(this._url).pipe(
      map(response => response.events)
    );
  }

  getEventById(id: number): Observable<Event> {
    return this.http.get<Event>(`${this._url}/${id}`);
  }

  createEvent(request: CreateEventRequest): Observable<void> {
    return this.http.post<void>(this._url, request);
  }

  attendEvent(eventId: number, request: AttendEventRequest): Observable<void> {
    return this.http.post<void>(`${this._url}/${eventId}/attend`, request);
  }

  getMyAttendance(eventId: number): Observable<AttendanceStatus | null> {
    return this.http.get<AttendanceResponse>(`${this._url}/${eventId}/attend`).pipe(
      map(response => response.status),
      catchError((err) => (err.status === 404 ? of(null) : throwError(() => err)))
    );
  }

  removeAttendance(eventId: number): Observable<void> {
    return this.http.delete<void>(`${this._url}/${eventId}/attend`);
  }
}
