import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CollegeService } from '../../../core/services/college.service';
import { College } from '../../../core/models/user.model';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './signup.html'
})
export class SignupComponent implements OnInit {
  form: FormGroup;
  colleges = signal<College[]>([]);
  loading = signal<boolean>(false);
  errorMessage = signal<string>('');

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private collegeService: CollegeService,
    private router: Router
  ) {
    this.form = this.fb.group({
      firstName: ['', Validators.required],
      lastName: [''],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      phoneNumber: [''],
      collegeId: [null, Validators.required]
    });
  }

  ngOnInit(): void {
    this.collegeService.getAll().subscribe({
      next: (colleges: College[]) => this.colleges.set(colleges)
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    this.authService.signup(this.form.value).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/verify-otp'], { queryParams: { email: this.form.value.email } });
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error || 'Signup failed. Please try again.');
      }
    });
  }
}
