import { User } from '../../core/auth/auth.model';

export interface Event {
  id: number;
  name: string;
  description?: string;
  location: string;
  eventDate: string;
  price?: number;
  ticketUrl?: string;
  imageUrl?: string;
  performers?: string;
  created?: string;
  organizer?: User;
}

export interface CreateEventRequest {
  name: string;
  location: string;
  eventDate: string;
  description?: string;
  price?: number;
  ticketUrl?: string;
  imageUrl?: string;
  performers?: string;
}

export interface AttendEventRequest {
  status: AttendanceStatus;
}

export type AttendanceStatus = 'ATTENDING' | 'WATCHING';

export interface EventsResponse {
  events: Event[];
}

export interface ErrorResponse {
  code: string;
  message: string;
  details?: string[];
  timestamp: string;
  path: string;
}
