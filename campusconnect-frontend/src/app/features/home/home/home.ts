import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EventService } from '../../../core/services/event.service';
import { HomeCategoryService } from '../../../core/services/home-category.service';
import { AuthService } from '../../../core/services/auth.service';
import { Event, HomeCategory } from '../../../core/models/event.model';
import { EventCard } from '../../../shared/components/event-card';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, EventCard],
  templateUrl: './home.html'
})
export class Home implements OnInit {
  categories = signal<HomeCategory[]>([]);
  featuredEvents = signal<Event[]>([]);
  loading = signal<boolean>(true);

  constructor(
    private eventService: EventService,
    private homeCategoryService: HomeCategoryService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.homeCategoryService.getActive().subscribe({
      next: (categories: HomeCategory[]) => this.categories.set(categories)
    });

    this.eventService.getAll().subscribe({
      next: (events: Event[]) => {
        this.featuredEvents.set(events.filter((e: Event) => e.status === 'UPCOMING').slice(0, 6));
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
