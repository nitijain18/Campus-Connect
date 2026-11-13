import { Component, Input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EventMediaService } from '../../core/services/event-media.service';
import { WishlistService } from '../../core/services/wishlist.service';
import { AuthService } from '../../core/services/auth.service';
import { Event } from '../../core/models/event.model';

@Component({
  selector: 'app-event-card',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './event-card.html'
})
export class EventCard implements OnInit {
  @Input({ required: true }) event!: Event;

  thumbnailUrl = signal<string | null>(null);
  inWishlist = signal<boolean>(false);
  wishlistLoading = signal<boolean>(false);

  constructor(
    private eventMediaService: EventMediaService,
    private wishlistService: WishlistService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.eventMediaService.getByEvent(this.event.id).subscribe({
      next: (media) => {
        if (media.length > 0) {
          this.thumbnailUrl.set(media[0].imageUrl);
        }
      }
    });

    const userId = this.authService.currentUser()?.id;
    if (userId) {
      this.wishlistService.getByUser(userId).subscribe({
        next: (wishlist) => {
          this.inWishlist.set(wishlist.events?.some((e) => e.id === this.event.id) || false);
        }
      });
    }
  }

  toggleWishlist(mouseEvent: MouseEvent): void {
    mouseEvent.stopPropagation();
    mouseEvent.preventDefault();

    const userId = this.authService.currentUser()?.id;
    if (!userId) return;

    this.wishlistLoading.set(true);
    const action = this.inWishlist()
      ? this.wishlistService.remove(userId, this.event.id)
      : this.wishlistService.add(userId, this.event.id);

    action.subscribe({
      next: () => {
        this.inWishlist.set(!this.inWishlist());
        this.wishlistLoading.set(false);
      },
      error: () => this.wishlistLoading.set(false)
    });
  }
}
