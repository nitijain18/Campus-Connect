import { Routes } from '@angular/router';
import { SignupComponent } from './features/auth/signup/signup';
import { LoginComponent } from './features/auth/login/login';
import { VerifyOtpComponent } from './features/auth/verify-otp/verify-otp';
import { Home } from './features/home/home/home';
import { EventList } from './features/events/event-list';
import { EventDetail } from './features/events/event-detail';
import { CollegeList } from './features/colleges/college-list';
import { CollegeDetail } from './features/colleges/college-detail';
import { VendorApply } from './features/vendor/vendor-apply';
import { VendorDashboard } from './features/vendor/vendor-dashboard';
import { EventCreate } from './features/vendor/event-create';
import { EventEdit } from './features/vendor/event-edit';
import { AdminDashboard } from './features/admin/admin-dashboard';
import { NotificationList } from './features/notifications/notification-list';
import { TicketList } from './features/tickets/ticket-list';
import { OrderList } from './features/orders/order-list';
import { OrderDetail } from './features/orders/order-detail';
import { WishlistPage } from './features/wishlist/wishlist-page';
import { ProfilePage } from './features/profile/profile-page';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { vendorGuard } from './core/guards/vendor.guard';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'signup', component: SignupComponent },
  { path: 'login', component: LoginComponent },
  { path: 'verify-otp', component: VerifyOtpComponent },
  { path: 'events', component: EventList },
  { path: 'events/:id', component: EventDetail },
  { path: 'colleges', component: CollegeList },
  { path: 'colleges/:id', component: CollegeDetail },
  { path: 'vendor/apply', component: VendorApply, canActivate: [authGuard] },
  { path: 'vendor/dashboard', component: VendorDashboard, canActivate: [vendorGuard] },
  { path: 'vendor/events/new', component: EventCreate, canActivate: [vendorGuard] },
  { path: 'vendor/events/:id/edit', component: EventEdit, canActivate: [vendorGuard] },
  { path: 'admin', component: AdminDashboard, canActivate: [adminGuard] },
  { path: 'notifications', component: NotificationList, canActivate: [authGuard] },
  { path: 'tickets', component: TicketList, canActivate: [authGuard] },
  { path: 'orders', component: OrderList, canActivate: [authGuard] },
  { path: 'orders/:id', component: OrderDetail, canActivate: [authGuard] },
  { path: 'wishlist', component: WishlistPage, canActivate: [authGuard] },
  { path: 'profile', component: ProfilePage, canActivate: [authGuard] }
];
