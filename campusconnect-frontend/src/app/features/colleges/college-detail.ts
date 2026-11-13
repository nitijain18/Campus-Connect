import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { CollegeService } from '../../core/services/college.service';
import { EventService } from '../../core/services/event.service';
import { College } from '../../core/models/user.model';
import { Event } from '../../core/models/event.model';
import { EventCard } from '../../shared/components/event-card';

@Component({
  selector: 'app-college-detail',
  standalone: true,
  imports: [CommonModule, EventCard],
  templateUrl: './college-detail.html'
})
export class CollegeDetail implements OnInit {
  college = signal<College | null>(null);
  events = signal<Event[]>([]);
  loading = signal<boolean>(true);

  constructor(
    private route: ActivatedRoute,
    private collegeService: CollegeService,
    private eventService: EventService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.collegeService.getById(id).subscribe({
      next: (college: College) => {
        this.college.set(college);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });

    this.eventService.getByCollege(id).subscribe({
      next: (events: Event[]) => {
        const visible = events.filter((e) => e.status === 'UPCOMING' || e.status === 'ONGOING');
        this.events.set(visible);
      }
    });
  }
}
