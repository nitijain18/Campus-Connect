import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Ticket } from '../models/order.model';

@Injectable({ providedIn: 'root' })
export class TicketService {
  private apiUrl = `${environment.apiUrl}/tickets`;

  constructor(private http: HttpClient) {}

  getByStudent(studentId: number): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.apiUrl}/student/${studentId}`);
  }

  getByOrder(orderId: number): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.apiUrl}/order/${orderId}`);
  }
}
