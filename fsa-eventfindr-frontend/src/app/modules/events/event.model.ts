import { User } from '../../core/auth/auth.model';

export interface EventArtist {
  id: number;
  artistUserId?: number;
  artistName: string;
  sortOrder: number;
}

export interface EventArtistRequest {
  artistUserId?: number;
  artistName: string;
}

export interface Event {
  id: number;
  name: string;
  description?: string;
  location: string;
  eventDate: string;
  price?: number;
  ticketUrl?: string;
  imageUrl?: string;
  genre?: string;
  status?: 'DRAFT' | 'PUBLISHED';
  featured?: boolean;
  canceled?: boolean;
  capacity?: number;
  attendingCount?: number;
  watchingCount?: number;
  commentCount?: number;
  averageRating?: number;
  ratingCount?: number;
  artists?: EventArtist[];
  created?: string;
  organizer?: User;
}

export interface EventMedia {
  id: number;
  mediaType: 'IMAGE' | 'VIDEO';
  contentType: string;
  sortOrder: number;
  url: string;
}

export interface CreateEventRequest {
  name: string;
  location: string;
  eventDate: string;
  description?: string;
  price?: number;
  ticketUrl?: string;
  imageUrl?: string;
  genre?: string;
  status?: 'DRAFT' | 'PUBLISHED';
  capacity?: number;
  artists?: EventArtistRequest[];
}

export interface EventComment {
  id: number;
  eventId: number;
  user: User;
  content: string;
  rating?: number;
  created: string;
}

export interface Notification {
  id: number;
  userId: number;
  eventId?: number;
  type: 'EVENT_REMINDER' | 'EVENT_CANCELED' | 'NEW_COMMENT' | 'NEW_FOLLOWER' | 'NEW_EVENT' | 'EVENT_UPDATED';
  message: string;
  read: boolean;
  created: string;
}

export interface FollowStatus {
  following: boolean;
  followerCount: number;
}

export interface AttendEventRequest {
  status: AttendanceStatus;
}

export type AttendanceStatus = 'ATTENDING' | 'WATCHING';

export interface AttendanceResponse {
  status: AttendanceStatus;
}

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

export interface Post {
  id: number;
  content: string;
  created: string;
  updated?: string;
  author: User;
  media: PostMediaItem[];
  mediaCount: number;
}

export interface PostMediaItem {
  id: number;
  mediaType: 'IMAGE' | 'VIDEO';
  contentType: string;
  sortOrder: number;
  url: string;
}
