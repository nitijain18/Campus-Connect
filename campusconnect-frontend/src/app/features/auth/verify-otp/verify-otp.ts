import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-verify-otp',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './verify-otp.html'
})
export class VerifyOtpComponent implements OnInit {
  form: FormGroup;
  email = '';
  loading = signal<boolean>(false);
  resending = signal<boolean>(false);
  errorMessage = signal<string>('');
  successMessage = signal<string>('');

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.form = this.fb.group({
      otp: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
    });
  }

  ngOnInit(): void {
    this.email = this.route.snapshot.queryParamMap.get('email') || '';
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    this.authService.verifyOtp({ email: this.email, otp: this.form.value.otp }).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error || 'Verification failed.');
      }
    });
  }

  resendOtp(): void {
    this.resending.set(true);
    this.successMessage.set('');
    this.authService.resendOtp(this.email).subscribe({
      next: () => {
        this.resending.set(false);
        this.successMessage.set('OTP resent to your email.');
      },
      error: () => {
        this.resending.set(false);
        this.errorMessage.set('Could not resend OTP.');
      }
    });
  }
}
