import { Vendor } from './vendor.model';
import { College } from './user.model';

export type EventStatus = 'UPCOMING' | 'ONGOING' | 'COMPLETED' | 'CANCELLED';

export interface Venue {
  id: number;
  name: string;
  address?: string;
  capacity?: number;
  college?: College;
  amenities?: string;
  imageUrl?: string;
}

export interface EventCategory {
  id: number;
  name: string;
  description?: string;
  imageUrl?: string;
}

export interface HomeCategory {
  id: number;
  name: string;
  imageUrl?: string;
  displayOrder?: number;
  isActive: boolean;
}

export interface EventMedia {
  id: number;
  imageUrl: string;
  mediaType: 'IMAGE' | 'VIDEO';
  displayOrder: number;
}

export interface Event {
  id: number;
  title: string;
  description?: string;
  eventDate: string;
  eventTime?: string;
  registrationDeadline?: string;
  price: number;
  totalSeats: number;
  availableSeats: number;
  status: EventStatus;
  isOnline: boolean;
  meetLink?: string;
  venueName?: string;
  vendor: Vendor;
  venue?: Venue;
  college: College;
  category?: EventCategory;
  averageRating: number;
  totalReviews: number;
  platformCommission?: number;
  createdAt?: string;
}
