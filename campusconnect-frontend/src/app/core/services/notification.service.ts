import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Notification } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private apiUrl = `${environment.apiUrl}/notifications`;
  unreadCount = signal<number>(0);

  constructor(private http: HttpClient) {}

  getByUser(userId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/user/${userId}`);
  }

  refreshUnreadCount(userId: number): void {
    this.http.get<{ unreadCount: number }>(`${this.apiUrl}/user/${userId}/unread-count`)
      .subscribe((res) => this.unreadCount.set(res.unreadCount));
  }

  markAsRead(id: number): Observable<Notification> {
    return this.http.patch<Notification>(`${this.apiUrl}/${id}/read`, {});
  }

  markAllAsRead(userId: number): Observable<string> {
    return this.http.patch(`${this.apiUrl}/user/${userId}/read-all`, {}, { responseType: 'text' })
      .pipe(tap(() => this.unreadCount.set(0)));
  }
}
