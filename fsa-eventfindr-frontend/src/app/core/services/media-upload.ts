import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { OAuthService } from 'angular-oauth2-oidc';

export type UploadResponseGuard<T> = (value: unknown) => value is T;

@Injectable({ providedIn: 'root' })
export class MediaUploadService {
  private readonly oauthService = inject(OAuthService);

  upload<T>(url: string, file: File, isExpectedResponse: UploadResponseGuard<T>, onProgress?: (percent: number) => void): Observable<T> {
    return new Observable<T>(subscriber => {
      const formData = new FormData();
      formData.append('file', file);

      const xhr = new XMLHttpRequest();
      xhr.open('POST', url);

      const token = this.oauthService.getAccessToken();
      if (token && this.oauthService.hasValidAccessToken()) {
        xhr.setRequestHeader('Authorization', `Bearer ${token}`);
      }

      xhr.upload.onprogress = (event) => {
        if (event.lengthComputable && onProgress) {
          onProgress(Math.round((event.loaded / event.total) * 100));
        }
      };

      xhr.onload = () => {
        if (xhr.status < 200 || xhr.status >= 300) {
          subscriber.error({ status: xhr.status, message: xhr.statusText });
          return;
        }

        const parsed = parseJson(xhr.responseText);
        if (!isExpectedResponse(parsed)) {
          subscriber.error({ status: xhr.status, message: 'Invalid upload response' });
          return;
        }

        subscriber.next(parsed);
        subscriber.complete();
      };

      xhr.onerror = () => subscriber.error({ status: 0, message: 'Network error' });
      xhr.send(formData);

      return () => xhr.abort();
    });
  }
}

function parseJson(value: string): unknown {
  try {
    return JSON.parse(value) as unknown;
  } catch {
    return undefined;
  }
}
