import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { EventService } from '../../core/services/event.service';
import { CollegeService } from '../../core/services/college.service';
import { EventCategoryService } from '../../core/services/event-category.service';
import { Event, EventCategory } from '../../core/models/event.model';
import { College } from '../../core/models/user.model';
import { EventCard } from '../../shared/components/event-card';

@Component({
  selector: 'app-event-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, EventCard],
  templateUrl: './event-list.html'
})
export class EventList implements OnInit {
  events = signal<Event[]>([]);
  colleges = signal<College[]>([]);
  categories = signal<EventCategory[]>([]);
  loading = signal<boolean>(true);

  filters = {
    keyword: '',
    category: null as number | null,
    college: null as number | null,
    minPrice: null as number | null,
    maxPrice: null as number | null,
    date: ''
  };

  constructor(
    private eventService: EventService,
    private collegeService: CollegeService,
    private eventCategoryService: EventCategoryService
  ) {}

  ngOnInit(): void {
    this.collegeService.getAll().subscribe({
      next: (colleges: College[]) => this.colleges.set(colleges)
    });

    this.eventCategoryService.getAll().subscribe({
      next: (categories: EventCategory[]) => this.categories.set(categories)
    });

    this.loadEvents();
  }

  loadEvents(): void {
    this.loading.set(true);
    this.eventService.search({
      category: this.filters.category ?? undefined,
      college: this.filters.college ?? undefined,
      minPrice: this.filters.minPrice ?? undefined,
      maxPrice: this.filters.maxPrice ?? undefined,
      date: this.filters.date || undefined,
      keyword: this.filters.keyword || undefined
    }).subscribe({
      next: (events: Event[]) => {
        // Hide events that have already ended or been cancelled from the browse view
        const visible = events.filter((e) => e.status === 'UPCOMING' || e.status === 'ONGOING');
        this.events.set(visible);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  onFilterChange(): void {
    this.loadEvents();
  }

  clearFilters(): void {
    this.filters = { keyword: '', category: null, college: null, minPrice: null, maxPrice: null, date: '' };
    this.loadEvents();
  }
}
