import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { VendorService } from '../../core/services/vendor.service';
import { EventService } from '../../core/services/event.service';
import { AuthService } from '../../core/services/auth.service';
import { Vendor } from '../../core/models/vendor.model';
import { Event } from '../../core/models/event.model';

@Component({
  selector: 'app-vendor-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './vendor-dashboard.html'
})
export class VendorDashboard implements OnInit {
  activeTab = signal<'overview' | 'events' | 'earnings'>('overview');
  vendor = signal<Vendor | null>(null);
  events = signal<Event[]>([]);
  earnings = signal<any>(null);
  loading = signal<boolean>(true);

  constructor(
    private vendorService: VendorService,
    private eventService: EventService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const userId = this.authService.currentUser()?.id;
    if (!userId) {
      this.loading.set(false);
      return;
    }

    this.vendorService.getByUserId(userId).subscribe({
      next: (vendor: Vendor) => {
        this.vendor.set(vendor);
        this.loadEvents(vendor.id);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  loadEvents(vendorId: number): void {
    this.eventService.getByVendor(vendorId).subscribe({
      next: (events: Event[]) => this.events.set(events)
    });
  }

  loadEarnings(): void {
    const v = this.vendor();
    if (!v) return;
    this.vendorService.getEarnings(v.id).subscribe({
      next: (data) => this.earnings.set(data)
    });
  }

  onTabChange(tab: 'overview' | 'events' | 'earnings'): void {
    this.activeTab.set(tab);
    if (tab === 'earnings' && !this.earnings()) {
      this.loadEarnings();
    }
  }
}
