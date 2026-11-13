import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Order } from '../models/order.model';

export interface PlaceOrderResponse {
  orderId?: number;
  razorpayOrderId?: string;
  amount?: number;
  currency?: string;
  // For free events, the backend returns the Order object directly instead
  id?: number;
  status?: string;
}

@Injectable({ providedIn: 'root' })
export class OrderService {
  private apiUrl = `${environment.apiUrl}/orders`;

  constructor(private http: HttpClient) {}

  placeOrder(studentId: number, eventId: number, quantity: number = 1): Observable<PlaceOrderResponse> {
    return this.http.post<PlaceOrderResponse>(`${this.apiUrl}/place/${studentId}`, { eventId, quantity });
  }

  getByStudent(studentId: number): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/student/${studentId}`);
  }

  getById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/${id}`);
  }

  cancel(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}/cancel`);
  }
}
