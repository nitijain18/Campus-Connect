import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EventService } from '../../core/services/event.service';
import { CollegeService } from '../../core/services/college.service';
import { EventCategoryService } from '../../core/services/event-category.service';
import { UploadService } from '../../core/services/upload.service';
import { College } from '../../core/models/user.model';
import { EventCategory, Event } from '../../core/models/event.model';

interface FilePreview {
  file: File;
  url: string;
}

@Component({
  selector: 'app-event-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './event-edit.html'
})
export class EventEdit implements OnInit {
  form: FormGroup;
  eventId!: number;
  colleges = signal<College[]>([]);
  categories = signal<EventCategory[]>([]);
  loading = signal<boolean>(false);
  fetching = signal<boolean>(true);
  uploadingImages = signal<boolean>(false);
  errorMessage = signal<string>('');
  successMessage = signal<string>('');
  filePreviews = signal<FilePreview[]>([]);

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private eventService: EventService,
    private collegeService: CollegeService,
    private eventCategoryService: EventCategoryService,
    private uploadService: UploadService
  ) {
    this.form = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      eventDate: ['', Validators.required],
      eventTime: [''],
      registrationDeadline: [''],
      price: [0, [Validators.required, Validators.min(0)]],
      totalSeats: [1, [Validators.required, Validators.min(1)]],
      isOnline: [false],
      meetLink: [''],
      venueName: [''],
      categoryId: [null]
    });
  }

  ngOnInit(): void {
    this.eventId = Number(this.route.snapshot.paramMap.get('id'));

    this.collegeService.getAll().subscribe({ next: (c: College[]) => this.colleges.set(c) });
    this.eventCategoryService.getAll().subscribe({ next: (c: EventCategory[]) => this.categories.set(c) });

    this.eventService.getById(this.eventId).subscribe({
      next: (event: Event) => {
        this.form.patchValue({
          title: event.title,
          description: event.description,
          eventDate: event.eventDate,
          eventTime: event.eventTime,
          registrationDeadline: event.registrationDeadline,
          price: event.price,
          totalSeats: event.totalSeats,
          isOnline: event.isOnline,
          meetLink: event.meetLink,
          venueName: event.venueName || '',
          categoryId: event.category?.id || null
        });
        this.fetching.set(false);
      },
      error: () => this.fetching.set(false)
    });
  }

  onFileSelected(event: any): void {
    const newFiles: File[] = Array.from(event.target.files);
    const newPreviews = newFiles.map((file) => ({ file, url: URL.createObjectURL(file) }));
    this.filePreviews.set([...this.filePreviews(), ...newPreviews]);
    event.target.value = '';
  }

  removeFile(index: number): void {
    const updated = [...this.filePreviews()];
    URL.revokeObjectURL(updated[index].url);
    updated.splice(index, 1);
    this.filePreviews.set(updated);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    const value = this.form.value;
    const payload: any = {
      title: value.title,
      description: value.description,
      eventDate: value.eventDate,
      eventTime: value.eventTime || null,
      registrationDeadline: value.registrationDeadline || null,
      price: value.price,
      totalSeats: value.totalSeats,
      isOnline: value.isOnline,
      meetLink: value.meetLink || null,
      venueName: value.venueName || null
    };

    if (value.categoryId) payload.category = { id: value.categoryId };

    this.eventService.update(this.eventId, payload).subscribe({
      next: () => {
        this.loading.set(false);
        if (this.filePreviews().length > 0) {
          this.uploadImages();
        } else {
          this.successMessage.set('Event updated successfully.');
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message || 'Update failed.');
      }
    });
  }

  uploadImages(): void {
    this.uploadingImages.set(true);
    const files = this.filePreviews().map((p) => p.file);
    this.uploadService.uploadEventImages(this.eventId, files).subscribe({
      next: () => {
        this.uploadingImages.set(false);
        this.successMessage.set('Event and images updated successfully.');
        this.filePreviews.set([]);
      },
      error: () => {
        this.uploadingImages.set(false);
        this.successMessage.set('Event updated, but image upload failed.');
      }
    });
  }

  goToEvent(): void {
    this.router.navigate(['/events', this.eventId]);
  }
}
