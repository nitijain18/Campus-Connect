import { User } from './user.model';

export interface Vendor {
  id: number;
  businessName: string;
  description?: string;
  city?: string;
  bannerImage?: string;
  gstin?: string;
  bankAccountNumber?: string;
  ifscCode?: string;
  accountHolderName?: string;
  isApproved: boolean;
  rejectionReason?: string;
  totalEarnings: number;
  pendingEarnings: number;
  totalEventsCreated: number;
  user: User;
  createdAt?: string;
}
