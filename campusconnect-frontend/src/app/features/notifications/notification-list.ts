import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../core/services/notification.service';
import { AuthService } from '../../core/services/auth.service';
import { Notification } from '../../core/models/notification.model';

@Component({
  selector: 'app-notification-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-list.html'
})
export class NotificationList implements OnInit {
  notifications = signal<Notification[]>([]);
  loading = signal<boolean>(true);

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const userId = this.authService.currentUser()?.id;
    if (!userId) {
      this.loading.set(false);
      return;
    }

    this.notificationService.getByUser(userId).subscribe({
      next: (notifs: Notification[]) => {
        this.notifications.set(notifs);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  markRead(notif: Notification): void {
    if (notif.isRead) return;
    this.notificationService.markAsRead(notif.id).subscribe({
      next: () => {
        this.notifications.update((list) =>
          list.map((n) => (n.id === notif.id ? { ...n, isRead: true } : n))
        );
      }
    });
  }

  markAllRead(): void {
    const userId = this.authService.currentUser()?.id;
    if (!userId) return;
    this.notificationService.markAllAsRead(userId).subscribe({
      next: () => {
        this.notifications.update((list) => list.map((n) => ({ ...n, isRead: true })));
      }
    });
  }
}
