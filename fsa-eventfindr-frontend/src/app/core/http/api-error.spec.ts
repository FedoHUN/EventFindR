import { HttpErrorResponse } from '@angular/common/http';
import { describe, expect, it } from 'vitest';
import { toApiError } from './api-error';

describe('toApiError', () => {
  it('maps backend error envelopes', () => {
    const error = new HttpErrorResponse({
      status: 403,
      url: '/events/1',
      error: {
        code: 'FORBIDDEN',
        message: 'You do not have permission.',
        details: ['owner required'],
        path: '/events/1',
        timestamp: '2026-05-27T20:00:00Z',
      }
    });

    expect(toApiError(error)).toEqual({
      status: 403,
      code: 'FORBIDDEN',
      message: 'You do not have permission.',
      details: ['owner required'],
      path: '/events/1',
      timestamp: '2026-05-27T20:00:00Z',
    });
  });

  it('uses safe fallback messages for network errors', () => {
    const error = new HttpErrorResponse({ status: 0, error: new ProgressEvent('error') });

    expect(toApiError(error).message).toBe('Unable to reach the server. Check your connection and try again.');
  });
});
