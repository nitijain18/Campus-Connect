export type NotificationType =
  | 'ORDER_CONFIRMED'
  | 'PAYMENT_SUCCESS'
  | 'VENDOR_APPROVED'
  | 'EVENT_REMOVED'
  | 'GENERAL'
  | 'RETURN_INITIATED';

export interface Notification {
  id: number;
  message: string;
  type: NotificationType;
  isRead: boolean;
  createdAt: string;
}
