export type UserRole = 'ROLE_STUDENT' | 'ROLE_VENDOR';

export interface College {
  id: number;
  name: string;
  city?: string;
  state?: string;
  address?: string;
  website?: string;
  logoUrl?: string;
}

export interface User {
  id: number;
  firstName: string;
  lastName?: string;
  email: string;
  phoneNumber?: string;
  profileImage?: string;
  role: UserRole;
  isAdmin: boolean;
  isVerified: boolean;
  college?: College;
  createdAt?: string;
}
