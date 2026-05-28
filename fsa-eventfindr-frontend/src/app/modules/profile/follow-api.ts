import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../../core/auth/auth.model';
import { FollowStatus } from '../events/event.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class FollowApi {
  private readonly http = inject(HttpClient);

  followUser(userId: number): Observable<void> {
    return this.http.post<void>(`${environment.beUrl}/users/${userId}/follow`, {});
  }

  unfollowUser(userId: number): Observable<void> {
    return this.http.delete<void>(`${environment.beUrl}/users/${userId}/follow`);
  }

  getFollowStatus(userId: number): Observable<FollowStatus> {
    return this.http.get<FollowStatus>(`${environment.beUrl}/users/${userId}/follow`);
  }

  getMyFollowing(): Observable<User[]> {
    return this.http.get<User[]>(`${environment.beUrl}/users/me/following`);
  }

  getMyFollowers(): Observable<{ followers: User[]; count: number }> {
    return this.http.get<{ followers: User[]; count: number }>(`${environment.beUrl}/users/me/followers`);
  }
}
