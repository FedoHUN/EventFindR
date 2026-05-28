import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../../core/auth/auth.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class UserApi {
  private readonly http = inject(HttpClient);

  searchArtists(query: string): Observable<User[]> {
    return this.http.get<User[]>(`${environment.beUrl}/artists/search`, {
      params: query ? { q: query } : {}
    });
  }

  getAllArtists(): Observable<User[]> {
    return this.http.get<User[]>(`${environment.beUrl}/artists/search`);
  }

  getOrganizers(): Observable<User[]> {
    return this.http.get<User[]>(`${environment.beUrl}/users/organizers`);
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${environment.beUrl}/users/${id}`);
  }
}
