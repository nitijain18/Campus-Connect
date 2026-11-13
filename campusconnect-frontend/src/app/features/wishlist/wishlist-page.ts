import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { WishlistService } from '../../core/services/wishlist.service';
import { AuthService } from '../../core/services/auth.service';
import { Event } from '../../core/models/event.model';
import { EventCard } from '../../shared/components/event-card';

@Component({
  selector: 'app-wishlist-page',
  standalone: true,
  imports: [CommonModule, RouterLink, EventCard],
  templateUrl: './wishlist-page.html'
})
export class WishlistPage implements OnInit {
  events = signal<Event[]>([]);
  loading = signal<boolean>(true);

  constructor(
    private wishlistService: WishlistService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const userId = this.authService.currentUser()?.id;
    if (!userId) {
      this.loading.set(false);
      return;
    }

    this.wishlistService.getByUser(userId).subscribe({
      next: (wishlist) => {
        this.events.set(wishlist.events || []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
