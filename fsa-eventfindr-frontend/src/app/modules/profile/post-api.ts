import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MediaUploadService } from '../../core/services/media-upload';
import { environment } from '../../../environments/environment';
import { Post, PostMediaItem } from '../events/event.model';

export interface PageResult<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

@Injectable({ providedIn: 'root' })
export class PostApi {
  private readonly http = inject(HttpClient);
  private readonly uploads = inject(MediaUploadService);

  createPost(content: string): Observable<{ id: number }> {
    return this.http.post<{ id: number }>(`${environment.beUrl}/posts`, { content });
  }

  getPostsByUser(userId: number): Observable<Post[]> {
    return this.http.get<Post[]>(`${environment.beUrl}/users/${userId}/posts`);
  }

  getPostsByUserPaginated(userId: number, page: number, size: number): Observable<PageResult<Post>> {
    return this.http.get<PageResult<Post>>(`${environment.beUrl}/users/${userId}/posts`, {
      params: { page: page.toString(), size: size.toString() }
    });
  }

  deletePost(postId: number): Observable<void> {
    return this.http.delete<void>(`${environment.beUrl}/posts/${postId}`);
  }

  uploadPostMedia(postId: number, file: File, onProgress?: (percent: number) => void): Observable<PostMediaItem> {
    const uploadBase = environment.uploadUrl || environment.beUrl;
    return this.uploads.upload(`${uploadBase}/posts/${postId}/media`, file, isPostMediaItem, onProgress);
  }

  deletePostMedia(postId: number, mediaId: number): Observable<void> {
    return this.http.delete<void>(`${environment.beUrl}/posts/${postId}/media/${mediaId}`);
  }

  resolvePostMediaUrl(url: string): string {
    if (url.startsWith('http')) return url;
    return (environment.uploadUrl || environment.beUrl) + url;
  }
}

function isPostMediaItem(value: unknown): value is PostMediaItem {
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
