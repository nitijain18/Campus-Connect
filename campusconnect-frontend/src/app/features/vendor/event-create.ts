import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { EventService } from '../../core/services/event.service';
import { VendorService } from '../../core/services/vendor.service';
import { CollegeService } from '../../core/services/college.service';
import { EventCategoryService } from '../../core/services/event-category.service';
import { UploadService } from '../../core/services/upload.service';
import { AuthService } from '../../core/services/auth.service';
import { College } from '../../core/models/user.model';
import { EventCategory } from '../../core/models/event.model';
import { Vendor } from '../../core/models/vendor.model';

interface FilePreview {
  file: File;
  url: string;
}

@Component({
  selector: 'app-event-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './event-create.html'
})
export class EventCreate implements OnInit {
  form: FormGroup;
  colleges = signal<College[]>([]);
  categories = signal<EventCategory[]>([]);
  vendor = signal<Vendor | null>(null);
  loading = signal<boolean>(false);
  uploadingImages = signal<boolean>(false);
  errorMessage = signal<string>('');
  filePreviews = signal<FilePreview[]>([]);

  constructor(
    private fb: FormBuilder,
    private eventService: EventService,
    private vendorService: VendorService,
    private collegeService: CollegeService,
    private eventCategoryService: EventCategoryService,
    private uploadService: UploadService,
    private authService: AuthService,
    private router: Router
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
      collegeId: [null, Validators.required],
      categoryId: [null]
    });
  }

  ngOnInit(): void {
    this.collegeService.getAll().subscribe({ next: (c: College[]) => this.colleges.set(c) });
    this.eventCategoryService.getAll().subscribe({ next: (c: EventCategory[]) => this.categories.set(c) });

    const userId = this.authService.currentUser()?.id;
    if (userId) {
      this.vendorService.getByUserId(userId).subscribe({
        next: (vendor: Vendor) => this.vendor.set(vendor)
      });
    }
  }

  onFileSelected(event: any): void {
    const newFiles: File[] = Array.from(event.target.files);
    const newPreviews = newFiles.map((file) => ({ file, url: URL.createObjectURL(file) }));
    this.filePreviews.set([...this.filePreviews(), ...newPreviews]);
    event.target.value = ''; // reset so picking the same file(s) again still fires change
  }

  removeFile(index: number): void {
    const updated = [...this.filePreviews()];
    URL.revokeObjectURL(updated[index].url);
    updated.splice(index, 1);
    this.filePreviews.set(updated);
  }

  onSubmit(): void {
    const v = this.vendor();
    if (this.form.invalid || !v) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

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
      venueName: value.venueName || null,
      vendor: { id: v.id },
      college: { id: value.collegeId }
    };

    if (value.categoryId) payload.category = { id: value.categoryId };

    this.eventService.create(payload).subscribe({
      next: (event) => {
        this.loading.set(false);

        if (this.filePreviews().length > 0) {
          this.uploadImages(event.id);
        } else {
          this.router.navigate(['/events', event.id]);
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message || err.error || 'Failed to create event.');
      }
    });
  }

  uploadImages(eventId: number): void {
    this.uploadingImages.set(true);
    const files = this.filePreviews().map((p) => p.file);
    this.uploadService.uploadEventImages(eventId, files).subscribe({
      next: () => {
        this.uploadingImages.set(false);
        this.router.navigate(['/events', eventId]);
      },
      error: () => {
        this.uploadingImages.set(false);
        this.router.navigate(['/events', eventId]);
      }
    });
  }
}
