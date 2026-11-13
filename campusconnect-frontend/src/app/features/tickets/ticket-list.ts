import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { TicketService } from '../../core/services/ticket.service';
import { AuthService } from '../../core/services/auth.service';
import { Ticket } from '../../core/models/order.model';

@Component({
  selector: 'app-ticket-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './ticket-list.html'
})
export class TicketList implements OnInit {
  tickets = signal<Ticket[]>([]);
  loading = signal<boolean>(true);
  expandedId = signal<number | null>(null);

  constructor(
    private ticketService: TicketService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const userId = this.authService.currentUser()?.id;
    if (!userId) {
      this.loading.set(false);
      return;
    }

    this.ticketService.getByStudent(userId).subscribe({
      next: (tickets: Ticket[]) => {
        this.tickets.set(tickets);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  toggleExpand(ticketId: number, event: Event): void {
    event.stopPropagation();
    this.expandedId.set(this.expandedId() === ticketId ? null : ticketId);
  }

  goToEvent(eventId: number): void {
    this.router.navigate(['/events', eventId]);
  }

  // Derives what the user should actually see, since Ticket.status alone doesn't
  // reflect the event having ended or been cancelled.
  displayStatus(ticket: Ticket): string {
    if (ticket.status === 'CANCELLED') return 'CANCELLED';
    if (ticket.status === 'USED') return 'USED';

    const event = ticket.order.event;
    if (event.status === 'CANCELLED') return 'EVENT CANCELLED';
    if (event.status === 'COMPLETED') return 'COMPLETED';

    const eventDateTime = new Date(event.eventDate + (event.eventTime ? 'T' + event.eventTime : ''));
    if (eventDateTime.getTime() < Date.now()) return 'COMPLETED';

    return 'ACTIVE';
  }

  statusClasses(ticket: Ticket): { bg: string; text: string } {
    const status = this.displayStatus(ticket);
    if (status === 'ACTIVE') return { bg: 'bg-green-100', text: 'text-green-700' };
    if (status === 'COMPLETED') return { bg: 'bg-blue-100', text: 'text-blue-700' };
    if (status === 'CANCELLED' || status === 'EVENT CANCELLED') return { bg: 'bg-red-100', text: 'text-red-700' };
    return { bg: 'bg-gray-100', text: 'text-gray-500' };
  }
}
