import { Component, Input, Output, EventEmitter, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportService } from '../../core/services/report.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-report-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './report-dialog.html'
})
export class ReportDialog {
  @Input() eventId: number | null = null;
  @Input() vendorId: number | null = null;
  @Output() closed = new EventEmitter<void>();

  reason = '';
  description = '';
  submitting = signal<boolean>(false);
  submitted = signal<boolean>(false);
  error = signal<string>('');

  constructor(
    private reportService: ReportService,
    private authService: AuthService
  ) {}

  submit(): void {
    const userId = this.authService.currentUser()?.id;
    if (!userId || !this.reason.trim()) {
      this.error.set('Please provide a reason.');
      return;
    }

    this.submitting.set(true);
    this.error.set('');

    const payload: any = {
      reason: this.reason,
      description: this.description,
      reportedBy: { id: userId }
    };
    if (this.eventId) payload.event = { id: this.eventId };
    if (this.vendorId) payload.vendor = { id: this.vendorId };

    this.reportService.create(payload).subscribe({
      next: () => {
        this.submitting.set(false);
        this.submitted.set(true);
      },
      error: () => {
        this.submitting.set(false);
        this.error.set('Failed to submit report.');
      }
    });
  }

  close(): void {
    this.closed.emit();
  }
}
