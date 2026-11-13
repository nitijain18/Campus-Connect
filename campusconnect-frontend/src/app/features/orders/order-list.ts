import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrderService } from '../../core/services/order.service';
import { AuthService } from '../../core/services/auth.service';
import { Order } from '../../core/models/order.model';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './order-list.html'
})
export class OrderList implements OnInit {
  orders = signal<Order[]>([]);
  loading = signal<boolean>(true);
  cancellingId = signal<number | null>(null);
  cancelError = signal<string>('');

  constructor(
    private orderService: OrderService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    const userId = this.authService.currentUser()?.id;
    if (!userId) {
      this.loading.set(false);
      return;
    }

    this.loading.set(true);
    this.orderService.getByStudent(userId).subscribe({
      next: (orders: Order[]) => {
        this.orders.set(orders.sort((a, b) => b.id - a.id));
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  cancelOrder(orderId: number, event: MouseEvent): void {
    event.stopPropagation();
    event.preventDefault();
    this.cancellingId.set(orderId);
    this.cancelError.set('');

    this.orderService.cancel(orderId).subscribe({
      next: () => {
        this.cancellingId.set(null);
        this.loadOrders();
      },
      error: (err) => {
        this.cancellingId.set(null);
        this.cancelError.set(err.error?.message || 'Cancellation failed.');
      }
    });
  }

  canCancel(order: Order): boolean {
    return order.status === 'PENDING' || order.status === 'CONFIRMED';
  }
}
