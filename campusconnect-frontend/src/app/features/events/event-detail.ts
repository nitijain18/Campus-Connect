import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EventService } from '../../core/services/event.service';
import { EventMediaService } from '../../core/services/event-media.service';
import { OrderService } from '../../core/services/order.service';
import { PaymentService } from '../../core/services/payment.service';
import { WishlistService } from '../../core/services/wishlist.service';
import { ReviewService } from '../../core/services/review.service';
import { AuthService } from '../../core/services/auth.service';
import { Event, EventMedia } from '../../core/models/event.model';
import { ReportDialog } from '../../shared/components/report-dialog';

const RAZORPAY_KEY_ID = 'rzp_test_TBhoscJHapbUg5';

@Component({
  selector: 'app-event-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, ReportDialog],
  templateUrl: './event-detail.html'
})
export class EventDetail implements OnInit {
  event = signal<Event | null>(null);
  media = signal<EventMedia[]>([]);
  activeImageIndex = signal<number>(0);
  loading = signal<boolean>(true);
  notFound = signal<boolean>(false);

  booking = signal<boolean>(false);
  bookingError = signal<string>('');
  bookingSuccess = signal<boolean>(false);

  inWishlist = signal<boolean>(false);
  wishlistLoading = signal<boolean>(false);

  reviews = signal<any[]>([]);
  myConfirmedOrder = signal<any>(null);
  reviewRating = 5;
  reviewText = '';
  submittingReview = signal<boolean>(false);
  reviewSubmitted = signal<boolean>(false);

  showReportDialog = signal<boolean>(false);

  constructor(
    private route: ActivatedRoute,
    private eventService: EventService,
    private eventMediaService: EventMediaService,
    private orderService: OrderService,
    private paymentService: PaymentService,
    private wishlistService: WishlistService,
    private reviewService: ReviewService,
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.eventService.getById(id).subscribe({
      next: (event: Event) => {
        this.event.set(event);
        this.loading.set(false);
        this.checkWishlist(id);
      },
      error: () => {
        this.notFound.set(true);
        this.loading.set(false);
      }
    });

    this.eventMediaService.getByEvent(id).subscribe({
      next: (media: EventMedia[]) => this.media.set(media)
    });

    this.reviewService.getByEvent(id).subscribe({
      next: (reviews) => this.reviews.set(reviews)
    });

    const userId = this.authService.currentUser()?.id;
    if (userId) {
      this.orderService.getByStudent(userId).subscribe({
        next: (orders: any[]) => {
          const confirmed = orders.find((o) => o.event.id === id && o.status === 'CONFIRMED');
          this.myConfirmedOrder.set(confirmed || null);
        }
      });
    }
  }

  selectImage(index: number): void {
    this.activeImageIndex.set(index);
  }

  checkWishlist(eventId: number): void {
    const userId = this.authService.currentUser()?.id;
    if (!userId) return;
    this.wishlistService.getByUser(userId).subscribe({
      next: (wishlist) => {
        this.inWishlist.set(wishlist.events?.some((e) => e.id === eventId) || false);
      }
    });
  }

  toggleWishlist(): void {
    const userId = this.authService.currentUser()?.id;
    const event = this.event();
    if (!userId || !event) {
      this.router.navigate(['/login']);
      return;
    }

    this.wishlistLoading.set(true);
    const action = this.inWishlist()
      ? this.wishlistService.remove(userId, event.id)
      : this.wishlistService.add(userId, event.id);

    action.subscribe({
      next: () => {
        this.inWishlist.set(!this.inWishlist());
        this.wishlistLoading.set(false);
      },
      error: () => this.wishlistLoading.set(false)
    });
  }

  bookNow(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    const event = this.event();
    const user = this.authService.currentUser();
    if (!event || !user) return;

    this.booking.set(true);
    this.bookingError.set('');

    this.orderService.placeOrder(user.id, event.id, 1).subscribe({
      next: (response: any) => {
        if (response.status) {
          this.booking.set(false);
          this.bookingSuccess.set(true);
          return;
        }
        this.openRazorpayCheckout(response, user, event);
      },
      error: (err) => {
        this.booking.set(false);
        this.bookingError.set(err.error?.message || err.error || 'Booking failed. Please try again.');
      }
    });
  }

  private openRazorpayCheckout(response: any, user: any, event: Event): void {
    const options = {
      key: RAZORPAY_KEY_ID,
      amount: Math.round(response.amount * 100),
      currency: response.currency || 'INR',
      name: 'CampusConnect',
      description: event.title,
      order_id: response.razorpayOrderId,
      prefill: {
        name: `${user.firstName} ${user.lastName || ''}`,
        email: user.email,
        contact: user.phoneNumber || ''
      },
      theme: { color: '#4f46e5' },
      handler: (razorpayResponse: any) => {
        this.verifyPayment(razorpayResponse);
      },
      modal: {
        ondismiss: () => {
          this.booking.set(false);
          this.bookingError.set('Payment cancelled.');
        }
      }
    };

    const rzp = new (window as any).Razorpay(options);
    rzp.open();
  }

  private verifyPayment(razorpayResponse: any): void {
    this.paymentService.verifyPayment({
      razorpay_order_id: razorpayResponse.razorpay_order_id,
      razorpay_payment_id: razorpayResponse.razorpay_payment_id,
      razorpay_signature: razorpayResponse.razorpay_signature
    }).subscribe({
      next: () => {
        this.booking.set(false);
        this.bookingSuccess.set(true);
      },
      error: () => {
        this.booking.set(false);
        this.bookingError.set('Payment verification failed. If money was deducted, contact support.');
      }
    });
  }

  submitReview(): void {
    const order = this.myConfirmedOrder();
    const userId = this.authService.currentUser()?.id;
    const event = this.event();
    if (!order || !userId || !event) return;

    this.submittingReview.set(true);
    this.reviewService.create({
      rating: this.reviewRating,
      reviewText: this.reviewText,
      event: { id: event.id },
      reviewer: { id: userId },
      order: { id: order.id }
    }).subscribe({
      next: (review) => {
        this.submittingReview.set(false);
        this.reviewSubmitted.set(true);
        this.reviews.set([...this.reviews(), review]);
      },
      error: () => this.submittingReview.set(false)
    });
  }

  openReportDialog(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.showReportDialog.set(true);
  }

  closeReportDialog(): void {
    this.showReportDialog.set(false);
  }
}
