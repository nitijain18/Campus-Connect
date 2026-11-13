import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { OrderService } from '../../core/services/order.service';
import { TicketService } from '../../core/services/ticket.service';
import { Order, Ticket } from '../../core/models/order.model';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './order-detail.html'
})
export class OrderDetail implements OnInit {
  order = signal<Order | null>(null);
  tickets = signal<Ticket[]>([]);
  loading = signal<boolean>(true);

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService,
    private ticketService: TicketService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.orderService.getById(id).subscribe({
      next: (order: Order) => {
        this.order.set(order);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });

    this.ticketService.getByOrder(id).subscribe({
      next: (tickets: Ticket[]) => this.tickets.set(tickets)
    });
  }
}
