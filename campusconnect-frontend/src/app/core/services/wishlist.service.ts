import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Event } from '../models/event.model';

export interface Wishlist {
  id: number;
  events: Event[];
}

@Injectable({ providedIn: 'root' })
export class WishlistService {
  private apiUrl = `${environment.apiUrl}/wishlist`;

  constructor(private http: HttpClient) {}

  getByUser(userId: number): Observable<Wishlist> {
    return this.http.get<Wishlist>(`${this.apiUrl}/user/${userId}`);
  }

  add(userId: number, eventId: number): Observable<Wishlist> {
    return this.http.post<Wishlist>(`${this.apiUrl}/add`, { userId, eventId });
  }

  remove(userId: number, eventId: number): Observable<Wishlist> {
    return this.http.delete<Wishlist>(`${this.apiUrl}/remove`, { params: { userId, eventId } });
  }
}
