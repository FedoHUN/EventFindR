import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MediaUploadService } from '../../core/services/media-upload';
import { environment } from '../../../environments/environment';
import { EventMedia } from './event.model';

@Injectable({ providedIn: 'root' })
export class EventMediaApi {
  private readonly http = inject(HttpClient);
  private readonly uploads = inject(MediaUploadService);
  private readonly eventsUrl = `${environment.beUrl}/events`;

  uploadMedia(eventId: number, file: File, onProgress?: (percent: number) => void): Observable<EventMedia> {
    const uploadBase = `${environment.uploadUrl || environment.beUrl}/events`;
    return this.uploads.upload(`${uploadBase}/${eventId}/media`, file, isEventMedia, onProgress);
  }

  getEventMedia(eventId: number): Observable<EventMedia[]> {
    return this.http.get<EventMedia[]>(`${this.eventsUrl}/${eventId}/media`);
  }

  deleteMedia(eventId: number, mediaId: number): Observable<void> {
    return this.http.delete<void>(`${this.eventsUrl}/${eventId}/media/${mediaId}`);
  }

  reorderMedia(eventId: number, mediaIds: number[]): Observable<void> {
    return this.http.put<void>(`${this.eventsUrl}/${eventId}/media/reorder`, mediaIds);
  }

  resolveUrl(mediaUrl: string | undefined | null): string {
    if (!mediaUrl) return '';
    if (mediaUrl.startsWith('http')) return mediaUrl;
    return environment.beUrl + mediaUrl;
  }
}

function isEventMedia(value: unknown): value is EventMedia {
  return isRecord(value)
    && typeof value['id'] === 'number'
    && (value['mediaType'] === 'IMAGE' || value['mediaType'] === 'VIDEO')
    && typeof value['contentType'] === 'string'
    && typeof value['sortOrder'] === 'number'
    && typeof value['url'] === 'string';
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}
