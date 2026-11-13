import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../core/services/user.service';
import { UploadService } from '../../core/services/upload.service';
import { AuthService } from '../../core/services/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile-page.html'
})
export class ProfilePage implements OnInit {
  form: FormGroup;
  loading = signal<boolean>(false);
  uploadingPhoto = signal<boolean>(false);
  successMessage = signal<string>('');
  errorMessage = signal<string>('');

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    public authService: AuthService,
    private http: HttpClient
  ) {
    this.form = this.fb.group({
      firstName: ['', Validators.required],
      lastName: [''],
      phoneNumber: ['']
    });
  }

  ngOnInit(): void {
    const user = this.authService.currentUser();
    if (user) {
      this.form.patchValue({
        firstName: user.firstName,
        lastName: user.lastName,
        phoneNumber: user.phoneNumber
      });
    }
  }

  onSubmit(): void {
    const user = this.authService.currentUser();
    if (this.form.invalid || !user) return;

    this.loading.set(true);
    this.successMessage.set('');
    this.errorMessage.set('');

    this.userService.update(user.id, this.form.value).subscribe({
      next: () => {
        this.loading.set(false);
        this.successMessage.set('Profile updated successfully.');
        this.authService.refreshUser().subscribe();
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message || 'Update failed.');
      }
    });
  }

  onPhotoSelected(event: any): void {
    const file: File = event.target.files[0];
    const user = this.authService.currentUser();
    if (!file || !user) return;

    this.uploadingPhoto.set(true);
    const formData = new FormData();
    formData.append('file', file);

    this.http.post(`${environment.apiUrl}/upload/user/${user.id}/profile`, formData).subscribe({
      next: () => {
        this.uploadingPhoto.set(false);
        this.authService.refreshUser().subscribe();
      },
      error: () => this.uploadingPhoto.set(false)
    });
  }
}
