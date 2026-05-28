import { HttpErrorResponse } from '@angular/common/http';

export interface ApiError {
  status: number;
  code: string;
  message: string;
  details: readonly string[];
  path?: string;
  timestamp?: string;
}

interface BackendErrorBody {
  code?: unknown;
  message?: unknown;
  details?: unknown;
  path?: unknown;
  timestamp?: unknown;
}

export function toApiError(error: unknown): ApiError {
  if (error instanceof HttpErrorResponse) {
    const body = isBackendErrorBody(error.error) ? error.error : undefined;
    return {
      status: error.status,
      code: stringOrDefault(body?.code, error.status ? `HTTP_${error.status}` : 'NETWORK_ERROR'),
      message: stringOrDefault(body?.message, fallbackMessage(error)),
      details: stringArrayOrEmpty(body?.details),
      path: typeof body?.path === 'string' ? body.path : error.url ?? undefined,
      timestamp: typeof body?.timestamp === 'string' ? body.timestamp : undefined,
    };
  }

  if (isRecord(error)) {
    return {
      status: typeof error['status'] === 'number' ? error['status'] : 0,
      code: 'UNKNOWN_ERROR',
      message: stringOrDefault(error['message'], 'Something went wrong.'),
      details: [],
    };
  }

  return {
    status: 0,
    code: 'UNKNOWN_ERROR',
    message: 'Something went wrong.',
    details: [],
  };
}

function fallbackMessage(error: HttpErrorResponse): string {
  if (error.status === 0) return 'Unable to reach the server. Check your connection and try again.';
  if (error.status === 401) return 'Please log in to continue.';
  if (error.status === 403) return 'You do not have permission to perform this action.';
  if (error.status === 404) return 'The requested resource was not found.';
  if (error.status >= 500) return 'The server is unavailable. Please try again later.';
  return error.message || 'Something went wrong.';
}

function stringOrDefault(value: unknown, fallback: string): string {
  return typeof value === 'string' && value.trim() ? value : fallback;
}

function stringArrayOrEmpty(value: unknown): readonly string[] {
  return Array.isArray(value) ? value.filter((item): item is string => typeof item === 'string') : [];
}

function isBackendErrorBody(value: unknown): value is BackendErrorBody {
  return isRecord(value);
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}
