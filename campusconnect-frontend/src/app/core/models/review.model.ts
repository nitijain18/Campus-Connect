export interface Review {
  id: number;
  rating: number;
  reviewText?: string;
  eventId: number;
  reviewerName: string;
  createdAt: string;
}
