export interface User {
  id: number;
  name: string;
  rola: UserRole;
  email: string;
}

export interface CreateUserRequest {
  name: string;
  email: string;
  rola: UserRole;
}

export type UserRole = 'USER' | 'ORGANIZER' | 'ADMIN';
