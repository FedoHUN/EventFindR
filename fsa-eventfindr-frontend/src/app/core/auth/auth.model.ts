export interface User {
  id: number;
  name: string;
  role: UserRole;
  email: string;
  organizationName?: string;
  organizationDescription?: string;
  artistName?: string;
  artistDescription?: string;
}

export interface CreateUserRequest {
  name: string;
  email: string;
  role: UserRole;
}

export type UserRole = 'USER' | 'ORGANIZER' | 'ARTIST' | 'ADMIN';
