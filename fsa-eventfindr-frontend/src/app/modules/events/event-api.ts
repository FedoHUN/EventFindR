import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Event, EventsResponse, CreateEventRequest, AttendEventRequest } from './event.model';

@Injectable({ providedIn: 'root' })
export class EventApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api';

  getAllEvents(): Observable<Event[]> {
    return this.http.get<EventsResponse>(`${this.baseUrl}/events`).pipe(
      map(response => response.events)
    );
  }

  getEventById(id: number): Observable<Event> {
    return this.http.get<Event>(`${this.baseUrl}/events/${id}`);
  }

  createEvent(request: CreateEventRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/events`, request);
  }

  attendEvent(eventId: number, request: AttendEventRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/events/${eventId}/attend`, request);
  }
}
