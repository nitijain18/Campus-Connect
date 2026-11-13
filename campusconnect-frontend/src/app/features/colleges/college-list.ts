import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CollegeService } from '../../core/services/college.service';
import { College } from '../../core/models/user.model';

@Component({
  selector: 'app-college-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './college-list.html'
})
export class CollegeList implements OnInit {
  colleges = signal<College[]>([]);
  loading = signal<boolean>(true);

  constructor(private collegeService: CollegeService) {}

  ngOnInit(): void {
    this.collegeService.getAll().subscribe({
      next: (colleges: College[]) => {
        this.colleges.set(colleges);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
