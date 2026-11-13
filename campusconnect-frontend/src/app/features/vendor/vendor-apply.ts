import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { VendorService } from '../../core/services/vendor.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-vendor-apply',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './vendor-apply.html'
})
export class VendorApply {
  form: FormGroup;
  loading = signal<boolean>(false);
  errorMessage = signal<string>('');
  submitted = signal<boolean>(false);

  constructor(
    private fb: FormBuilder,
    private vendorService: VendorService,
    private authService: AuthService
  ) {
    this.form = this.fb.group({
      businessName: ['', Validators.required],
      description: [''],
      city: ['', Validators.required],
      gstin: [''],
      bankAccountNumber: ['', Validators.required],
      ifscCode: ['', Validators.required],
      accountHolderName: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const userId = this.authService.currentUser()?.id;
    if (!userId) {
      this.errorMessage.set('You must be logged in to apply.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    const payload = { ...this.form.value, user: { id: userId } };

    this.vendorService.apply(payload)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => this.submitted.set(true),
        error: (err) => this.errorMessage.set(err.error?.message || err.error || 'Application failed. Please try again.')
      });
  }
}
