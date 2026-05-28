import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Notification } from '../../modules/events/event.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class NotificationApi {
  private readonly http = inject(HttpClient);

  getNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${environment.beUrl}/notifications`);
  }

  getUnreadNotificationCount(): Observable<number> {
    return this.http.get<{ count: number }>(`${environment.beUrl}/notifications/unread-count`).pipe(
      map(response => response.count)
    );
  }

  markNotificationAsRead(id: number): Observable<void> {
    return this.http.post<void>(`${environment.beUrl}/notifications/${id}/read`, {});
  }

  markAllNotificationsAsRead(): Observable<void> {
    return this.http.post<void>(`${environment.beUrl}/notifications/read-all`, {});
  }
}
