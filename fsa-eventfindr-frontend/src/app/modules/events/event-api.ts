import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of, throwError } from 'rxjs';
import { Event, EventsResponse, CreateEventRequest, AttendEventRequest, AttendanceResponse, AttendanceStatus } from './event.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class EventApi {
  private readonly http = inject(HttpClient);
  private readonly _url = environment.beUrl + '/events';
  private readonly storagePrefix = 'ef_events_';

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

  // --- Local-only helpers for organizer actions (stored in browser localStorage).
  private readLocalMap(key: string): Record<string, boolean> {
    try {
      const raw = localStorage.getItem(this.storagePrefix + key);
      return raw ? JSON.parse(raw) : {};
    } catch (e) {
      return {};
    }
  }

  private writeLocalMap(key: string, map: Record<string, boolean>): void {
    try {
      localStorage.setItem(this.storagePrefix + key, JSON.stringify(map));
    } catch (e) {
      // ignore
    }
  }

  isCanceled(eventId: number): boolean {
    const map = this.readLocalMap('canceled');
    return !!map[eventId];
  }

  toggleCanceled(eventId: number): void {
    const map = this.readLocalMap('canceled');
    map[eventId] = !map[eventId];
    this.writeLocalMap('canceled', map);
  }

  isDeleted(eventId: number): boolean {
    const map = this.readLocalMap('deleted');
    return !!map[eventId];
  }

  markDeleted(eventId: number): void {
    const map = this.readLocalMap('deleted');
    map[eventId] = true;
    this.writeLocalMap('deleted', map);
  }
}
