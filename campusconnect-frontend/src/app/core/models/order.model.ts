import { User } from './user.model';
import { Event } from './event.model';

export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'REFUNDED';
export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';
export type TicketStatus = 'ACTIVE' | 'USED' | 'CANCELLED';

export interface Order {
  id: number;
  totalAmount: number;
  platformCommission: number;
  status: OrderStatus;
  orderDate: string;
  student: User;
  event: Event;
  createdAt?: string;
}

export interface Ticket {
  id: number;
  ticketNumber: string;
  qrCodeUrl?: string;
  status: TicketStatus;
  order: Order;
  attendeeName: string;
  attendeeEmail: string;
}

export interface Payment {
  id: number;
  amount: number;
  platformFee: number;
  status: PaymentStatus;
  paymentMethod: 'RAZORPAY' | 'FREE';
  razorpayOrderId?: string;
  order: Order;
  paidAt?: string;
}
