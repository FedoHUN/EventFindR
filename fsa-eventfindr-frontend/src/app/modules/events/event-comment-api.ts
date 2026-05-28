import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { EventComment } from './event.model';

@Injectable({ providedIn: 'root' })
export class EventCommentApi {
  private readonly http = inject(HttpClient);
  private readonly eventsUrl = `${environment.beUrl}/events`;

  getComments(eventId: number): Observable<EventComment[]> {
    return this.http.get<EventComment[]>(`${this.eventsUrl}/${eventId}/comments`);
  }

  addComment(eventId: number, content: string, rating?: number): Observable<EventComment> {
    return this.http.post<EventComment>(`${this.eventsUrl}/${eventId}/comments`, { content, rating: rating ?? null });
  }

  deleteComment(commentId: number): Observable<void> {
    return this.http.delete<void>(`${environment.beUrl}/comments/${commentId}`);
  }
}
