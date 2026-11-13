import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VendorService } from '../../core/services/vendor.service';
import { EventCategoryService } from '../../core/services/event-category.service';
import { CollegeService } from '../../core/services/college.service';
import { EventService } from '../../core/services/event.service';
import { UserService } from '../../core/services/user.service';
import { PaymentService } from '../../core/services/payment.service';
import { ReportService, VendorReport } from '../../core/services/report.service';
import { AdminService, AdminOverview } from '../../core/services/admin.service';
import { Vendor } from '../../core/models/vendor.model';
import { EventCategory, Event } from '../../core/models/event.model';
import { College, User } from '../../core/models/user.model';

type Tab = 'overview' | 'vendors' | 'categories' | 'colleges' | 'events' | 'students' | 'payments' | 'reports';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-dashboard.html'
})
export class AdminDashboard implements OnInit {
  activeTab = signal<Tab>('overview');

  // Overview
  overview = signal<AdminOverview | null>(null);

  // Vendors
  pendingVendors = signal<Vendor[]>([]);
  vendorsLoading = signal<boolean>(true);
  rejectingId = signal<number | null>(null);
  rejectReason = '';

  // Categories
  categories = signal<EventCategory[]>([]);
  newCategory = { name: '', description: '' };
  categorySubmitting = signal<boolean>(false);

  // Colleges
  colleges = signal<College[]>([]);
  newCollege = { name: '', city: '', state: '', address: '', website: '' };
  collegeSubmitting = signal<boolean>(false);

  // Events
  events = signal<Event[]>([]);
  removingEventId = signal<number | null>(null);
  removeReason = '';

  // Students
  students = signal<User[]>([]);

  // Payments
  payments = signal<any[]>([]);

  // Reports
  openReports = signal<VendorReport[]>([]);

  constructor(
    private vendorService: VendorService,
    private eventCategoryService: EventCategoryService,
    private collegeService: CollegeService,
    private eventService: EventService,
    private userService: UserService,
    private paymentService: PaymentService,
    private reportService: ReportService,
    private adminService: AdminService
  ) {}

  ngOnInit(): void {
    this.loadOverview();
    this.loadPendingVendors();
  }

  setTab(tab: Tab): void {
    this.activeTab.set(tab);
    if (tab === 'categories' && this.categories().length === 0) this.loadCategories();
    if (tab === 'colleges' && this.colleges().length === 0) this.loadColleges();
    if (tab === 'events' && this.events().length === 0) this.loadEvents();
    if (tab === 'students' && this.students().length === 0) this.loadStudents();
    if (tab === 'payments' && this.payments().length === 0) this.loadPayments();
    if (tab === 'reports' && this.openReports().length === 0) this.loadReports();
  }

  // ---------- Overview ----------
  loadOverview(): void {
    this.adminService.getOverview().subscribe({ next: (data) => this.overview.set(data) });
  }

  // ---------- Vendors ----------
  loadPendingVendors(): void {
    this.vendorsLoading.set(true);
    this.vendorService.getPending().subscribe({
      next: (vendors: Vendor[]) => {
        this.pendingVendors.set(vendors);
        this.vendorsLoading.set(false);
      },
      error: () => this.vendorsLoading.set(false)
    });
  }

  approve(id: number): void {
    this.vendorService.approve(id).subscribe({ next: () => this.loadPendingVendors() });
  }

  startReject(id: number): void {
    this.rejectingId.set(id);
    this.rejectReason = '';
  }

  confirmReject(id: number): void {
    if (!this.rejectReason.trim()) return;
    this.vendorService.reject(id, this.rejectReason).subscribe({
      next: () => {
        this.rejectingId.set(null);
        this.loadPendingVendors();
      }
    });
  }

  cancelReject(): void {
    this.rejectingId.set(null);
  }

  // ---------- Categories ----------
  loadCategories(): void {
    this.eventCategoryService.getAll().subscribe({ next: (c: EventCategory[]) => this.categories.set(c) });
  }

  submitCategory(): void {
    if (!this.newCategory.name) return;
    this.categorySubmitting.set(true);
    this.eventCategoryService.create(this.newCategory).subscribe({
      next: () => {
        this.categorySubmitting.set(false);
        this.newCategory = { name: '', description: '' };
        this.loadCategories();
      },
      error: () => this.categorySubmitting.set(false)
    });
  }

  // ---------- Colleges ----------
  loadColleges(): void {
    this.collegeService.getAll().subscribe({ next: (c: College[]) => this.colleges.set(c) });
  }

  submitCollege(): void {
    if (!this.newCollege.name) return;
    this.collegeSubmitting.set(true);
    this.collegeService.create(this.newCollege).subscribe({
      next: () => {
        this.collegeSubmitting.set(false);
        this.newCollege = { name: '', city: '', state: '', address: '', website: '' };
        this.loadColleges();
      },
      error: () => this.collegeSubmitting.set(false)
    });
  }

  deleteCollege(id: number): void {
    this.collegeService.delete(id).subscribe({ next: () => this.loadColleges() });
  }

  // ---------- Events ----------
  loadEvents(): void {
    this.eventService.getAll().subscribe({ next: (e: Event[]) => this.events.set(e) });
  }

  startRemoveEvent(id: number): void {
    this.removingEventId.set(id);
    this.removeReason = '';
  }

  confirmRemoveEvent(id: number): void {
    if (!this.removeReason.trim()) return;
    this.eventService.remove(id, this.removeReason).subscribe({
      next: () => {
        this.removingEventId.set(null);
        this.loadEvents();
      }
    });
  }

  cancelRemoveEvent(): void {
    this.removingEventId.set(null);
  }

  // ---------- Students ----------
  loadStudents(): void {
    this.userService.getAll().subscribe({ next: (u: User[]) => this.students.set(u) });
  }

  deleteStudent(id: number): void {
    this.userService.delete(id).subscribe({ next: () => this.loadStudents() });
  }

  // ---------- Payments ----------
  loadPayments(): void {
    this.paymentService.getAll().subscribe({ next: (p: any[]) => this.payments.set(p) });
  }

  // ---------- Reports ----------
  loadReports(): void {
    this.reportService.getByStatus('OPEN').subscribe({ next: (r: VendorReport[]) => this.openReports.set(r) });
  }

  resolveReport(id: number): void {
    this.reportService.resolve(id).subscribe({ next: () => this.loadReports() });
  }
}
